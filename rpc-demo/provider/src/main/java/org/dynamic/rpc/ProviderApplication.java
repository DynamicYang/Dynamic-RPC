package org.dynamic.rpc;

import org.dynamic.rpc.serviceImpl.HelloServiceImpl;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class ProviderApplication {
    public static void main(String[] args) {

        ServiceConfig<HelloService> service = new ServiceConfig<>();
        service.setInterface(HelloService.class);
        service.setRef(new HelloServiceImpl());


        DynamicBootstrap.getInstance().
                application("demo")
                .registry(new RegistryConfig("zookeeper://172.29.207.114"))
//                .registry(new RegistryConfig())
                .protocol(new ProtocolConfig("jdk"))
//                .publish(service)
                .scan("org.dynamic.rpc")
                .start();


    }
}

