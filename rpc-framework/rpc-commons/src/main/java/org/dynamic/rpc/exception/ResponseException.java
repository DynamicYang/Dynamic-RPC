package org.dynamic.rpc.exception;

/**
 * @author: DynamicYang
 * @create: 2024-03-08
 * @Description:
 */
public class ResponseException extends RuntimeException {
    private int code;
    private String desc;
    public ResponseException(int code, String desc) {
        super(desc);
        this.code = code;
        this.desc = desc;

    }
}
