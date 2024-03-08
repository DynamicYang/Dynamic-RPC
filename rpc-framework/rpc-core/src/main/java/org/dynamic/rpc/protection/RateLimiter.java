package org.dynamic.rpc.protection;

/**
 * @author: DynamicYang
 * @create: 2024-03-08
 * @Description:
 */
public interface RateLimiter {

    boolean allowRequest();

}
