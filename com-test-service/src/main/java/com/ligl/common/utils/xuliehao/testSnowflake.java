package com.ligl.common.utils.xuliehao;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2018/1/26 0026 下午 3:56
 * Version: 1.0
 * Twitter-Snowflake算法生成序列号
 *  https://www.cnblogs.com/songshuai/p/5603111.html
 *  http://blog.csdn.net/yangding_/article/details/52768906
 *  http://blog.csdn.net/li396864285/article/details/54668031
 */
public class testSnowflake {

    public static void main(String[] args) {
        Long andf = nextId();
        System.out.println("===="+andf);
    }


    public static synchronized long nextId() {
        long twepoch = 1288834974657L;
        long workerId= 0L;
        long dataCenterId= 0L;
        long sequence = 0L;
        long workerIdBits = 5L;
        long dataCenterIdBits = 5L;
        long maxWorkerId = -1L ^ (-1L << workerIdBits);
        long maxDataCenterId = -1L ^ (-1L << dataCenterIdBits);
        long sequenceBits = 12L;

        long workerIdShift = sequenceBits;
        long dataCenterIdShift = sequenceBits + workerIdBits;
        long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
        long sequenceMask = -1L ^ (-1L << sequenceBits);

        long lastTimestamp = -1L;

        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - twepoch) << timestampLeftShift) | (dataCenterId << dataCenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private static long timeGen() {
        return System.currentTimeMillis();
    }






}
