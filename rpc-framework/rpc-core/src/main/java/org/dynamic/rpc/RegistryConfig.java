package org.dynamic.rpc;

import org.dynamic.rpc.discovery.Impl.ZookeeperRegistry;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.Constant;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class RegistryConfig {
    //连接的url
    String connectString;
    String ip;
    String port;

    public RegistryConfig(String connectString) {
        this.connectString = connectString;
    }
    public RegistryConfig(){
        this.connectString = Constant.DEFAULT_CONNECTSTRING;
    }

    /**
     * @Author DynamicYang
     * @Description: 先使用简单工厂
     * @Date 2023/9/15
     * @Param
     * @return org.dynamic.rpc.discovery.Registry
     **/
    public Registry getRegistry() {
        String registryType = getRegistryType();
        if (this.connectString == null){
            return new ZookeeperRegistry();
        }
        if (registryType.toLowerCase().trim().equals("zookeeper")){
            String host = connectString.split("://")[1];
            return new ZookeeperRegistry(host,Constant.DEFAULT_TIMEOUT);
        }
        return null;
    }

    /**
     * @Author DynamicYang
     * @Description: 拿到注册中心的类型
     * @Date 2023/9/15
     * @Param
     * @return java.lang.String
     **/
    public String getRegistryType() {
        String[] typeAndHost = connectString.split("://");
        if(typeAndHost.length != 2){
            throw new RuntimeException("给定的注册中心连接url不合法");
        }
        return typeAndHost[0];
    }
}
