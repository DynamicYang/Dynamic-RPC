package org.dynamic.rpc.discovery;

import org.dynamic.rpc.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * @author: DynamicYang
 * @create: 2023-09-15
 * @Description:抽象的注册中心
 */
public interface Registry {

    /**
     * @Author DynamicYang
     * @Description: 服务的配置内容
     * @Date  2023/9/15
     * @Param
     * @return void
     **/
    void register(ServiceConfig<?> serviceConfig);
    /**
     * @Author DynamicYang
     * @Description: 服务下线
     * @Date  2023/9/15
     * @Param
     * @return void
     **/
    void serviceDown(ServiceConfig<?> serviceConfig);

    /**
     * @Author DynamicYang
     * @Description: 从注册中心寻找一个可用的服务
     * @Date 2023/9/15
     * @Param
     * @return java.net.InetSocketAddress
     **/

    InetSocketAddress lookup(String serviceName);
}
