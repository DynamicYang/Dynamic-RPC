package org.dynamic.rpc.loadbalancer;

import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.loadbalancer.impl.RoundRobinLoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2024-02-22
 * @Description:
 */
public abstract class AbstractLoadBalancer  implements LoadBalancer{




    private Map<String,Selector> cache = new ConcurrentHashMap<>(8);




    @Override
    public InetSocketAddress select(String serviceName) {
        //优先从缓存中获取一个选择器
        Selector selector = cache.get(serviceName);

        if (selector == null) {

            //对于这个负载均衡器内部应该维护一个服务列表作为缓存
            List<InetSocketAddress> serviceList = DynamicBootstrap.getInstance().getRegistry().lookup(serviceName);


            //提供一些算法负责选取适合的结点
            selector = getSelector(serviceList);

            //将选择器放入缓存中
            cache.put(serviceName,selector);

        }
        return selector.getNext();

    }

    /**
     * @Author DynamicYang
     * @Description:输入服务列表返回一个负载均衡算法的选择器
     * @Param
     * @return org.dynamic.rpc.loadbalancer.Selector
     **/
    protected  abstract  Selector getSelector(List<InetSocketAddress> serviceList);

    @Override
    public synchronized void reBalance(String serviceName,List<InetSocketAddress> addressList) {
        cache.put(serviceName,getSelector(addressList));
    }
}
