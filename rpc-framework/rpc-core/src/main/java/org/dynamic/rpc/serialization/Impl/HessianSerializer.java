package org.dynamic.rpc.serialization.Impl;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.exception.SerializeException;
import org.dynamic.rpc.serialization.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
@Slf4j
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj)  {
        if(obj == null) {
            return null;
        }
           try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
               Hessian2Output hessian2Output = new Hessian2Output(byteArrayOutputStream);
               hessian2Output.writeObject(obj);
               hessian2Output.flush();

               if (log.isDebugEnabled()) {
                   log.debug("对象使用hessian【{}】序列化成功】", obj);
               }
               return byteArrayOutputStream.toByteArray();
           } catch (IOException e){
               throw new SerializeException(e);
           }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            Hessian2Input hessian2Input = new Hessian2Input(byteArrayInputStream);
            if(log.isDebugEnabled()) {
                log.debug("类使用hessian【{}】反序列化成功】", clazz);
            }
            return clazz.cast(hessian2Input.readObject());
        }catch (IOException e) {
            log.error("在使用hessian反序列化对象【{}】时发生异常", clazz);
            throw new SerializeException(e);
        }
    }

    }
