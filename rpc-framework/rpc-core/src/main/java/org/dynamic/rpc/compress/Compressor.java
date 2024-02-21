package org.dynamic.rpc.compress;

public interface Compressor {

    /**
     * @Author DynamicYang
     * @Description: 压缩
     * @Param
     * @return byte[]
     **/
    byte[] compress(byte[] bytes);

    /**
     * @Author DynamicYang
     * @Description: 解压
     * @Param
     * @return byte[]
     **/
    byte[] decompress(byte[] bytes);
}
