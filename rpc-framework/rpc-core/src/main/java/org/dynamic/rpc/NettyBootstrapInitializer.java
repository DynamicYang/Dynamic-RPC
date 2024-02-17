package org.dynamic.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author: DynamicYang
 * @create: 2023-09-16
 * @Description:
 * todo 怎么扩展
 */
public class NettyBootstrapInitializer {
    private static  final Bootstrap bootstrap = new Bootstrap();
    private static final Logger log = LoggerFactory.getLogger(NettyBootstrapInitializer.class);


    static{
        final NioEventLoopGroup eventGroup = new NioEventLoopGroup();
        bootstrap.group(eventGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                super.channelRead(ctx, msg);
                            }

                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {

                                log.info("收到消息{}",msg.toString(CharsetUtil.UTF_8));

                                String result = msg.toString(CharsetUtil.UTF_8);
                                // 从全局挂起的请求当中寻找与之匹配的待处理的completeFuture
                                CompletableFuture<Object> completableFuture = DynamicBootstrap.PENDING_REQUEST.get(1l);
                                completableFuture.complete(result);


                            }
                        });

                    }
                });

    }
    private NettyBootstrapInitializer(){}

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }



}
