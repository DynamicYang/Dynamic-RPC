package org.dynamic.rpc.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.IDGenerator;
import org.dynamic.rpc.NettyBootstrapInitializer;
import org.dynamic.rpc.annotation.TryTimes;
import org.dynamic.rpc.compress.CompressorFactory;
import org.dynamic.rpc.config.Configuration;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.exception.NetworkException;
import org.dynamic.rpc.protection.CircuitBreaker;
import org.dynamic.rpc.serialization.SerializerFactory;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.request.Payload;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author: DynamicYang
 * @create: 2024-02-16
 * @Description: 封装客户端通信的基础逻辑，每一个代理对象的远程调用过程都封装在了invoke方法中
 */
@Slf4j
public class ConsumerInvocationHandler implements InvocationHandler {


    //从接口中获取判断是否需要重试


    private Registry registry;
    private Class<?> serviceInterface;

    public ConsumerInvocationHandler(Registry registry, Class<?> serviceInterface){
        this.registry = registry;
        this.serviceInterface = serviceInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ExecutionException, InterruptedException, TimeoutException {


        TryTimes tryTimesAnnotation = method.getAnnotation(TryTimes.class);

        int tryTimes = 0;
        int intervalTime = 0;
        if (tryTimesAnnotation != null) {
            tryTimes = tryTimesAnnotation.tryTimes();
            intervalTime = tryTimesAnnotation.intervalTime();
        }

        while (true) {

            /*
             * ---------------------报文封装--------------------------
             *
             *
             */




            Payload payload = Payload.builder()
                    .interfaceName(serviceInterface.getName())
                    .methodName(method.getName())
                    .parameterTypes(method.getParameterTypes())
                    .parametersValues(args)
                    .returnType(method.getReturnType())
                    .build();
            Configuration configuration = DynamicBootstrap.getInstance().getConfiguration();

            DynamicRPCRequest rpcRequest = DynamicRPCRequest.builder()
                    .requestId(configuration.getIdGenerator().getId())
                    .compressType(CompressorFactory.getCompressorWrapper(configuration.getCompressorType()).getCode())
                    .requestType(RequestType.REQUEST.getId())
                    .serializationType(SerializerFactory.getSerializerWrapper(configuration.getSerializerType()).getCode())
                    .timeStamp(System.currentTimeMillis())
                    .payload(payload).build();

            //记得释放
            DynamicBootstrap.RPC_REQUEST.set(rpcRequest);




            InetSocketAddress address = configuration.getLoadBalancer().select(serviceInterface.getName());


            if (log.isDebugEnabled()) {
                log.debug("服务调用方发现了，服务【{}】可用主机【{}】", serviceInterface.getName(), address.getAddress());
            }

            Map<SocketAddress, CircuitBreaker> everyHostCircuitBreaker = configuration.getEveryIpCircuitBreaker();

            CircuitBreaker circuitBreaker = everyHostCircuitBreaker.get(address);

            if(circuitBreaker == null){
                circuitBreaker = new CircuitBreaker(10,0.5F);
                everyHostCircuitBreaker.put(address, circuitBreaker);
            }

            try {
                if(rpcRequest.getRequestType() == RequestType.HEART_BEAT.getId() && circuitBreaker.isBreak()){
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                           configuration.getEveryIpCircuitBreaker().get(address).reset();
                        }
                    }, 5000);

                    throw new RuntimeException("服务熔断了,请稍后再试");
                }
            Channel channel = getAvailableChannel(address);
                if (log.isDebugEnabled()){
                    log.debug("获取到与【{}】的通道，准备发送数据", address.getAddress());
                }



/*
            *info 同步方案
            *
            *
            * await  另起线程没有异常处理 sync在主线程抛出异常
            *
            ChannelFuture = channel.writeAndFlush(new Object());
            if(channelFuture.isDone()){
                Object result = channelFuture.getNow();
            }else if(!channelFuture.isSuccess()){
                Throwable cause = channelFuture.cause();
                throw new RuntimeException(cause);
            }
*/

            // *info 异步策略


            CompletableFuture<Object> completableFuture = new CompletableFuture<>();
            // *info 使用全局的completableFuture
            DynamicBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);
// *info promise
            channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {

                if (!promise.isSuccess()) {
                    completableFuture.completeExceptionally(promise.cause());
                }
            });


//            Object o = completableFuture.get(3,TimeUnit.SECONDS);
            // q: 如过没有处理这个completableFuture会阻塞点当前线程，等待complete方法的执行
            //需要在哪里调用complete方法得到结果？，很明显是pipeline中最终handler的处理结果
//        try {
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } catch (ExecutionException e) {
//            throw new RuntimeException(e);
//        } catch (TimeoutException e) {
//            throw new RuntimeException(e);
//        }
            Object result = completableFuture.get(10, TimeUnit.SECONDS);
            DynamicBootstrap.RPC_REQUEST.remove();
            circuitBreaker.recordRequest();
            return result;
            }catch (Exception e){
                tryTimes--;
                circuitBreaker.recordErrorRequest();
                try {
                    Thread.sleep(intervalTime);
                }catch (InterruptedException e1){
                    log.error("在进行重试时发生异常",e1);
                }

                if(tryTimes <=0 ){
                    log.error("对方法【{}】进行远程调用时，重试{}次，服务依然不可用",method.getName(),tryTimes);
                    break;
                }
            }
        }
        throw new RuntimeException("执行远程方法" + method.getName() + "时，调用失败");
    }


    /**
     * @Param 远程地址
     * @return channel
     **/

    private Channel getAvailableChannel(InetSocketAddress address){

        Channel channel = DynamicBootstrap.CHANNEL_CACHE.get(address);


        if(channel == null){

            CompletableFuture<Channel> channelFuture =  new CompletableFuture<>();
            NettyBootstrapInitializer.getBootstrap().connect(address).addListener(
                    (ChannelFutureListener) promise -> {
                        if(promise.isDone()){
                            channelFuture.complete(promise.channel());
                            if(log.isDebugEnabled()){
                                log.debug("与服务端{}建立连接成功",address);
                            }
                        }else if(!promise.isSuccess()){
                            channelFuture.completeExceptionally(promise.cause());
                        }
                    }
            );
            //阻塞获取channel
            try {
                channel = channelFuture.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取channel异常",e);
                throw new RuntimeException(e);
            }


            //缓存channel
            DynamicBootstrap.CHANNEL_CACHE.put(address,channel);
        }
        if(channel == null){
            throw new NetworkException("获取channel失败");
        }
        return channel;
    }

}
