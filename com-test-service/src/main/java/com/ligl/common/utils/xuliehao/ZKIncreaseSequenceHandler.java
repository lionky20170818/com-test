package com.ligl.common.utils.xuliehao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 基于zk的永久型自增节点PERSISTENT_SEQUENTIAL实现
 * 每次生成节点后会使用线程池执行删除节点任务,以减小zk的负担
 * Created by wangwanbin on 2017/9/5.
 */
@Slf4j
public class ZKIncreaseSequenceHandler extends SequenceHandler implements PooledObjectFactory<CuratorFramework> {
    private static ZKIncreaseSequenceHandler instance = new ZKIncreaseSequenceHandler();
    private static ExecutorService fixedThreadPool = Executors.newSingleThreadExecutor();
    private GenericObjectPool genericObjectPool;
    private static final Map<String, Queue<Long>> preNodeMap = new HashMap<>();
    private static String ZK_ADDRESS = ""; //192.168.0.65
    private static String PATH = "";//  /sequence/p2p
    private static String SEQ = "";//seq;

    /**
     * 私有化构造方法,单例模式
     */
    private ZKIncreaseSequenceHandler() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(4);
        genericObjectPool = new GenericObjectPool(this, config);
    }

    /**
     * 获取sequence工具对象的唯一方法
     *
     * @return
     */
    public static ZKIncreaseSequenceHandler getInstance(String zkAddress, String path, String seq) {
        ZK_ADDRESS = zkAddress;
        PATH = path;
        SEQ = seq;
        //实例化待删除Map
        for (SequenceEnum sequenceEnum : SequenceEnum.values()) {
            Queue<Long> queue = new ConcurrentLinkedQueue<>();
            preNodeMap.put(sequenceEnum.getCode(), queue);
        }
        return instance;
    }

    @Override
    public long nextId(final SequenceEnum sequenceEnum) {
        String result = createNode(sequenceEnum.getCode());
        final String idstr = result.substring((PATH + "/" + sequenceEnum.getCode() + "/" + SEQ).length());
        final long id = Long.parseLong(idstr);
        final Queue<Long> preNodeQueue = preNodeMap.get(sequenceEnum.getCode());//获取对应的待删除队列
        preNodeQueue.add(id);
        //删除上一个节点
        fixedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                Iterator<Long> iterator = preNodeQueue.iterator();
                if (iterator.hasNext()) {
                    long preNode = iterator.next();
                    if (preNode < id) {
                        final String format = "%0" + idstr.length() + "d";
                        String preIdstr = String.format(format, preNode);
                        final String prePath = PATH + "/" + sequenceEnum.getCode() + "/" + SEQ + preIdstr;
                        CuratorFramework client = null;
                        try {
                            client = (CuratorFramework) genericObjectPool.borrowObject();
                            client.delete().forPath(prePath);
                            log.debug("删除" + prePath);
                            preNodeQueue.remove(preNode);
                        } catch (Exception e) {
                            log.error("delete preNode error", e);
                        } finally {
                            if (client != null)
//                                对象池：https://segmentfault.com/a/1190000006889810
                                genericObjectPool.returnObject(client);
                        }
                    }
                }
            }
        });
        return id;
    }


    private String createNode(String prefixName) {
        CuratorFramework client = null;
        try {
            client = (CuratorFramework) genericObjectPool.borrowObject();
            String result = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(PATH + "/" + prefixName + "/" + SEQ, String.valueOf(0).getBytes());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("create zookeeper node error", e);
        } finally {
            if (client != null)
                genericObjectPool.returnObject(client);
        }
    }

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        long startTime = System.currentTimeMillis();   //获取开始时间
        final ZKIncreaseSequenceHandler sequenceHandler = ZKIncreaseSequenceHandler.getInstance("192.168.0.65", "/sequence/p2p", "seq");
        int count = 10;
        final CountDownLatch cd = new CountDownLatch(count);
        for (int i = 0; i < count; i++) {
            executorService.execute(new Runnable() {
                public void run() {
                    System.out.printf("线程 %s %d \n", Thread.currentThread().getId(), sequenceHandler.nextId(SequenceEnum.ACCOUNT));
                    cd.countDown();
                }
            });
            executorService.execute(new Runnable() {
                public void run() {
                    System.out.printf("线程 %s %d \n", Thread.currentThread().getId(), sequenceHandler.nextId(SequenceEnum.ACCOUNT));
                    cd.countDown();
                }
            });
        }
        try {
            cd.await();
        } catch (InterruptedException e) {
            log.error("Interrupted thread", e);
            Thread.currentThread().interrupt();
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");

    }

    @Override
    public PooledObject<CuratorFramework> makeObject() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.newClient(ZK_ADDRESS, new ExponentialBackoffRetry(1000, 3));
        client.start();
        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(PooledObject<CuratorFramework> p) throws Exception {

    }

    @Override
    public boolean validateObject(PooledObject<CuratorFramework> p) {
        return false;
    }

    @Override
    public void activateObject(PooledObject<CuratorFramework> p) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<CuratorFramework> p) throws Exception {

    }
}
