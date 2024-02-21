package org.dynamic.rpc.serialization;

public interface Serializer {

    /**
     * 序列化
     * @param obj
     * @return
     */
    byte[] serialize(Object obj);


    /**
     * 反序列化
     * @param
     * @return
     **/
    <T> T deserialize(byte[] bytes, Class<T> clazz);



}
