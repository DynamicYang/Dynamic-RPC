package org.dynamic.rpc.channel.handler.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.transport.message.MessageFormatConstant;
import org.dynamic.rpc.transport.message.response.DynamicRPCResponse;

import java.util.concurrent.CompletableFuture;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<DynamicRPCResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DynamicRPCResponse response) throws Exception {

//            if(response.getRequestType() == RequestType.HEART_BEAT.getId()) {
//                Object returnValue = new Object();
//            }

            Object returnValue = response.getBody();

            returnValue = returnValue == null ? new Object(): returnValue;

            CompletableFuture<Object> completableFuture = DynamicBootstrap.PENDING_REQUEST.get(response.getRequestId());

            completableFuture.complete(returnValue);

            if (log.isDebugEnabled()){
                log.debug("返回结果：{}", returnValue);
            }
    }
}
