package org.dynamic.rpc.channel.handler.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;

import java.util.concurrent.CompletableFuture;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        log.info("收到消息{}",msg.toString(CharsetUtil.UTF_8));

        String result = msg.toString(CharsetUtil.UTF_8);
        // 从全局挂起的请求当中寻找与之匹配的待处理的completeFuture
        CompletableFuture<Object> completableFuture = DynamicBootstrap.PENDING_REQUEST.get(1L);
        completableFuture.complete(result);
    }
}
