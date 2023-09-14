package org.dynamic.rpc;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class ServiceConfig<T> {
    private Class<T> serviceInterface;
    private T service;
    public void setInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public void setRef(T service) {
        this.service = service;

    }

    public Class<T> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<T> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public T getService() {
        return service;
    }

    public void setService(T service) {
        this.service = service;
    }
}
