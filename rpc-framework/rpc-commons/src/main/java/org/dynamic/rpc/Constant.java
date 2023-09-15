package org.dynamic.rpc;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class Constant {
//    public static String DEFAULT_CONNECTSTRING = "172.29.207.114:2181";
    public static String DEFAULT_CONNECTSTRING = "zookeeper://127.0.0.1:2181";
    public static int DEFAULT_TIMEOUT = 10000;

    public static String BASE_NODE = "/rpc-metadata";
    public static String BASE_CONSUMER_NODE = BASE_NODE + "/consumer";

    public static String BASE_PROVIDER_NODE = BASE_NODE + "/provider";




}
