package org.dynamic.rpc.config.properties;

import lombok.Data;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.config.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: DynamicYang
 * @create: 2024-03-09
 * @Description:
 */
@ConfigurationProperties(prefix = "dynamic.rpc")
@Component
@Data
public class DynamicRpcConfigurationProperties {



    private static final Configuration configuration = DynamicBootstrap.getInstance().getConfiguration();

    private String appName = "default"; ;

    private String group = "default";

    private String host = "127.0.0.1";

    private String serializationType = "hessian";

    private String port = "9876";

    private String registryAddress = "127.0.0.1:2181";

    private String registryType = "zookeeper";


}
