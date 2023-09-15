package org.dynamic.rpc.exception;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class ZooKeeperException extends RuntimeException{
    public ZooKeeperException(Throwable cause){
        cause.printStackTrace();
    }
    public ZooKeeperException(){}
}
