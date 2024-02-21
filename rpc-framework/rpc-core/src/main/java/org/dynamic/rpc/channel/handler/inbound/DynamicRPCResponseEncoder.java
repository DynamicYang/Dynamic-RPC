package org.dynamic.rpc.channel.handler.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.compress.Compressor;
import org.dynamic.rpc.compress.CompressorFactory;
import org.dynamic.rpc.serialization.Serializer;
import org.dynamic.rpc.serialization.SerializerFactory;
import org.dynamic.rpc.transport.message.MessageFormatConstant;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.request.Payload;
import org.dynamic.rpc.transport.message.response.DynamicRPCResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author: DynamicYang
 * @create: 2024-02-19
 * @Description:
 */
@Slf4j
public class DynamicRPCResponseEncoder extends MessageToByteEncoder<DynamicRPCResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DynamicRPCResponse response, ByteBuf byteBuf) throws Exception {

        // 三个字节的魔数值
        byteBuf.writeBytes(MessageFormatConstant.MAGIC);
        // 一个字节的版本号
        byteBuf.writeByte(MessageFormatConstant.VERSION);
        // 两个字节的头部长度
        byteBuf.writeShort(MessageFormatConstant.HEADER_LENGTH);

        //   总长度未知
        byteBuf.writerIndex( byteBuf.writerIndex() + MessageFormatConstant.FULL_LENGTH);
        //三个类型
        byteBuf.writeInt(response.getCode());
        byteBuf.writeByte(response.getCompressType());
        byteBuf.writeByte(response.getSerializationType());





        //序列化
        Serializer serializer = SerializerFactory.getSerializerWrapper(response.getSerializationType()).getSerializer();
        // 写入请求体
        byte[] body = serializer.serialize(response.getBody());

        // 压缩
        Compressor compressor = CompressorFactory.getCompressorWrapper(response.getCompressType()).getCompressor();
         body = compressor.compress(body);

        if (body != null){
            byteBuf.writeBytes(body);
        }
        int bodyLength = body == null ? 0 : body.length;

        // 重新处理报文的总长度
        //先保存当前写指针的位置
        int index = byteBuf.writerIndex();
        byteBuf.writerIndex(MessageFormatConstant.MAGIC.length + MessageFormatConstant.VERSION_LENGTH + MessageFormatConstant.HEADER_LENGTH_LENGTH);
        byteBuf.writeInt(MessageFormatConstant.HEADER_LENGTH+bodyLength);

        //将写指针归位
        byteBuf.writerIndex(index);
        if(log.isDebugEnabled()){
            log.debug("请求【{}】已经在服务端完成响应编码，",response.getRequestId());
        }




    }


}
