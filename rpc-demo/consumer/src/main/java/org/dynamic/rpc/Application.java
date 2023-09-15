package org.dynamic.rpc;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class Application {


    public static void main(String[] args) {
        ReferenceConfig<HelloService> reference = new ReferenceConfig<>();
        reference.setInterface(HelloService.class);

        DynamicBootstrap.getInstance().
                application("demo-consumer")
                .registry(new RegistryConfig("zookeeper://172.29.207.114:2181"))
                .reference(reference);



        HelloService service = reference.get();
        System.out.println(service.sayHello("dynamic"));
    }
}
