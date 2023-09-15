package org.dynamic.rpc.exception;

import java.net.SocketException;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class NetworkException extends RuntimeException {


    public NetworkException() {

    }

    public NetworkException(Throwable cause) {
        cause.printStackTrace();
    }
}
