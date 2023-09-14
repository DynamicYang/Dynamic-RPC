package org.dynamic.rpc;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:管理注册中心，生成基础目录
 */
public class Main {
    public static void main(String[] args) {
        ZooKeeper zooKeeper = ZookeeperUtils.createZookeeper();
        String basePath = Constant.BASE_NODE;
        String consumerPath =  Constant.BASE_CONSUMER_NODE;
        String providerPath =  Constant.BASE_PROVIDER_NODE;
        ZooKeeperNode baseNode = new ZooKeeperNode(basePath,null);
        ZooKeeperNode consumerNode = new ZooKeeperNode(consumerPath,null);
        ZooKeeperNode providerNode = new ZooKeeperNode(providerPath,null);
        List.of(baseNode,consumerNode,providerNode).forEach(node -> {
            ZookeeperUtils.createNode(zooKeeper,node,null, CreateMode.PERSISTENT);
        });


    }
}
