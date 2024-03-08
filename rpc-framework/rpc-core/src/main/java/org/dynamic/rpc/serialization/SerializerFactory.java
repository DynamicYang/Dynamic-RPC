package org.dynamic.rpc.serialization;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.serialization.Impl.JDKSerializer;
import org.dynamic.rpc.serialization.Impl.JsonSerializer;

import java.util.List;
import java.util.Set;
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

    public static < T extends Serializer> void addSerializerIfAbsent(   T serializer){
        String type = serializer.getClass().getSimpleName().replace("Serializer","");
        for(String s : SERIALIZER_CACHE.keySet()){
            if(s.toLowerCase().equals(type.toLowerCase())){
                log.info("已加载序列化方式：{}，无需重复加载",type);
                return;
            }
        }
        byte code = (byte) (SERIALIZER_CACHE.size()+1);
        SerializerWrapper wrapper = new SerializerWrapper(code,type,serializer);
        SERIALIZER_CACHE.put(type,wrapper);
        TYPE_CODE_CACHE.put(code,wrapper);
    }
    public static < T extends Serializer> void addSerializerIfAbsent(   List<T> serializers) {
        for (T serializer : serializers) {
            addSerializerIfAbsent(serializer);
        }
    }
}
