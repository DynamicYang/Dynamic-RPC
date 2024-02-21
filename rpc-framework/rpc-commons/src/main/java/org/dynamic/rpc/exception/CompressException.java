package org.dynamic.rpc.exception;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
public class CompressException extends RuntimeException {


    public CompressException(String msg) {
            super(msg);
        }

    public CompressException(Throwable cause) {
            cause.printStackTrace();
        }
}
