package org.dynamic.rpc.channel.handler.inbound;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.ServiceConfig;
import org.dynamic.rpc.enumration.RequestType;
import org.dynamic.rpc.enumration.ResponseCode;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;
import org.dynamic.rpc.transport.message.request.Payload;
import org.dynamic.rpc.transport.message.response.DynamicRPCResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: DynamicYang
 * @create: 2024-02-19
 * @Description:
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<DynamicRPCRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DynamicRPCRequest msg) throws Exception {

        //1、获取负载内容
        Payload payload = msg.getPayload();
        //2、根据负载内容进行方法调用
        Object o = callTargetMethod(payload);
        if(log.isDebugEnabled())    {
            log.debug("请求【{}】已经在服务端完成方法调用，",msg.getRequestId());
        }
        //3、封装响应
        DynamicRPCResponse response = new DynamicRPCResponse();

        response.setRequestId(msg.getRequestId());
        response.setCode(ResponseCode.SUCCESS.getCode());
        response.setRequestType(msg.getRequestType());
        response.setCompressType(msg.getCompressType());
        response.setSerializationType((msg.getSerializationType()));
        response.setBody(o);
        //4、写出响应

        ctx.channel().writeAndFlush(o);


    }

    private Object callTargetMethod(Payload payload){
        String interfaceName = payload.getInterfaceName();
        String methodName = payload.getMethodName();
        Class<?>[] parameterTypes = payload.getParameterTypes();
        Object[] args = payload.getParametersValues();

        //寻找匹配的暴露出去的具体的实现
        ServiceConfig<?> serviceConfig = DynamicBootstrap.SERVICES_COLLECTION.get(interfaceName);
        Object impl = serviceConfig.getService();
        Object result ;
        //通过反射调用
        try {
            Method method = impl.getClass().getMethod(methodName, parameterTypes);
            result = method.invoke(impl,args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            log.error("调用服务【{}】的方法【{}】时发生了异常",interfaceName,methodName,e);
            throw new RuntimeException(e);
        }
        return result;
    }
}
