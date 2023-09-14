package org.dynamic.rpc.serviceImpl;

import org.dynamic.rpc.HelloService;

/**
 * @author: DynamicYang
 * @create: 2023-09-14 10:25
 * @Description:
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
