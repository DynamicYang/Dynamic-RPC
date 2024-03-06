package org.dynamic.rpc.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
public interface Selector {
    /**
     * @Author DynamicYang
     * @Description: 从服务列表中获得一个可用的服务结点
     * @Param
     * @return java.net.InetSocketAddress
     **/
    InetSocketAddress getNext();


}
