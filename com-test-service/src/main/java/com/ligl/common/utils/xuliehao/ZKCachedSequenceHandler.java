/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese
 * opensource volunteers. you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Any questions about this component can be directed to it's project Web address
 * https://code.google.com/p/opencloudb/.
 *
 */
package com.ligl.common.utils.xuliehao;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据开源项目mycat实现基于zookeeper的递增序列号
 * <p>
 * 只要配置好ZK地址和表名的如下属性
 * MINID 某线程当前区间内最小值
 * MAXID 某线程当前区间内最大值
 * CURID 某线程当前区间内当前值
 *
 * @author wangwanbin
 * @version 1.0
 * @time 2017/9/1
 */
public class ZKCachedSequenceHandler extends SequenceHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(ZKCachedSequenceHandler.class);
    private static final String KEY_MIN_NAME = ".MINID";// 1
    private static final String KEY_MAX_NAME = ".MAXID";// 10000
    private static final String KEY_CUR_NAME = ".CURID";// 888
    private final static long PERIOD = 1000;//每次缓存的ID段数量
    private static ZKCachedSequenceHandler instance = new ZKCachedSequenceHandler();
    final static String ZK_ADDRESS = "192.168.0.65"; //192.168.0.65
    final static String PATH = "/sequence/p2p";
    final static String SEQ = "seq";

    /**
     * 私有化构造方法,单例模式
     */
    private ZKCachedSequenceHandler() {
    }

    /**
     * 获取sequence工具对象的唯一方法
     *
     * @return
     */
    public static ZKCachedSequenceHandler getInstance() {
        return instance;
    }

    private Map<String, Map<String, String>> tableParaValMap = null;

    private CuratorFramework client;
    private InterProcessSemaphoreMutex interProcessSemaphore = null;

    public void loadZK() {
        try {
            this.client = CuratorFrameworkFactory.newClient(ZK_ADDRESS, new ExponentialBackoffRetry(1000, 3));
            this.client.start();
        } catch (Exception e) {
            LOGGER.error("Error caught while initializing ZK:" + e.getCause());
        }
    }

    public Map<String, String> getParaValMap(String prefixName) {
        if (tableParaValMap == null) {
            try {
                loadZK();
                fetchNextPeriod(prefixName);
            } catch (Exception e) {
                LOGGER.error("Error caught while loding configuration within current thread:" + e.getCause());
            }
        }
        Map<String, String> paraValMap = tableParaValMap.get(prefixName);
        return paraValMap;
    }

    public Boolean fetchNextPeriod(String prefixName) {
        try {
            Stat stat = this.client.checkExists().forPath(PATH + "/" + prefixName + SEQ);

            if (stat == null || (stat.getDataLength() == 0)) {
                try {
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                            .forPath(PATH + "/" + prefixName + SEQ, String.valueOf(0).getBytes());
                } catch (Exception e) {
                    LOGGER.debug("Node exists! Maybe other instance is initializing!");
                }
            }
            if (interProcessSemaphore == null) {
                interProcessSemaphore = new InterProcessSemaphoreMutex(client, PATH + "/" + prefixName + SEQ);
            }
            interProcessSemaphore.acquire();
            if (tableParaValMap == null) {
                tableParaValMap = new ConcurrentHashMap<>();
            }
            Map<String, String> paraValMap = tableParaValMap.get(prefixName);
            if (paraValMap == null) {
                paraValMap = new ConcurrentHashMap<>();
                tableParaValMap.put(prefixName, paraValMap);
            }
            long now = Long.parseLong(new String(client.getData().forPath(PATH + "/" + prefixName + SEQ)));
            client.setData().forPath(PATH + "/" + prefixName + SEQ, (Long.toString(now + PERIOD)).getBytes());
            if (now == 1) {
                paraValMap.put(prefixName + KEY_MAX_NAME, Long.toString(PERIOD));
                paraValMap.put(prefixName + KEY_MIN_NAME, "1");
                paraValMap.put(prefixName + KEY_CUR_NAME, "0");
            } else {
                paraValMap.put(prefixName + KEY_MAX_NAME, Long.toString(now + PERIOD));
                paraValMap.put(prefixName + KEY_MIN_NAME, Long.toString(now));
                paraValMap.put(prefixName + KEY_CUR_NAME, Long.toString(now));
            }
        } catch (Exception e) {
            LOGGER.error("Error caught while updating period from ZK:" + e.getCause());
        } finally {
            try {
                if (interProcessSemaphore != null) {
                    interProcessSemaphore.release();
                }
            } catch (Exception e) {
                LOGGER.error("Error caught while realeasing distributed lock", e);
            }
        }
        return true;
    }

    public Boolean updateCURIDVal(String prefixName, Long val) {
        Map<String, String> paraValMap = tableParaValMap.get(prefixName);
        if (paraValMap == null) {
            throw new IllegalStateException("ZKCachedSequenceHandler should be loaded first!");
        }
        paraValMap.put(prefixName + KEY_CUR_NAME, val + "");
        return true;
    }

    /**
     * 获取自增ID
     *
     * @param sequenceEnum
     * @return
     */
    @Override
    public synchronized long nextId(SequenceEnum sequenceEnum) {
        String prefixName = sequenceEnum.getCode();
        Map<String, String> paraMap = this.getParaValMap(prefixName);
        if (null == paraMap) {
            throw new RuntimeException("fetch Param Values error.");
        }
        Long nextId = Long.parseLong(paraMap.get(prefixName + KEY_CUR_NAME)) + 1;
        Long maxId = Long.parseLong(paraMap.get(prefixName + KEY_MAX_NAME));
        if (nextId > maxId) {
            fetchNextPeriod(prefixName);
            return nextId(sequenceEnum);
        }
        updateCURIDVal(prefixName, nextId);
        return nextId.longValue();
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        long startTime = System.currentTimeMillis();   //获取开始时间
        final ZKCachedSequenceHandler sequenceHandler = getInstance();
        sequenceHandler.loadZK();
        new Thread() {
            @Override
            public void run() {
                long startTime2 = System.currentTimeMillis();   //获取开始时间
                for (int i = 0; i < 5000; i++) {
                    System.out.println("线程1 " + sequenceHandler.nextId(SequenceEnum.ACCOUNT));
                }
                long endTime2 = System.currentTimeMillis(); //获取结束时间
                System.out.println("程序运行时间1： " + (endTime2 - startTime2) + "ms");
            }
        }.start();
        for (int i = 0; i < 5000; i++) {
            System.out.println("线程2 " + sequenceHandler.nextId(SequenceEnum.ACCOUNT));
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间2： " + (endTime - startTime) + "ms");
    }
}
