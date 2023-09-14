package org.dynamic.rpc;

import org.dynamic.rpc.serviceImpl.HelloServiceImpl;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class Application {
    public static void main(String[] args) {

        ServiceConfig<HelloService> service = new ServiceConfig<>();
        service.setInterface(HelloService.class);
        service.setRef(new HelloServiceImpl());


        DynamicBootstrap.getInstance().
                application("demo")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig())
                .publish(service)
                .start();


    }
}
