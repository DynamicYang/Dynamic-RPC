package org.dynamic.rpc.transport.message.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: DynamicYang
 * @create: 2024-02-17
 * @Description: 负载
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payload implements Serializable {

    //接口名称
    private String interfaceName;

    //调用方法的名字
    private String methodName;


   // 参数列表
    private Class[] parameterTypes;

    //参数值
    private Object[] parametersValues;

    //返回值的封装
    private Class<?> returnType;
//
//    public String getInterfaceName() {
//        return interfaceName;
//    }
//
//    public void setInterfaceName(String interfaceName) {
//        this.interfaceName = interfaceName;
//    }
//
//    public String getMethodName() {
//        return methodName;
//    }
//
//    public void setMethodName(String methodName) {
//        this.methodName = methodName;
//    }
//
//    public Class[] getParameterTypes() {
//        return parameterTypes;
//    }
//
//    public void setParameterTypes(Class[] parameterTypes) {
//        this.parameterTypes = parameterTypes;
//    }
//
//    public Object[] getParametersValues() {
//        return parametersValues;
//    }
//
//    public void setParametersValues(Object[] parametersValues) {
//        this.parametersValues = parametersValues;
//    }
//
//    public Class<?> getReturnType() {
//        return returnType;
//    }
//
//    public void setReturnType(Class<?> returnType) {
//        this.returnType = returnType;
//    }
//
//    public Payload(String interfaceName, String methodName, Class[] parameterTypes, Object[] parametersValues, Class<?> returnType) {
//        this.interfaceName = interfaceName;
//        this.methodName = methodName;
//        this.parameterTypes = parameterTypes;
//        this.parametersValues = parametersValues;
//        this.returnType = returnType;
//    }
//
//    public Payload() {
//
//    }
}
