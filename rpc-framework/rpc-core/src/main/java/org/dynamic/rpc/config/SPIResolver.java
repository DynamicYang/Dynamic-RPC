package org.dynamic.rpc.config;

import org.dynamic.rpc.compress.Compressor;
import org.dynamic.rpc.compress.CompressorFactory;
import org.dynamic.rpc.loadbalancer.LoadBalancer;
import org.dynamic.rpc.serialization.Serializer;
import org.dynamic.rpc.serialization.SerializerFactory;
import org.dynamic.rpc.spi.SPIHandler;

import java.util.List;

/**
 * @author: DynamicYang
 * @create: 2024-03-07
 * @Description:
 */
public class SPIResolver {

    public static  void loadBySPI(Configuration configuration) {
        List<LoadBalancer> loadBalancers = SPIHandler.get(LoadBalancer.class);



        List<Compressor> compressors = SPIHandler.get(Compressor.class);
        CompressorFactory.addCompressorIfAbsent(compressors);


        List<Serializer> serializers = SPIHandler.get(Serializer.class);
        SerializerFactory.addSerializerIfAbsent(serializers);



    }


}
