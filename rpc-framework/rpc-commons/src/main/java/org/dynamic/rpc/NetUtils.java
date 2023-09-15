package org.dynamic.rpc;

import org.dynamic.rpc.exception.NetworkException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * @author: DynamicYang
 * @create: 2023-09-14
 * @Description:
 */
public class NetUtils {

    private static final Logger log = LoggerFactory.getLogger(NetUtils.class);
    public static String getIp(){


        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()){
                NetworkInterface iface = interfaces.nextElement();
                //过滤非回环接口和虚拟接口
                if( iface.isLoopback() || iface.isVirtual() || iface.isUp()){
                    continue;
                }
                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()){
                    InetAddress addr = addresses.nextElement();
                  //过滤环回地址和IPV6地址
                    if(addr instanceof Inet6Address || addr.isLoopbackAddress()){
                        continue;
                    }
                    return addr.getHostAddress ();
                }
            }
            log.debug("没有获取到网络信息");
            throw new NetworkException();
        } catch (SocketException e) {
            log.error("获取局域网ip时发生异常");
            throw new NetworkException(e);
        }

    }
    /*
   获取本机网内地址
    */
    public static String  getInet4Address(){
        try {
            //获取所有网络接口
            Enumeration<NetworkInterface> allNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            //遍历所有网络接口
            for(;allNetworkInterfaces.hasMoreElements();){
                NetworkInterface networkInterface=allNetworkInterfaces.nextElement();
                //如果此网络接口为 回环接口 或者 虚拟接口(子接口) 或者 未启用 或者 描述中包含VM
                if(networkInterface.isLoopback()||networkInterface.isVirtual()||!networkInterface.isUp()||networkInterface.getDisplayName().contains("VM")){
                    //继续下次循环
                    continue;
                }
                //如果不是Intel与Realtek的网卡
                if(!(networkInterface.getDisplayName().contains("Intel"))&&!(networkInterface.getDisplayName().contains("Realtek"))){
                         //继续下次循环
                            continue;
                }
                //遍历此接口下的所有IP（因为包括子网掩码各种信息）
                for(Enumeration<InetAddress> inetAddressEnumeration=networkInterface.getInetAddresses();inetAddressEnumeration.hasMoreElements();){
                    InetAddress inetAddress=inetAddressEnumeration.nextElement();
                    //如果此IP不为空
                    if(inetAddress!=null){
                        //如果此IP为IPV4 则返回
                        if(inetAddress instanceof Inet4Address){
                            return inetAddress.getHostAddress();
                        }

                       // -------这样判断IPV4更快----------
                        if(inetAddress.getAddress().length==4){
                            return inetAddress.getHostAddress();
                        }



                    }
                }


            }
            return null;

        }catch(SocketException e){

            new NetworkException(e);
            log.debug("获取局域网ip时发生异常");
            return null;
        }
    }


}
