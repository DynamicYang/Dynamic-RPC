package org.dynamic.rpc.enumration;

public enum ResponseCode {

    SUCCESS((byte)0,"success"),
    FAIL((byte)1,"fail");

    private byte code;
    private String desc;

    ResponseCode(byte id,String desc){
        this.code = id;
        this.desc = desc;
    }
    public byte getCode(){
        return code;
    }
    public String getDesc(){
        return desc;
    }
}
