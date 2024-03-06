package org.dynamic.rpc.loadbalancer.impl;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.exception.LoadBalancerException;
import org.dynamic.rpc.loadbalancer.AbstractLoadBalancer;
import org.dynamic.rpc.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: DynamicYang
 * @create: 2024-02-26
 * @Description:
 */
@Slf4j
public class MinimumResponseTimeLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new MinimumResponseTimeLoadBalancer.MinimumResponseTimeSelector(serviceList);
    }

    private static class MinimumResponseTimeSelector implements Selector{




        public MinimumResponseTimeSelector(List<InetSocketAddress> serviceList){

        }

        @Override
        public InetSocketAddress getNext() {
            Map.Entry<Long, Channel> entry = DynamicBootstrap.RESPONSE_TIME_CHANNEL_CACHE.firstEntry();
            if(entry != null){
                return (InetSocketAddress) entry.getValue().remoteAddress();
            }
            Channel channel = (Channel) DynamicBootstrap.CHANNEL_CACHE.values().toArray()[0];
            return (InetSocketAddress) channel.remoteAddress();

        }


    }

}
