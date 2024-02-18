package org.dynamic.rpc.transport.message;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */
public class MessageFormatConstant {

    public  final static byte[] MAGIC = "drpc".getBytes(StandardCharsets.UTF_8);
    public  final static byte VERSION = 1;

    public final static short  HEADER_LENGTH = (byte) (MAGIC.length + 1 + 2 + 4 + 1 + 1 + 1 + 8);

    public final static int HEADER_LENGTH_LENGTH = 2;
    public final static int MAX_FRAME_LENGTH =  1024 * 1024;
    public static final int VERSION_LENGTH = 1 ;
    public static final int FULL_LENGTH = 4;


    public static final byte REQUEST_TYPE_HEARTBEAT = 2;
}
