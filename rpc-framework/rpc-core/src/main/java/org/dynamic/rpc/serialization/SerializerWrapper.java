package org.dynamic.rpc.serialization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SerializerWrapper {
    private byte code;

    private String type;

    private Serializer serializer;

}
