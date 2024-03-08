package org.dynamic.rpc;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.dynamic.rpc.annotation.RpcAPI;
import org.dynamic.rpc.annotation.RpcService;
import org.dynamic.rpc.channel.handler.inbound.DynamicRPCRequestDecoder;
import org.dynamic.rpc.channel.handler.inbound.DynamicRPCResponseEncoder;
import org.dynamic.rpc.channel.handler.inbound.MethodCallHandler;
import org.dynamic.rpc.config.Configuration;
import org.dynamic.rpc.core.HeartbeatDetector;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import org.dynamic.rpc.protection.CircuitBreaker;
import org.dynamic.rpc.protection.RateLimiter;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class DynamicBootstrap {

    private static final Logger log = LoggerFactory.getLogger(DynamicBootstrap.class);
    private static final DynamicBootstrap instance = new DynamicBootstrap();

    public static  LoadBalancer LOAD_BALANCER;

    private Configuration configuration;


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

    public static final TreeMap<Long,Channel> RESPONSE_TIME_CHANNEL_CACHE = new TreeMap<>();

    public static String SERIALIZE_TYPE = "JDK";

    public static String  COMPRESS_TYPE = "gzip";

    public static  ThreadLocal<DynamicRPCRequest> RPC_REQUEST = new ThreadLocal<>();






    private DynamicBootstrap(){

        configuration = new Configuration();
    }

    public static DynamicBootstrap getInstance(){
        return instance;
    }


    public DynamicBootstrap application(String appName){
        configuration.setAppName(appName);
        return this;
    }

    public DynamicBootstrap registry(RegistryConfig registryConfig){

        configuration.setRegistryConfig(registryConfig);

        //使用工厂来获取注册中心
        this.registryConfig = registryConfig;
        registryCenter = registryConfig.getRegistry();
        LOAD_BALANCER = new RoundRobinLoadBalancer();
        return this;
    }

    public DynamicBootstrap loadBalancer(LoadBalancer loadBalancer){
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    public DynamicBootstrap protocol(ProtocolConfig protocolConfig){
        configuration.setProtocolConfig(protocolConfig);

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
    public DynamicBootstrap publish(List<ServiceConfig<?>> serviceConfigs){
        for (ServiceConfig<?> serviceConfig : serviceConfigs) {
            publish(serviceConfig);
        }
        return this;
    }

    public DynamicBootstrap reference(ReferenceConfig<?> referenceConfig){
       //开启对服务的心跳检测
        HeartbeatDetector.detectHeartbeat(referenceConfig.getServiceInterface().getName());
      referenceConfig.setRegistryConfig(this.registryConfig);

      return this;

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
        configuration.setSerializerType(serializeType);
        SERIALIZE_TYPE = serializeType;
        if (log.isDebugEnabled()){
            log.debug("当前使用了{}的序列化方式", serializeType);
        }
        return this;
    }

    public DynamicBootstrap compressor(String type) {
        configuration.setCompressorType(type);
        COMPRESS_TYPE = type;
        if (log.isDebugEnabled()){
            log.debug("当前使用了{}的压缩解压方式", type);
        }
        return this;
    }

    public DynamicBootstrap scan(String packageName) {
        List<String> classNames = getAllClassName(packageName);
        List< Class<?>> classes = classNames.stream().map(className -> {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).filter(clazz -> {
            if (clazz.isAnnotationPresent(RpcAPI.class)) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());

        for(Class<?> clazz:classes) {
            Class<?>[]  interfaceClass = clazz.getInterfaces();
            Object instance = null;
            try {
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            // 获取分组信息
            RpcAPI rpcApi = clazz.getAnnotation(RpcAPI.class);
            String group = rpcApi.group();

            for (Class<?> anInterface : interfaceClass) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfig.setGroup(group);
                if (log.isDebugEnabled()){
                    log.debug("---->已经通过包扫描，将服务【{}】发布.",anInterface);
                }
              publish(serviceConfig);
            }

        }
        return this;
    }

    private List<String> getAllClassName(String packageName) {
        String basePath = packageName.replace("\\.", "/");
        URL resource = ClassLoader.getSystemClassLoader().getResource(basePath);
        if(resource == null){
            throw new RuntimeException("未找到对应的包名");
        }
       String path = resource.getPath();
        List<String> classNames = new ArrayList<>();
        classNames = recursionFile(path,classNames,basePath);
        return classNames;
    }

    private List<String> recursionFile(String path,List<String> classNames,String basePath){
        File file = new File(path);

        if(file.isDirectory()){
            File[] files = file.listFiles();
            if (files == null || files.length == 0){
                return classNames;
            }
            for(File f:files){
                if(f.isDirectory()){
                    recursionFile(f.getPath(),classNames,basePath);
                }else{
                    String fileName = f.getName();
                    String absolutePath = f.getAbsolutePath();

                    if(fileName.endsWith(".class")){
                        String className = getClassNameByAbsolutePath(absolutePath,basePath);
                        classNames.add(className);
                    }
                }
            }
        }
        return classNames;
    }
    private String getClassNameByAbsolutePath(String absolutePath,String basePath) {
        String classPath =  absolutePath.substring(absolutePath.indexOf(basePath.replaceAll("/","\\\\"))).replaceAll("\\\\",".");
        String className = classPath.substring(0,classPath.indexOf(".class"));
        return className;

    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public DynamicBootstrap group(String group){
        configuration.setGroup(group);
        return this;
    }
}
