package org.dynamic.rpc.core;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author: DynamicYang
 * @create: 2024-03-08
 * @Description:
 */
public class ShutDownHolder {

    // 用来标记请求挡板
    public static AtomicBoolean BAFFLE = new AtomicBoolean(false);

    // 用于请求的计数器
    public static LongAdder REQUEST_COUNTER = new LongAdder();
}
