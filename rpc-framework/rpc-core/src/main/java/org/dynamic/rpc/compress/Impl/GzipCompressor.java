package org.dynamic.rpc.compress.Impl;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.compress.Compressor;
import org.dynamic.rpc.exception.CompressException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author: DynamicYang
 * @create: 2024-02-21
 * @Description:
 */
@Slf4j
public class GzipCompressor implements Compressor {
    @Override
    public byte[] compress(byte[] bytes) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);
            gzipOutputStream.write(bytes);
            gzipOutputStream.finish();
            if(log.isDebugEnabled()){
                log.debug("使用gzip压缩成功", bytes);
            }
            return byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            log.error("使用gzip压缩时发生异常", e);
            throw new CompressException(e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        try {
            GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);
            byte[] result = new byte[bytes.length];
            int len = gzipInputStream.read(result);
            if(log.isDebugEnabled()){
                log.debug("使用gzip解压缩成功", bytes);
            }
            return result;
        } catch (IOException e) {
            log.error("使用gzip解压缩时发生异常", e);
            throw new CompressException(e);
        }

    }
}
