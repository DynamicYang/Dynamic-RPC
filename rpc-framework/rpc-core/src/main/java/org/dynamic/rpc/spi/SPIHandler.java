package org.dynamic.rpc.spi;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.loadbalancer.LoadBalancer;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: DynamicYang
 * @create: 2024-03-07
 * @Description:
 */
@Slf4j
public class SPIHandler {
    private static final String BASE_PATH = "META-INF/rpc-services/";

    //缓存接口实现
    private static final Map<Class<?>,List<Object>> SPI_IMPLEMENT = new ConcurrentHashMap<>(16);


    //先定义一个缓存，保存spi相关的原始内容,避免频繁io
    private static final  Map<String, List<String>> SPI_CONTENT = new ConcurrentHashMap<>(16);


    static{

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url = classLoader.getResource(BASE_PATH);
            if (url == null) {
                throw new RuntimeException("META-INF/services目录不存在");
            }else {
                File file = new File(url.getPath());
                File[] files = file.listFiles();
                if (files != null && files.length > 0) {
                    for (File f : files) {
                        String key = f.getName();
                        List<String> value = getImplClassName(f);
                        SPI_CONTENT.put(key, value);
                    }
                }
            }
    }

    private static List<String> getImplClassName(File f) {
        try (FileReader fileReader = new FileReader(f); BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            List<String> implClassName = new ArrayList<>(16);

            while(true){
                String line = bufferedReader.readLine();
                if(line == null || "".equals(line))  break;
                implClassName.add(line);

            }
            return implClassName;


        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }



    public static <T> List<T> get(Class<T> clazz) {
        List impls = SPI_IMPLEMENT.get(clazz);
        if (impls != null && impls.size() > 0) {
            return  impls;
        }



        String clazzName = clazz.getName();
        List<String> implClassName = SPI_CONTENT.get(clazzName);

        impls = new ArrayList<>();
        //实例化
        try {
            for (String className : implClassName) {
                Class<?> aClass = Class.forName(className);
                Object instance = aClass.getConstructor().newInstance();
                impls.add(instance);

            }
            SPI_IMPLEMENT.put(clazz, impls);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
           log.info("无法实例化类：{}",clazzName);
        }

        return impls;
    }



}
