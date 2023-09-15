package org.dynamic.rpc;


import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.dynamic.rpc.discovery.Impl.ZookeeperRegistry;
import org.dynamic.rpc.discovery.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class DynamicBootstrap {
    private static final Logger log = LoggerFactory.getLogger(DynamicBootstrap.class);
    private static final DynamicBootstrap instance = new DynamicBootstrap();

    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private ServiceConfig<?> serviceConfig;
    private ReferenceConfig<?> referenceConfig;
    private Registry registryCenter;
    //维护已经发布且暴露的服务列表 key->interface的全限定名  value是定义好的serviceConfig
    private static final  Map<String,ServiceConfig<?>> SERVICES_COLLECTION = new HashMap<>(16);




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
        try {
            Thread.sleep(1000000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
