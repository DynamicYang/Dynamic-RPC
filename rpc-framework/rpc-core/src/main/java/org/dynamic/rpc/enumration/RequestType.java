package org.dynamic.rpc.enumration;
/**
 * @Author DynamicYang
 * @Description:用来标记请求类型
 **/
public enum RequestType {
    REQUEST((byte)1,"normal_request"),HEART_BEAT((byte)2,"heart_beat_request");
    private byte id;

    private String type;

    RequestType(byte id,String type){
        this.id = id;
        this.type = type;
    }

    public byte getId(){
        return id;
    }

    public String getType(){
        return type;
    }


}
