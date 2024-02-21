package org.dynamic.rpc.channel.handler.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.compress.Compressor;
import org.dynamic.rpc.compress.CompressorFactory;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.serialization.Serializer;
import org.dynamic.rpc.serialization.SerializerFactory;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.MessageFormatConstant;
import org.dynamic.rpc.transport.message.request.Payload;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */
@Slf4j
public class DynamicRPCRequestDecoder extends LengthFieldBasedFrameDecoder {

    public DynamicRPCRequestDecoder() {
        //找到当前报文的总长度，截取报文，截取出的报文进行解析
        super(  //最大桢长度，超过就会直接丢弃
                MessageFormatConstant.MAX_FRAME_LENGTH,
                //长度的字段偏移量
                MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_LENGTH_LENGTH,
                //长度的字段长度
                MessageFormatConstant.FULL_LENGTH,
                //负载的适配长度
                -(MessageFormatConstant.MAGIC.length + MessageFormatConstant.HEADER_LENGTH + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_LENGTH_LENGTH),
                0
        );
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object msg = super.decode(ctx, in);
        if (msg instanceof ByteBuf byteBuf){
            return decodeFrame(byteBuf);
        }
        return null;
    }

    private Object decodeFrame(ByteBuf byteBuf){
        //1、解析魔数
        byte[] magic = new byte[MessageFormatConstant.MAGIC.length];
        byteBuf.readBytes(magic);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != MessageFormatConstant.MAGIC[i]){
                throw new RuntimeException("请求不合法！");
            }
        }
        //2、解析版本号
        byte version = byteBuf.readByte();
        if (version != MessageFormatConstant.VERSION){
            throw new RuntimeException("获得的请求的版本不支持！");
        }
        //3、解析头部长度
        short headerLength = byteBuf.readShort();
        //4、解析总长度
        int fullLength = byteBuf.readInt();
        //5、解析压缩类型
        byte compressType = byteBuf.readByte();
        //6、解析序列化类型
        byte serializationType = byteBuf.readByte();
        //7、解析请求类型  判断是否为心跳检测
        byte requestType = byteBuf.readByte();
        //8、解析请求id
        long requestId = byteBuf.readLong();

        DynamicRPCRequest dynamicRPCRequest = new DynamicRPCRequest();
        dynamicRPCRequest.setCompressType(compressType);
        dynamicRPCRequest.setSerializationType(serializationType);
        dynamicRPCRequest.setRequestType(requestType);


        // 心跳检测请求没有负载
        if (requestType == RequestType.HEART_BEAT.getId()) {
            return dynamicRPCRequest;
        }
        //9、解析负载
        int payloadLength = fullLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        //解压缩
        Compressor compressor = CompressorFactory.getCompressorWrapper(compressType).getCompressor();
        payload = compressor.decompress(payload);
        //反序列化
        Serializer serializer = SerializerFactory.getSerializerWrapper(serializationType).getSerializer();
        Payload requestPayload = serializer.deserialize(payload, Payload.class);

        dynamicRPCRequest.setPayload(requestPayload);
        return dynamicRPCRequest;
    }
}
