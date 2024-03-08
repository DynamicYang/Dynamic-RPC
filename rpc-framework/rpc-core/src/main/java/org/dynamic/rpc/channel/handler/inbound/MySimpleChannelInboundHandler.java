package org.dynamic.rpc.channel.handler.inbound;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.enumration.ResponseCode;
import org.dynamic.rpc.exception.ResponseException;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.protection.CircuitBreaker;
import org.dynamic.rpc.transport.message.MessageFormatConstant;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.response.DynamicRPCResponse;
import org.dynamic.rpc.DynamicBootstrap;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description:
 */
@Slf4j
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<DynamicRPCResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DynamicRPCResponse response) throws Exception {

        // 从全局的挂起的请求中寻找与之匹配的待处理的completableFuture
        CompletableFuture<Object> completableFuture = DynamicBootstrap.PENDING_REQUEST.get(response.getRequestId());

        SocketAddress socketAddress = channelHandlerContext.channel().remoteAddress();
        Map<SocketAddress, CircuitBreaker> everyIpCircuitBreaker = DynamicBootstrap.getInstance()
                .getConfiguration().getEveryIpCircuitBreaker();
        CircuitBreaker circuitBreaker = everyIpCircuitBreaker.get(socketAddress);

        int code = response.getCode();
        if(code == ResponseCode.FAIL.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，返回错误的结果，响应码[{}].",
                    response.getRequestId(),response.getCode());
            throw new ResponseException(code,ResponseCode.FAIL.getDesc());

        } else if (code == ResponseCode.RATE_LIMIT.getCode()){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，被限流，响应码[{}].",
                    response.getRequestId(),response.getCode());
            throw new ResponseException(code,ResponseCode.RATE_LIMIT.getDesc());

        } else if (code == ResponseCode.RESOURCE_NOT_FOUND.getCode() ){
            circuitBreaker.recordErrorRequest();
            completableFuture.complete(null);
            log.error("当前id为[{}]的请求，未找到目标资源，响应码[{}].",
                    response.getRequestId(),response.getCode());
            throw new ResponseException(code,ResponseCode.RESOURCE_NOT_FOUND.getDesc());

        } else if (code == ResponseCode.SUCCESS.getCode() ){
            // 服务提供方，给予的结果
            Object returnValue = response.getBody();
            completableFuture.complete(returnValue);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture，处理响应结果。", response.getRequestId());
            }
        } else if(code == ResponseCode.SUCCESS_HEART_BEAT.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("以寻找到编号为【{}】的completableFuture,处理心跳检测，处理响应结果。", response.getRequestId());
            }
        } else if(code == ResponseCode.BECOLSING.getCode()){
            completableFuture.complete(null);
            if (log.isDebugEnabled()) {
                log.debug("当前id为[{}]的请求，访问被拒绝，目标服务器正处于关闭中，响应码[{}].",
                        response.getRequestId(),response.getCode());
            }

            // 修正负载均衡器
            // 从健康列表中移除
            DynamicBootstrap.CHANNEL_CACHE.remove(socketAddress);
            // reLoadBalance
            LoadBalancer loadBalancer = DynamicBootstrap.getInstance()
                    .getConfiguration().getLoadBalancer();
            // 重新进行负载均衡
            DynamicRPCRequest DynamicRPCRequest = DynamicBootstrap.RPC_REQUEST.get();
            loadBalancer.reBalance(DynamicRPCRequest.getPayload().getInterfaceName()
                    ,DynamicBootstrap.CHANNEL_CACHE.keySet().stream().toList());

            throw new ResponseException(code,ResponseCode.BECOLSING.getDesc());
        }
    }
}
