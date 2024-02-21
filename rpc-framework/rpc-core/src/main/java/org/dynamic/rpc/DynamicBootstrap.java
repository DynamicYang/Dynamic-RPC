package org.dynamic.rpc;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.dynamic.rpc.channel.handler.inbound.DynamicRPCRequestDecoder;
import org.dynamic.rpc.channel.handler.inbound.DynamicRPCResponseEncoder;
import org.dynamic.rpc.channel.handler.inbound.MethodCallHandler;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class DynamicBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DynamicBootstrap.class);
    private static final DynamicBootstrap instance = new DynamicBootstrap();

    public static  LoadBalancer LOAD_BALANCER;

    public static int PORT = 8088;
    public static final IDGenerator ID_GENERATOR = new IDGenerator(1,2);

    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private ServiceConfig<?> serviceConfig;
    private ReferenceConfig<?> referenceConfig;
    private Registry registryCenter;
    //维护已经发布且暴露的服务列表 key->interface的全限定名  value是定义好的serviceConfig
    public static final  Map<String,ServiceConfig<?>> SERVICES_COLLECTION = new HashMap<>(16);
    //连接的缓存 如果使用InetSocketAddress作为key,一定要看他有没有重写equals方法
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);

    //定义全局的对外挂起的completableFuture,key为标识
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);

    public static String SERIALIZE_TYPE = "JDK";

    public static String  COMPRESS_TYPE = "gzip";





    private DynamicBootstrap(){}

    public static DynamicBootstrap getInstance(){
        return instance;
    }


    public DynamicBootstrap application(String appName){
        return this;
    }

    public DynamicBootstrap registry(RegistryConfig registryConfig){


        //使用工厂来获取注册中心
        this.registryConfig = registryConfig;
        registryCenter = registryConfig.getRegistry();
        LOAD_BALANCER = new RoundRobinLoadBalancer();
        return this;
    }

    public DynamicBootstrap protocol(ProtocolConfig protocolConfig){
        this.protocolConfig = protocolConfig;
        if (log.isDebugEnabled()){
            log.debug("当前使用了{}的协议进行序列化", protocolConfig);
        }
        return this;
    }


    public DynamicBootstrap publish(ServiceConfig<?> serviceConfig){


        SERVICES_COLLECTION.put(serviceConfig.getServiceInterface().getName(), serviceConfig);
        registryCenter.register(serviceConfig);
        return this;
    }

    public void reference(ReferenceConfig<?> referenceConfig){
      referenceConfig.setRegistryConfig(this.registryConfig);

    }

    public void start(){
        //创建bossGroup
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(2);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(10);
        ServerBootstrap bootstrap = new ServerBootstrap();//用于启动nio服务
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)//通过工厂方法设计模式实例化一个channel
                    .localAddress(new InetSocketAddress(PORT))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline()
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new DynamicRPCRequestDecoder())
                                    .addLast(new MethodCallHandler())
                                    .addLast(new DynamicRPCResponseEncoder());

                        }
                    });
            //绑定服务器，该实例将提供有关IO操作的结果或者状态的信息
            ChannelFuture channelFuture = bootstrap.bind(PORT).sync();
            if(log.isDebugEnabled()){
               log.debug("消息发送成功");
           }
            //阻塞操作，closeFuture()开启了一个channel的监听器(这期间channel在进行各项工作),知道链路断开
            //closeFuture().sync();会阻塞点当前线程，知道通道关闭操作完成。这可以用于确保在关闭通道之前，程序不会提前退出
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                bossGroup.shutdownGracefully().sync();
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }




    }

    public Registry getRegistry(){
        return registryCenter;
    }

    public DynamicBootstrap serialize(String serializeType) {
        SERIALIZE_TYPE = serializeType;
        if (log.isDebugEnabled()){
            log.debug("当前使用了{}的序列化方式", serializeType);
        }
        return this;
    }

    public DynamicBootstrap compressor(String type) {
        COMPRESS_TYPE = type;
        if (log.isDebugEnabled()){
            log.debug("当前使用了{}的压缩解压方式", type);
        }
        return this;
    }
}
