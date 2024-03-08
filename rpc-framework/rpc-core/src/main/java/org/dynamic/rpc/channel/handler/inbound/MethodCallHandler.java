package org.dynamic.rpc.channel.handler.inbound;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.ServiceConfig;
import org.dynamic.rpc.core.ShutDownHolder;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.enumration.ResponseCode;
import org.dynamic.rpc.protection.RateLimiter;
import org.dynamic.rpc.protection.TokenBuketRateLimiter;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.request.Payload;
import org.dynamic.rpc.transport.message.response.DynamicRPCResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketAddress;
import java.util.Map;

/**
 * @author: DynamicYang
 * @create: 2024-02-19
 * @Description:
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<DynamicRPCRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DynamicRPCRequest DynamicRPCRequest) throws Exception {

        // 1、先封装部分响应
        DynamicRPCResponse DynamicRPCResponse = new DynamicRPCResponse();
        DynamicRPCResponse.setRequestId(DynamicRPCRequest.getRequestId());
        DynamicRPCResponse.setCompressType(DynamicRPCRequest.getCompressType());
        DynamicRPCResponse.setSerializationType(DynamicRPCRequest.getSerializationType());

        // 2、 获得通道
        Channel channel = channelHandlerContext.channel();

        // 3、查看关闭的挡板是否打开，如果挡板已经打开，返回一个错误的响应
        if( ShutDownHolder.BAFFLE.get() ){
            DynamicRPCResponse.setCode(ResponseCode.BECOLSING.getCode());
            channel.writeAndFlush(DynamicRPCResponse);
            return;
        }

        // 4、计数器加一
        ShutDownHolder.REQUEST_COUNTER.increment();

        // 4、完成限流相关的操作
        SocketAddress socketAddress = channel.remoteAddress();
        Map<SocketAddress, RateLimiter> everyIpRateLimiter =
                DynamicBootstrap.getInstance().getConfiguration().getEveryIpRateLimiter();

        RateLimiter rateLimiter = everyIpRateLimiter.get(socketAddress);
        if (rateLimiter == null) {
            rateLimiter = new TokenBuketRateLimiter(10, 10);
            everyIpRateLimiter.put(socketAddress, rateLimiter);
        }
        boolean allowRequest = rateLimiter.allowRequest();

        // 5、处理请求的逻辑
        // 限流
        if (!allowRequest) {
            // 需要封装响应并且返回了
            DynamicRPCResponse.setCode(ResponseCode.RATE_LIMIT.getCode());

            // 处理心跳
        } else if (DynamicRPCRequest.getRequestType() == RequestType.HEART_BEAT.getId()) {
            // 需要封装响应并且返回
            DynamicRPCResponse.setCode(ResponseCode.SUCCESS_HEART_BEAT.getCode());

            // 正常调用
        } else {
            /** ---------------具体的调用过程--------------**/
            // （1）获取负载内容
            Payload requestPayload = DynamicRPCRequest.getPayload();

            // （2）根据负载内容进行方法调用
            try {
                Object result = callTargetMethod(requestPayload);
                if (log.isDebugEnabled()) {
                    log.debug("请求【{}】已经在服务端完成方法调用。", DynamicRPCRequest.getRequestId());
                }
                // （3）封装响应   我们是否需要考虑另外一个问题，响应码，响应类型
                DynamicRPCResponse.setCode(ResponseCode.SUCCESS.getCode());
                DynamicRPCResponse.setBody(result);
            } catch (Exception e){
                log.error("编号为【{}】的请求在调用过程中发生异常。",DynamicRPCRequest.getRequestId(),e);
                DynamicRPCResponse.setCode(ResponseCode.FAIL.getCode());
            }
        }

        // 6、写出响应
        channel.writeAndFlush(DynamicRPCResponse);

        // 7、计数器减一
        ShutDownHolder.REQUEST_COUNTER.decrement();
    }

    private Object callTargetMethod(Payload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParameterTypes();
        Object[] parametersValue = requestPayload.getParametersValues();

        // 寻找到匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = DynamicBootstrap.SERVICES_COLLECTION.get(interfaceName);
        Object refImpl = serviceConfig.getService();

        // 通过反射调用 1、获取方法对象  2、执行invoke方法
        Object returnValue;
        try {
            Class<?> aClass = refImpl.getClass();
            Method method = aClass.getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常。", interfaceName, methodName, e);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
