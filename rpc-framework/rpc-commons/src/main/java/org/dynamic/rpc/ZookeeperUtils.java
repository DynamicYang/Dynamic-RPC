package org.dynamic.rpc;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description: zookeeper工具类
 */
public class ZookeeperUtils {
    private static final Logger log = LoggerFactory.getLogger(ZookeeperUtils.class);

    public static ZooKeeper createZookeeper(){
        String connectString = Constant.DEFAULT_CONNECTSTRING;
        int timeout = Constant.DEFAULT_TIMEOUT;
        return createZookeeper(connectString,timeout);
    }
    
    /**
     * @Author DynamicYang
     * @Description:创建zookeeper实例
     * @Date  2023/9/14
     * @Param 
     * @return org.apache.zookeeper.ZooKeeper
     **/
    public static ZooKeeper createZookeeper(String connectString,int timeout){
        CountDownLatch countDownLatch = new CountDownLatch(1);
        
        try {
            final ZooKeeper zooKeeper = new ZooKeeper(connectString,timeout,event->{
                
                //只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    log.debug("客户端连接成功。");
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
            return zooKeeper;
            
        } catch (InterruptedException | IOException e) {
            log.error("创建zookeeper实例时发生异常",e);
            throw new   RuntimeException(e);
        }

    }

    /**
     * @Author DynamicYang
     * @Description: 创建zookeeper结点的工具方法
     * @Date  2023/9/14
     * @Param
     * @return java.lang.Boolean
     **/
    public static Boolean createNode(ZooKeeper zookeeper, ZooKeeperNode node, Watcher watcher , CreateMode mode){
        try{
            if (zookeeper.exists(node.getNodePath(), watcher) == null){
                String result = zookeeper.create(node.getNodePath(), node.getData(), ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
                return true;
            }
            else {
                if(log.isDebugEnabled()){
                    log.debug("结点【{}】已经存在", node.getNodePath());
                }
                return  false;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }

    }



    public static boolean exists(ZooKeeper zookeeper, String node,Watcher watcher){
        try {
            return zookeeper.exists(node, watcher) != null;
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static void close(ZooKeeper zookeeper){
        try {
            zookeeper.close();
        } catch (InterruptedException e) {
            log.error("关闭zookeeper实例时发生异常",e);
            throw new RuntimeException(e);
        }
    }

    public static List<String> getChildren(ZooKeeper zookeeper, String node,Watcher watcher){
        try {
            return zookeeper.getChildren(node, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error("获取子节点【{}】时发生异常",node,e);
            throw new RuntimeException(e);
        }

    }


}
