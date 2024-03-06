package org.dynamic.rpc;

import io.netty.channel.Channel;
import lombok.Data;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.loadbalancer.impl.RoundRobinLoadBalancer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2024-03-06
 *  @Description:全局配置类
 * xml配置，代码配置，spi配置，默认项
 */
@Data
public class Configuration {



    //端口号
    public static int PORT = 8088;

    public  String appName = "default";
    public static  LoadBalancer LOAD_BALANCER = new RoundRobinLoadBalancer();

    //ID生成器
    public static  IDGenerator ID_GENERATOR = new IDGenerator(1,2);

    //注册中心配置
    private RegistryConfig registryConfig;

    //协议配置
    private ProtocolConfig protocolConfig;

    //服务配置
    private ServiceConfig<?> serviceConfig;
    private ReferenceConfig<?> referenceConfig;
    private Registry registryCenter;

    public static String SERIALIZE_TYPE = "JDK";

    public static String  COMPRESS_TYPE = "gzip";




    //读取xml配置
    public Configuration(){

    }

    //spi 配置



}
