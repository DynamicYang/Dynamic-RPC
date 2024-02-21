package org.dynamic.rpc.exception;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
public class LoadBalancerException extends RuntimeException {

    public LoadBalancerException(String message){
        super(message);
    }

    public LoadBalancerException(Throwable cause){
        super(cause);
    }
}
