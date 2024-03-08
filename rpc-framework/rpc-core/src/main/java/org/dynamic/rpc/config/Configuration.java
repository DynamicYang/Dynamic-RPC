package org.dynamic.rpc.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.*;
import org.dynamic.rpc.compress.Compressor;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.loadbalancer.impl.RoundRobinLoadBalancer;
import org.dynamic.rpc.protection.CircuitBreaker;
import org.dynamic.rpc.protection.RateLimiter;
import org.dynamic.rpc.serialization.Serializer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2024-03-06
 *  @Description:全局配置类
 * xml配置，代码配置，spi配置，默认项由启动程序指定
 */
@Data
@Slf4j
public class Configuration {



    //端口号
    private  int port = 8088;

    private  String appName = "default";

    private String group = "default";

    private   LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    //ID生成器
    private IDGenerator idGenerator = new IDGenerator(1,2);

    //注册中心配置
    private RegistryConfig registryConfig;

    //协议配置
    private ProtocolConfig protocolConfig;

    //服务配置
    private ServiceConfig<?> serviceConfig;

    private ReferenceConfig<?> referenceConfig;

    private Registry registryCenter;

    private  String serializerType = "JDK";

    private  String compressorType = "gzip";

    private  Serializer serializer;

    private Compressor compressor;

    // 为每一个ip配置一个限流器
    private final Map<SocketAddress, RateLimiter> everyIpRateLimiter = new ConcurrentHashMap<>(16);


    // 为每一个ip配置一个断路器，熔断
    private final Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = new ConcurrentHashMap<>(16);


    public Configuration(){
        //1,首先加载默认配置项



        XMLResolver.loadByXML(this);
    }



}
