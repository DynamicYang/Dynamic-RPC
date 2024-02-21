package org.dynamic.rpc;

import java.util.concurrent.atomic.LongAdder;

/**
 * @author: DynamicYang
 * @create: 2024-02-20
 * @Description: 手写雪花算法ID生成器，也可以用美团中间件
 */
public class IDGenerator {

     //起始时间戳
    public static final long START_STAMP = DateUtil.get("2023-09-09").getTime();

    //
    public static final int DATA_CENTER_BIT = 5;

    public static final int MACHINE_BIT = 5;

    public static final int SEQUENCE_BIT = 12;

    public static final long MAX_DATACENTER_NUM = ~(-1L << DATA_CENTER_BIT);

    public static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);

    public static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    public static final long MACHINE_LEFT = SEQUENCE_BIT;

    public static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;

    public static final long TIMESTAMP_LEFT = SEQUENCE_BIT + MACHINE_BIT + DATA_CENTER_BIT;

    private static final LongAdder snowFlake = new LongAdder();

    private static long dataCenterId;

    private static long machineId;

    private static LongAdder sequence = new LongAdder();

    private static long lastTimeStamp = -1;

    public IDGenerator(long dataCenterId, long machineId){
        if (dataCenterId > MAX_DATACENTER_NUM || machineId > MAX_MACHINE_NUM){
            throw new IllegalArgumentException("dataCenterId或machineId超出范围");
        }


    }


    public  long getId(){
        long currentTimeStamp = System.currentTimeMillis();

        long timeStamp = currentTimeStamp - START_STAMP;

        if(timeStamp < lastTimeStamp){
            throw new RuntimeException("服务器时间回拨");
        }

       if(timeStamp == lastTimeStamp){
           sequence.increment();
           if(sequence.sum() >=  MAX_SEQUENCE){
               timeStamp = tilNextMillis(lastTimeStamp);
           }
       }else {
           sequence.reset();
       }

       lastTimeStamp = timeStamp;

        return timeStamp << TIMESTAMP_LEFT | dataCenterId << DATACENTER_LEFT | machineId << MACHINE_LEFT | sequence.sum();
    }

    private static long tilNextMillis(long lastTimeStamp) {
        long currentTimeStamp = System.currentTimeMillis() - START_STAMP;

        while(currentTimeStamp == lastTimeStamp){
            currentTimeStamp = System.currentTimeMillis() - START_STAMP;
        }

        return currentTimeStamp;
    }


    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            IDGenerator iDGenerator = new IDGenerator(1, 2);
            new Thread(()->{
                System.out.println(iDGenerator.getId());
            }).start();

        }
    }




}
