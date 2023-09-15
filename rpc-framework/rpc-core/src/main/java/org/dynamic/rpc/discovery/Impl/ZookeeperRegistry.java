package org.dynamic.rpc.discovery.Impl;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.dynamic.rpc.*;
import org.dynamic.rpc.discovery.AbstractRegistry;
import org.dynamic.rpc.exception.DiscoveryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

/**
 * @author: DynamicYang
 * @create: 2023-09-15
 * @Description:
 */
public class ZookeeperRegistry extends AbstractRegistry {
    private static final Logger log = LoggerFactory.getLogger(ZookeeperRegistry.class);
    private ZooKeeper zooKeeper ;

    public ZookeeperRegistry(){
        this.zooKeeper = ZookeeperUtils.createZookeeper();
    }
    public ZookeeperRegistry(String connectString,int timeout){
        this.zooKeeper = ZookeeperUtils.createZookeeper(connectString,timeout);
    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {

        //服务节点的名称
        String serviceParentPath = Constant.BASE_PROVIDER_NODE + "/"+serviceConfig.getServiceInterface().getName();
        if(ZookeeperUtils.exists(zooKeeper,serviceParentPath,null)){
            ZooKeeperNode serviceParentNode = new ZooKeeperNode(serviceParentPath,null);
            ZookeeperUtils.createNode(zooKeeper,serviceParentNode,null, CreateMode.PERSISTENT);

        }
        //创建本机服务结点,当前服务结点应该是一个临时结点，以ip:port作为本机服务名称的标识
        String currentNodePath = serviceParentPath + "/" + NetUtils.getInet4Address()+":8088";
        if (ZookeeperUtils.exists(zooKeeper,currentNodePath,null)){
            ZooKeeperNode currentNode = new ZooKeeperNode(currentNodePath,"demo".getBytes());
            boolean is =  ZookeeperUtils.createNode(zooKeeper,currentNode,null,CreateMode.PERSISTENT);
            log.debug("本机结点{}已注册",NetUtils.getInet4Address());
        }


        if(log.isDebugEnabled()){
            log.debug("服务{}已经被注册", serviceConfig.getServiceInterface().getName());
        }
    }

    @Override
    public void serviceDown(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public InetSocketAddress lookup(String serviceName) {
        //找到服务对应的结点
        String serviceNodePath = Constant.BASE_PROVIDER_NODE + "/" + serviceName;
        //从zk中获取他的子节点
        List<String> children = ZookeeperUtils.getChildren(zooKeeper, serviceNodePath, null);
        List<InetSocketAddress> collect = children.stream().map(ip -> {
            String[] ipAndPort = ip.split(":");
            return new InetSocketAddress(ipAndPort[0], Integer.parseInt(ipAndPort[1]));
        }).toList();
        if(collect.size() == 0){
            throw new DiscoveryException("未发现可用的服务主机");
        }
        return collect.get(0);
    }
}
