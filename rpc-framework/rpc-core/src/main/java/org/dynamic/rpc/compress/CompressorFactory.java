package org.dynamic.rpc.compress;

import io.netty.handler.logging.LogLevel;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.compress.Impl.GzipCompressor;
import org.dynamic.rpc.serialization.Serializer;
import org.dynamic.rpc.serialization.SerializerWrapper;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
@Slf4j
public class CompressorFactory {
    private final static ConcurrentHashMap<String, CompressorWrapper> COMPRESSOR_CACHE = new ConcurrentHashMap(8);
    private final static ConcurrentHashMap<Byte, CompressorWrapper> TYPE_CODE_CACHE = new ConcurrentHashMap<Byte, CompressorWrapper>(8);


    static{
        COMPRESSOR_CACHE.put("gzip",new CompressorWrapper((byte)1,"gzip",new GzipCompressor()));
        TYPE_CODE_CACHE.put((byte)1,COMPRESSOR_CACHE.get("gzip"));

    }

    //工厂方法获取一个序列化器
    public static CompressorWrapper getCompressorWrapper(String type){
        CompressorWrapper wrapper = COMPRESSOR_CACHE.get(type);
        if(wrapper == null){
            if(log.isDebugEnabled()){
                log.debug("暂不支持的压缩方式：{}，将使用默认压缩方式", type);
            }
            return COMPRESSOR_CACHE.get("gzip");
        }
        return COMPRESSOR_CACHE.get(type);
    }

    public static CompressorWrapper getCompressorWrapper(byte code){
        return TYPE_CODE_CACHE.get(code);

    }

    public static <T extends Compressor> void addCompressorIfAbsent( T compressor){
        String type = compressor.getClass().getSimpleName().replace("Compressor","");
        byte code = (byte) (COMPRESSOR_CACHE.size()+1);
        for(String s : COMPRESSOR_CACHE.keySet()){
            if(s.toLowerCase().equals(type.toLowerCase())){
                log.info("已加载序列化方式：{}，无需重复加载",type);
                return;
            }
        }

        CompressorWrapper wrapper = new CompressorWrapper(code,type,compressor);
        COMPRESSOR_CACHE.put(type,wrapper);
        TYPE_CODE_CACHE.put(code,wrapper);
    }

    public static <T extends Compressor> void addCompressorIfAbsent( List<T> compressor){
        for(T c : compressor){
            addCompressorIfAbsent(c);
        }
    }

}
