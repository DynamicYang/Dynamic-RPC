package org.dynamic.rpc.loadbalancer.impl;

import lombok.extern.slf4j.Slf4j;
import org.dynamic.rpc.DynamicBootstrap;
import org.dynamic.rpc.loadbalancer.AbstractLoadBalancer;
import org.dynamic.rpc.loadbalancer.Selector;
import org.dynamic.rpc.transport.message.request.DynamicRPCRequest;

import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author: DynamicYang
 * @create: 2024-02-22
 * @Description:
 */
@Slf4j
public class ConsistencyHashLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected Selector getSelector(List<InetSocketAddress> serviceList) {
        return new ConsistencyHashLoadBalancer.ConsistencyHashSelector(serviceList,128);
    }

    private static class ConsistencyHashSelector implements Selector{

        private SortedMap<Integer,InetSocketAddress> circle = new TreeMap<>();



        private int virtualNodeCount;


        public ConsistencyHashSelector(List<InetSocketAddress> serviceList,int virtualNodeCount){
            //将服务结点转化为虚拟结点，进行挂载

            this.virtualNodeCount = virtualNodeCount;
            for(InetSocketAddress address : serviceList){
                addNodeToCircle(address);
            }
        }

        private void addNodeToCircle(InetSocketAddress address){
            //为每一个结点生成匹配的虚拟结点
            for (int i = 0; i < virtualNodeCount; i++) {

                int hash = hash(address.toString() + ":" + i);
                //将结点挂载到对应的虚拟结点上
                circle.put(hash,address);
            }
        }

        private void removeNodeFromCircle(InetSocketAddress address){

            for (int i = 0; i < virtualNodeCount; i++) {
                int hash = hash(address.toString() + ":" + i);
                circle.remove(hash);
            }
        }

        private int hash(String s) {
            MessageDigest md5;
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            byte[] bytes = md5.digest(s.getBytes());
            int res = 0;
            for (int i = 0; i < 4; i++) {
                res = (res << 8) | (bytes[i] & 0xFF);
            }
            return res;
        }


        @Override
        public InetSocketAddress getNext() {
            //哈希环建立完成之后，需要根据请求特征进行后续hash运算
            DynamicRPCRequest rpcRequest = DynamicBootstrap.RPC_REQUEST.get();

            //根据请求特征进行hash运算
            String requestId =String.valueOf(rpcRequest.getRequestId());

            //对请求ID做hash，覆写String的hash
            int hash = hash(requestId);

            //判断该hash值是否能够直接落在一个服务器节点上，和服务器的hash一样
            if(!circle.containsKey(hash)){

                SortedMap<Integer,InetSocketAddress> tailMap = circle.tailMap(hash);
                //hash值落在虚拟结点上
                hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
            }

            return circle.get(hash);

        }


    }

}
