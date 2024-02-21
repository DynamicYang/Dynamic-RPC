package org.dynamic.rpc.serialization;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.serialization.Impl.JDKSerializer;
import org.dynamic.rpc.serialization.Impl.JsonSerializer;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
@Slf4j
public class SerializerFactory {
    private final static ConcurrentHashMap<String,SerializerWrapper> SERIALIZER_CACHE = new ConcurrentHashMap(8);
    private final static ConcurrentHashMap<Byte,SerializerWrapper> TYPE_CODE_CACHE = new ConcurrentHashMap<>(8);


    static{
        SERIALIZER_CACHE.put("JDK",new SerializerWrapper((byte)1,"JDK",new JDKSerializer()));
        SERIALIZER_CACHE.put("Json",new SerializerWrapper((byte)2,"Json",new JsonSerializer()));
        SERIALIZER_CACHE.put("Hessian",new SerializerWrapper((byte)3,"Hessian",null));
        TYPE_CODE_CACHE.put((byte)1,SERIALIZER_CACHE.get("JDK"));
        TYPE_CODE_CACHE.put((byte)2,SERIALIZER_CACHE.get("Json"));
        TYPE_CODE_CACHE.put((byte)3,SERIALIZER_CACHE.get("Hessian"));
    }

    //工厂方法获取一个序列化器
    public static SerializerWrapper getSerializerWrapper(String type){
        SerializerWrapper wrapper = SERIALIZER_CACHE.get(type);
        if(wrapper == null){
            if(log.isDebugEnabled()){
                log.debug("暂不支持的序列化方式：{}，将使用默认序列化方式", type);
            }
            return SERIALIZER_CACHE.get("JDK");
        }
        return SERIALIZER_CACHE.get(type);
    }

    public static SerializerWrapper getSerializerWrapper(byte code){
        return TYPE_CODE_CACHE.get(code);

    }
}
