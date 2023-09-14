package org.dynamic.rpc;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class ZooKeeperNode {
    byte[] data;
    String nodePath;

    public ZooKeeperNode( String nodePath,byte[] data) {
        this.data = data;
        this.nodePath = nodePath;
    }

    public ZooKeeperNode() {

    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setNodePath(String nodePath) {
        this.nodePath = nodePath;
    }
}
