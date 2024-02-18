package org.dynamic.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.dynamic.rpc.channel.ConsumerChannelInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
                .handler(new ConsumerChannelInitializer());

    }
    private NettyBootstrapInitializer(){}

    public static Bootstrap getBootstrap(){
        return bootstrap;
    }



}
