package org.dynamic.rpc.proxy;

import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.ReferenceConfig;
import org.dynamic.rpc.RegistryConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2024-03-08
 * @Description:
 */
public class ProxyFactory {
    private static Map<Class<?>,Object> cache = new ConcurrentHashMap<>(32);

    public static <T> T getProxy(Class<T> clazz) {

        Object bean = cache.get(clazz);
        if(bean != null){
            return (T)bean;
        }

        ReferenceConfig<T> reference = new ReferenceConfig<>();
        reference.setInterface(clazz);

        // 代理做了些什么?
        // 1、连接注册中心
        // 2、拉取服务列表
        // 3、选择一个服务并建立连接
        // 4、发送请求，携带一些信息（接口名，参数列表，方法的名字），获得结果
        DynamicBootstrap.getInstance()
                .application("first-rpc-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2199"))
                .serialize("hessian")
                .compress("gzip")
                .group("primary")
                .reference(reference);
        T t = reference.get();
        cache.put(clazz,t);
        return t;
    }
}
