package org.dynamic.rpc.serialization.Impl;

import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.serialization.Serializer;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug("对象使用json【{}】序列化成功】", obj);
        }
        return JSON.toJSONBytes(obj);
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }
        if(log.isDebugEnabled()) {
            log.debug("类使用json【{}】反序列化成功】", clazz);
        }
        return JSON.parseObject(bytes, clazz);
    }
}
