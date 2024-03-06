package org.dynamic.rpc.loadbalancer.impl;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.exception.LoadBalancerException;
import org.dynamic.rpc.loadbalancer.AbstractLoadBalancer;
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
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
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


    }
}
