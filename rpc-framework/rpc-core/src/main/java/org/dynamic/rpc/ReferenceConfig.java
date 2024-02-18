package org.dynamic.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.exception.NetworkException;
import org.dynamic.rpc.proxy.handler.ConsumerInvocationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;


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
        Class<T>[] cLasses = new Class[]{serviceInterface};

        InvocationHandler handler = new ConsumerInvocationHandler(registryConfig.getRegistry(),serviceInterface);

        Object serviceProxy = Proxy.newProxyInstance(classLoader, cLasses, handler);
        return (T) serviceProxy ;

    }
}
