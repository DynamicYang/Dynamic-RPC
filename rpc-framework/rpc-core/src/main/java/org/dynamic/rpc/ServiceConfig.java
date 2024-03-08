package org.dynamic.rpc;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class ServiceConfig<T> {
    private Class<?> serviceInterface;
    private Object service;

    private String group = "default";
    public void setInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }



    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setRef(Object service) {
        this.service = service;

    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Object getService() {
        return service;
    }

    public void setService(T service) {
        this.service = service;
    }
}
