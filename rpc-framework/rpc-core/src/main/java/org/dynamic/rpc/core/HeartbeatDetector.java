package org.dynamic.rpc.core;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.NettyBootstrapInitializer;
import org.dynamic.rpc.compress.CompressorFactory;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.serialization.SerializerFactory;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author: DynamicYang
 * @create: 2024-02-26
 * @Description:
 */
@Slf4j
public class HeartbeatDetector {
    public static void detectHeartbeat(String serviceName) {

        // 从注册中心拉取服务列表并建立连接
        Registry registry = DynamicBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addressList = registry.lookup(serviceName);

        //将连接进行缓存
        for(InetSocketAddress address : addressList){
            try {
                if(!DynamicBootstrap.CHANNEL_CACHE.containsKey(address)) {
                    Channel channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    DynamicBootstrap.CHANNEL_CACHE.put(address, channel);
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }

        //任务，定期发送心跳
        Thread thread = new Thread(()->
            new Timer().scheduleAtFixedRate(new MyTimerTask(),0,2000),"rpc-heartbeatDetector-thread");
        thread.setDaemon(true);
        thread.start();



    }

    private static class MyTimerTask extends TimerTask {

        @Override
        public void run() {

            //将响应时常的MAP清空
            DynamicBootstrap.RESPONSE_TIME_CHANNEL_CACHE.clear();


            Map<InetSocketAddress,Channel> cache = DynamicBootstrap.CHANNEL_CACHE;
            for(Map.Entry<InetSocketAddress,Channel> entry : cache.entrySet()) {

                int tryTime = 3;
                while (tryTime > 0) {
                Channel channel = entry.getValue();

                long start = System.currentTimeMillis();

                //构建心跳请求
                DynamicRPCRequest rpcRequest = DynamicRPCRequest.builder()
                        .requestId(DynamicBootstrap.ID_GENERATOR.getId())
                        .compressType(CompressorFactory.getCompressorWrapper(DynamicBootstrap.COMPRESS_TYPE).getCode())
                        .requestType(RequestType.HEART_BEAT.getId())
                        .serializationType(SerializerFactory.getSerializerWrapper(DynamicBootstrap.SERIALIZE_TYPE).getCode())
                        .timeStamp(start)
                        .build();
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();

                DynamicBootstrap.PENDING_REQUEST.put(rpcRequest.getRequestId(), completableFuture);

                channel.writeAndFlush(rpcRequest).addListener((ChannelFutureListener) promise -> {

                    if (!promise.isSuccess()) {
                        completableFuture.completeExceptionally(promise.cause());
                    }
                });
                Long endTime = 0L;
                try {
                    completableFuture.get(1, TimeUnit.SECONDS);

                    endTime = System.currentTimeMillis();

                    Long time = endTime - start;

                    DynamicBootstrap.RESPONSE_TIME_CHANNEL_CACHE.put(time, channel);


                    log.debug("与服务端【{}】心跳检测时间：{}ms", channel.remoteAddress(), time);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                } catch (TimeoutException e) {
                    tryTime--;
                    log.error("与服务端【{}】心跳检测失败，重试次数：{}", channel.remoteAddress(), tryTime);
                    if(tryTime == 0) {
                        //移除失效的地址
                        DynamicBootstrap.CHANNEL_CACHE.remove(entry.getKey());
                    }


                    try {
                        Thread.sleep(10*new Random().nextInt(5));
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }

                }

            }
            }

        }
    }
}
