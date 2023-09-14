package org.dynamic.rpc;

import java.lang.reflect.Proxy;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class ReferenceConfig<T> {
    private Class<T> serviceInterface;

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }



    public void setInterface(Class<T> clazz) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * @Author DynamicYang
     * @Description: 动态代理获取接口实现
     * @Date  2023/9/14
     * @Param
     * @return T
     **/
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] cLasses = new Class[]{serviceInterface};

        Object serviceProxy =  Proxy.newProxyInstance(classLoader, cLasses, (proxy, method, args) -> {
            System.out.println("获取到实例");
            return null;
        });

        return (T) serviceProxy;

    }
}
