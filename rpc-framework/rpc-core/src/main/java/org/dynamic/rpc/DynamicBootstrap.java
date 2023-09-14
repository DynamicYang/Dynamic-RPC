package org.dynamic.rpc;


import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class DynamicBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(DynamicBootstrap.class);
    private static final DynamicBootstrap instance = new DynamicBootstrap();

    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    private ServiceConfig<?> serviceConfig;
    private ReferenceConfig<?> referenceConfig;

    private ZooKeeper zooKeeper;
    private DynamicBootstrap(){}

    public static DynamicBootstrap getInstance(){
        return instance;
    }


    public DynamicBootstrap application(String appName){
        return this;
    }

    public DynamicBootstrap registry(RegistryConfig registryConfig){
        //先耦合zookeeper
        //TODO 支持其他注册中心
        zooKeeper = ZookeeperUtils.createZookeeper();
        this.registryConfig = registryConfig;
        return this;
    }

    public DynamicBootstrap protocol(ProtocolConfig protocolConfig){
        this.protocolConfig = protocolConfig;
        if (logger.isDebugEnabled()){
            logger.debug("当前使用了{}的协议进行序列化", protocolConfig);
        }
        return this;
    }


    public DynamicBootstrap publish(ServiceConfig<?> serviceConfig){
        this.serviceConfig = serviceConfig;


        if(logger.isDebugEnabled()){
            logger.debug("服务{}已经被注册", serviceConfig.getServiceInterface().getName());
        }
        return this;
    }

    public void reference(ReferenceConfig<?> referenceConfig){
        this.referenceConfig = referenceConfig;
    }

    public void start(){

    }


}
