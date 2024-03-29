package org.dynamic.rpc.enumration;

public enum ResponseCode {

    SUCCESS((byte) 20,"成功"),
    SUCCESS_HEART_BEAT((byte) 21,"心跳检测成功返回"),
    RATE_LIMIT((byte)31,"服务被限流" ),
    RESOURCE_NOT_FOUND((byte)44,"请求的资源不存在" ),
    FAIL((byte)50,"调用方法发生异常"),
    BECOLSING((byte)51,"调用方法发生异常");

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
