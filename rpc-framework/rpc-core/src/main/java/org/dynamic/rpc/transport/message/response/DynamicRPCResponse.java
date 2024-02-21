package org.dynamic.rpc.transport.message.response;

/**
 * @author: DynamicYang
 * @create: 2024-02-19
 * @Description: 响应体
 */
public class DynamicRPCResponse {

    private long requestId;


    private int code;

    private Object body;

    private byte compressType;

    private byte serializationType;

    private byte requestType;

    public long getRequestId() {
        return requestId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public int getCode() {
        return code;
    }

    public byte getRequestType() {
        return requestType;
    }

    public void setRequestType(byte requestType) {
        this.requestType = requestType;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public byte getCompressType() {
        return compressType;
    }

    public void setCompressType(byte compressType) {
        this.compressType = compressType;
    }

    public byte getSerializationType() {
        return serializationType;
    }

    public void setSerializationType(byte serializationType) {
        this.serializationType = serializationType;
    }

    public DynamicRPCResponse(long requestId, int code, Object body, byte compressType, byte serializationType, byte requestType) {
        this.requestId = requestId;
        this.code = code;
        this.body = body;
        this.compressType = compressType;
        this.serializationType = serializationType;
        this.requestType = requestType;
    }

    public DynamicRPCResponse() {

    }
}

