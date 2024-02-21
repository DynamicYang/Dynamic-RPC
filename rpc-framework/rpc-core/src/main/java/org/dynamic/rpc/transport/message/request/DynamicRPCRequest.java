package org.dynamic.rpc.transport.message.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicRPCRequest  {

    //请求的ID
    private long requestId;

    private byte requestType;

    private byte compressType;

    private byte serializationType;

    private Payload payload;
//
//    public long getRequestId() {
//        return requestId;
//    }
//
//    public void setRequestId(long requestId) {
//        this.requestId = requestId;
//    }
//
//    public byte getRequestType() {
//        return requestType;
//    }
//
//    public void setRequestType(byte requestType) {
//        this.requestType = requestType;
//    }
//
//    public byte getCompressType() {
//        return compressType;
//    }
//
//    public void setCompressType(byte compressType) {
//        this.compressType = compressType;
//    }
//
//    public byte getSerializationType() {
//        return serializationType;
//    }
//
//    public void setSerializationType(byte serializationType) {
//        this.serializationType = serializationType;
//    }
//
//    public Payload getPayload() {
//        return payload;
//    }
//
//    public void setPayload(Payload payload) {
//        this.payload = payload;
//    }
//
//    public DynamicRPCRequest(long requestId, byte requestType, byte compressType, byte serializationType, Payload payload) {
//        this.requestId = requestId;
//        this.requestType = requestType;
//        this.compressType = compressType;
//        this.serializationType = serializationType;
//        this.payload = payload;
//    }
//
//    public DynamicRPCRequest() {
//
//    }


}
