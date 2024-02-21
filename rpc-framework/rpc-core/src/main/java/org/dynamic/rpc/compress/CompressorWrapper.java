package org.dynamic.rpc.compress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompressorWrapper {

    private byte code;

    private String type;

    private Compressor compressor;
}
