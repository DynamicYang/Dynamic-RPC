package org.dynamic.rpc.watcher;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.NettyBootstrapInitializer;
import org.dynamic.rpc.discovery.Registry;
import org.dynamic.rpc.loadbalancer.LoadBalancer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author: DynamicYang
 * @create: 2024-03-06
 * @Description:
 */
@Slf4j
public class UpAndDownWatcher implements Watcher {
    @Override
    public void process(WatchedEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("检测到服务【{}】有结点上下线，将重新拉取服务列表...", event.getPath());
        }
        String serviceName = getServiceName(event.getPath());
        Registry registry = DynamicBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addressList = registry.lookup(serviceName);
        for (InetSocketAddress address : addressList) {

            if (!(DynamicBootstrap.CHANNEL_CACHE.containsKey(address))) {
                Channel channel = null;

                try {
                    channel = NettyBootstrapInitializer.getBootstrap().connect(address).sync().channel();
                    DynamicBootstrap.CHANNEL_CACHE.put(address, channel);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        for (Map.Entry<InetSocketAddress, Channel> entry : DynamicBootstrap.CHANNEL_CACHE.entrySet()) {

            if (!addressList.contains(entry.getKey())) {
                DynamicBootstrap.CHANNEL_CACHE.remove(entry.getKey());
            }
        }
        LoadBalancer loadBalancer = DynamicBootstrap.LOAD_BALANCER;
        loadBalancer.reBalance(serviceName,addressList);
    }

    private String getServiceName(String path){

        String[] split = path.split("/");
        return split[split.length-1];
    }
}
