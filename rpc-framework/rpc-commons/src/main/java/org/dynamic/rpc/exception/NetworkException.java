package org.dynamic.rpc.exception;

import java.net.SocketException;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class NetworkException extends RuntimeException {


    public NetworkException(String msg) {
            super(msg);
    }

    public NetworkException(Throwable cause) {
        cause.printStackTrace();
    }
}
