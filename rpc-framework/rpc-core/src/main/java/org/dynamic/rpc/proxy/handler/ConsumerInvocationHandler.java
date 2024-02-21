package org.dynamic.rpc.proxy.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.IDGenerator;
import org.dynamic.rpc.NettyBootstrapInitializer;
import org.dynamic.rpc.compress.CompressorFactory;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.exception.NetworkException;
import org.dynamic.rpc.serialization.SerializerFactory;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.request.Payload;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
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

    private Registry registry;
    private Class<?> serviceInterface;

    public ConsumerInvocationHandler(Registry registry, Class<?> serviceInterface){
        this.registry = registry;
        this.serviceInterface = serviceInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws ExecutionException, InterruptedException, TimeoutException {
        log.info("method: " + method.getName());
        log.info("args" + args);



        //todo q:每次调用远程服务都要去远程拉取服务列表吗？
        //todo 实现服务调用的负载均衡


        InetSocketAddress address = DynamicBootstrap.LOAD_BALANCER.select(serviceInterface.getName());


        if (log.isDebugEnabled()){
            log.debug("服务调用方发现了，服务【{}】可用主机【{}】",serviceInterface.getName(),address.getAddress());
        }

        Channel channel = getAvailableChannel(address);


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


        DynamicRPCRequest rpcRequest = DynamicRPCRequest.builder()
                        .requestId(DynamicBootstrap.ID_GENERATOR.getId())
                        .compressType(CompressorFactory.getCompressorWrapper(DynamicBootstrap.COMPRESS_TYPE).getCode())
                        .requestType(RequestType.REQUEST.getId())
                        .serializationType(SerializerFactory.getSerializerWrapper(DynamicBootstrap.SERIALIZE_TYPE).getCode())
                        .payload(payload).build();
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
        DynamicBootstrap.PENDING_REQUEST.put(1L,completableFuture);
// *info promise
        channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise->{

            if(!promise.isSuccess()){
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
        Object result = completableFuture.get(10,TimeUnit.SECONDS);
        return result;
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
