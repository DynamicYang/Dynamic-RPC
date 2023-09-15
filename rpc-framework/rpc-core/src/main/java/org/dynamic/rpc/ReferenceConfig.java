package org.dynamic.rpc;

import org.dynamic.rpc.discovery.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;


/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class ReferenceConfig<T> {
    private static final Logger log = LoggerFactory.getLogger(ReferenceConfig.class);
    private Class<T> serviceInterface;
    private RegistryConfig registryConfig;

    public RegistryConfig getRegistryConfig() {
        return registryConfig;
    }
    public void setRegistryConfig(RegistryConfig registryConfig) {
        this.registryConfig = registryConfig;
    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }



    public void setInterface(Class<T> clazz) {
        this.serviceInterface = clazz;
    }

    /**
     * @Author DynamicYang
     * @Description: 动态代理获取服务接口代理
     * @Date  2023/9/14
     * @Param
     * @return T
     **/
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] cLasses = new Class[]{serviceInterface};

        Object serviceProxy =  Proxy.newProxyInstance(classLoader, cLasses, (proxy, method, args) -> {
            log.info("method: " + method.getName());
            log.info("args" + args);
            Registry registry = registryConfig.getRegistry();

            //todo q:每次调用远程服务都要去远程拉取服务列表吗？
            InetSocketAddress address = registry.lookup(serviceInterface.getName());
            if (log.isDebugEnabled()){
                log.debug("服务调用方发现了，服务【{}】可用主机【{}】",serviceInterface.getName(),address.getAddress());
            }

            return null;
        });

        return (T) serviceProxy  ;

    }
}
