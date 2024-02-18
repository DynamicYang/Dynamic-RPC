package org.dynamic.rpc.channel.handler.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.transport.message.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.MessageFormatConstant;
import org.dynamic.rpc.transport.message.Payload;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */
@Slf4j
public class DynamicRPCMessageDecoder extends LengthFieldBasedFrameDecoder {

    public DynamicRPCMessageDecoder() {
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
        //7、解析请求类型 todo 判断是为心跳检测
        byte requestType = byteBuf.readByte();
        //8、解析请求id
        long requestId = byteBuf.readLong();

        DynamicRPCRequest dynamicRPCRequest = new DynamicRPCRequest();
        dynamicRPCRequest.setCompressType(compressType);
        dynamicRPCRequest.setSerializationType(serializationType);
        dynamicRPCRequest.setRequestType(requestType);


        //todo 心跳检测请求没有负载
        if (requestType == MessageFormatConstant.REQUEST_TYPE_HEARTBEAT){
            return dynamicRPCRequest;
        }
        //9、解析负载
        int payloadLength = fullLength - headerLength;
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        //解压缩 TODO


        //反序列化 TODO


        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(payload);
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)
        ) {
            Payload requestPayload = (Payload) objectInputStream.readObject();
            dynamicRPCRequest.setPayload(requestPayload);
        } catch (IOException | ClassNotFoundException e) {
            log.error("请求【{}】反序列化时发生异常",requestId,e);
            throw new RuntimeException(e);
        }

        return dynamicRPCRequest;
    }
}
