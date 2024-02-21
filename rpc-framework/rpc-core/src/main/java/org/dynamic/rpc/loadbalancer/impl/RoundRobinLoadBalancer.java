package org.dynamic.rpc.loadbalancer.impl;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.exception.LoadBalancerException;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */

@Slf4j
public class RoundRobinLoadBalancer implements LoadBalancer {

    private Registry registry;

    private Selector selector;

    private Map<String,Selector> cache = new ConcurrentHashMap<>(8);

    public RoundRobinLoadBalancer(){
        this.registry = DynamicBootstrap.getInstance().getRegistry();

    }


    @Override
    public InetSocketAddress select(String serviceName) {
        //优先从缓存中获取一个选择器
        Selector selector = cache.get(serviceName);

        if (selector == null) {

            //对于这个负载均衡器内部应该维护一个服务列表作为缓存
            List<InetSocketAddress> serviceList = registry.lookup(serviceName);


            //提供一些算法负责选取适合的结点
            selector = new RoundRobinSelector(serviceList);

            //将选择器放入缓存中
            cache.put(serviceName,selector);

        }
        return selector.getNext();

    }

    private static class RoundRobinSelector implements Selector{

        private List<InetSocketAddress> serviceList;

        private AtomicInteger index;


        public RoundRobinSelector(List<InetSocketAddress> serviceList){
            this.serviceList = serviceList;
            this.index = new AtomicInteger(0);
        }

        @Override
        public InetSocketAddress getNext() {

            if(serviceList.size() == 0 || serviceList == null){
                log.error("进行负载均衡选取可用服务结点时服务列表为空，此时无法进行负载均衡选取");
                throw new LoadBalancerException("服务列表为空");
            }

            InetSocketAddress address = serviceList.get(index.getAndIncrement() % serviceList.size());
            return address;
        }

        @Override
        public void reBalance(List<InetSocketAddress> serviceList) {

        }
    }
}
