package org.dynamic.rpc.channel;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.dynamic.rpc.channel.handler.inbound.DynamicRPCRequestDecoder;
import org.dynamic.rpc.channel.handler.inbound.MySimpleChannelInboundHandler;
import org.dynamic.rpc.channel.handler.outbound.DynamicRPCRequestEncoder;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast(new DynamicRPCRequestEncoder())
                .addLast(new DynamicRPCRequestDecoder())
                .addLast(new MySimpleChannelInboundHandler());
    }
}
