package org.dynamic.rpc.serviceImpl;

import org.dynamic.rpc.HelloService;
import org.dynamic.rpc.annotation.RpcAPI;

/**
 * @author: DynamicYang
 * @create: 2023-09-14 10:25
 * @Description:
 */
@RpcAPI(group = "primary")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
