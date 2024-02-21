package org.dynamic.rpc.exception;

/**
 * @author: DynamicYang
 * @create: 2024-02-20
 * @Description:
 */
public class SerializeException extends RuntimeException{
    public SerializeException(String msg) {
        super(msg);
    }

    public SerializeException(Throwable cause) {
        cause.printStackTrace();
    }
}
