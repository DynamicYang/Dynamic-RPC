package org.dynamic.rpc.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
public interface LoadBalancer {

    /**
     * @Author DynamicYang
     * @Description:根据服务名得到一个可用的服务
     * @Param
     * @return java.net.InetSocketAddress
     **/
    InetSocketAddress select(String serviceName);


    void reBalance(String serviceName,List<InetSocketAddress> addressList);
}
