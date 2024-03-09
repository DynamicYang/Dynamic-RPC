package org.dynamic.rpc;

import org.dynamic.rpc.annotation.TryTimes;

public interface HelloService {

    @TryTimes(tryTimes = 3, intervalTime = 4000)
    String sayHello(String name);
}
