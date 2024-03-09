package org.dynamic.rpc.springbootproviderdemo;

import org.apache.dubbo.config.annotation.Service;
import org.dynamic.rpc.HelloService;
import org.dynamic.rpc.annotation.RpcAPI;
import org.springframework.stereotype.Component;

/**
 * @author: DynamicYang
 * @create: 2024-03-08
 * @Description:
 */
@Service
@Component
@RpcAPI(group = "primary")
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
