package org.dynamic.rpc.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: DynamicYang
 * @create: 2024-03-07
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ObjectWrapper<T> {
    String type;
    Byte code;
    T t;
}
