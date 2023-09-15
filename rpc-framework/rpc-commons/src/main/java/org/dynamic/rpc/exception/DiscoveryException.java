package org.dynamic.rpc.exception;

/**
 * @author: DynamicYang
 * @create: 2023-09-15
 * @Description:
 */
public class DiscoveryException extends RuntimeException{
    public DiscoveryException(Throwable throwable){
        super(throwable);
    }
    public DiscoveryException(String message){
        super(message);
    }
}
