package org.dynamic.rpc.serialization.Impl;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.exception.SerializeException;
import org.dynamic.rpc.serialization.Serializer;

import java.io.*;

/**
 * @author: DynamicYang
 * @create: 2024-02-20
 * @Description:
 */
@Slf4j
public class JDKSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            log.error("在序列化对象【{}】时发生异常", obj);
            throw new SerializeException(e);
        }

    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }
        try(ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(bytes))){
            return clazz.cast(objectInputStream.readObject());
        }catch (IOException | ClassNotFoundException e) {
            log.error("在反序列化对象【{}】时发生异常", clazz);
            throw new SerializeException(e);
        }
    }
}
