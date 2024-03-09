package org.dynamic.rpc;

import org.dynamic.rpc.annotation.RpcService;


import org.dynamic.rpc.config.properties.DynamicRpcConfigurationProperties;
import org.dynamic.rpc.proxy.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Field;

/**
 * @author: DynamicYang
 * @create: 2024-03-08
 * @Description:
 */
@Component
public class DynamicRPCProxyBeanPostProcessor implements BeanPostProcessor {

    @Autowired
    private DynamicRpcConfigurationProperties dynamicRpcConfigurationProperties;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 想办法给他生成一个代理
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            RpcService rpcService = field.getAnnotation(RpcService.class);
            if(rpcService != null){
                // 获取一个代理
                Class<?> type = field.getType();
                Object proxy = ProxyFactory.getProxy(type);
                field.setAccessible(true);
                try {
                    field.set(bean,proxy);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return bean;
    }
}
