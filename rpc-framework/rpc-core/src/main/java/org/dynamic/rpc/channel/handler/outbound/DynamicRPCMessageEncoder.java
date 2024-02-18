package org.dynamic.rpc.channel.handler.outbound;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.transport.message.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.MessageFormatConstant;
import org.dynamic.rpc.transport.message.Payload;
import java.io.*;


/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description: 消息出栈第一步处理
 * 4位magic(魔数) -- drpc.getByte()
 * 1位version(版本号) -- 1
 * 2位 header length(头部长度)
 * 4位 full length(总的报文长度)
 * 1位compress type(压缩类型)
 * 1位serialization type(序列化类型)
 * 1位request type(请求类型)
 * 8位 request id(请求id)
 */
@Slf4j
public class DynamicRPCMessageEncoder extends MessageToByteEncoder<DynamicRPCRequest>  {



    @Override
    protected void encode(ChannelHandlerContext ctx, DynamicRPCRequest msg, ByteBuf out) throws Exception {

        // 三个字节的魔数值
        out.writeBytes(MessageFormatConstant.MAGIC);
        // 一个字节的版本号
        out.writeByte(MessageFormatConstant.VERSION);
        // 两个字节的头部长度
        out.writeShort(MessageFormatConstant.HEADER_LENGTH);

        //   总长度未知
        out.writerIndex( out.writerIndex() + 4);
        //三个类型
        out.writeByte(msg.getRequestType());
        out.writeByte(msg.getCompressType());
        out.writeByte(msg.getSerializationType());


        // 写入请求体
        byte[] body = getBodyBytes(msg.getPayload());
        out.writeBytes(body);
        // 重新处理报文的总长度
        //先保存当前写指针的位置
        int index = out.writerIndex();
        out.writerIndex(7);
        out.writeInt(MessageFormatConstant.HEADER_LENGTH+body.length);

        //将写指针归位
        out.writerIndex(index);





    }

    private byte[] getBodyBytes(Payload payload){
        // todo 还需要针对不同的消息类型进行不同的处理
        //序列化
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = null;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(payload);
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            log.error("序列化异常",e);
            throw new RuntimeException(e);
        }



    }
}
