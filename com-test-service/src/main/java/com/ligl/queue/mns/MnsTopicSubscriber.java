package com.ligl.queue.mns;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.MNSClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 功能: MNS服务主题订阅者
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/26 0026 15:33
 * 版本: V1.0
 */
public class MnsTopicSubscriber {

    /**
     * 日志器
     */
    private static Logger logger = LoggerFactory.getLogger(MnsTopicSubscriber.class);

    /**
     * 阿里云授权信息
     */
    private CloudAccount cloudAccount;

    /**
     * 无消息时等待时间,默认5秒
     */
    private int waitSeconds = 5;

    /**
     * 消息接收线程池
     */
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     * 消息处理器映射关系
     */
    private Map<String, MnsTopicListener> topicListenerMap;

    /**
     * 消息处理线程列表
     */
    private List<MnsTopicThread> topicThreadList = new ArrayList<>();

    public void startup() {
        logger.info("MnsTopicSubscriber正在启动");
        for (String key : topicListenerMap.keySet()) {
            MnsTopicThread thread = new MnsTopicThread(this.cloudAccount, this.waitSeconds,
                    key, topicListenerMap.get(key));
            topicThreadList.add(thread);
            taskExecutor.execute(thread);
        }
        logger.info("MnsTopicSubscriber启动完毕");
    }

    public void shutdown() {
        logger.info("MnsTopicSubscriber正在停止");
        for (MnsTopicThread thread : this.topicThreadList) {
            thread.shutdown();
        }
        try {
            Thread.sleep(this.waitSeconds * this.topicThreadList.size() * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            MNSClient client = cloudAccount.getMNSClient();
            if (client != null) {
                client.close();
            }
        }
        logger.info("MnsTopicSubscriber停止完毕");
    }

    public CloudAccount getCloudAccount() {
        return cloudAccount;
    }

    public void setCloudAccount(CloudAccount cloudAccount) {
        this.cloudAccount = cloudAccount;
    }

    public ThreadPoolTaskExecutor getTaskExecutor() {
        return taskExecutor;
    }

    public void setTaskExecutor(ThreadPoolTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public Map<String, MnsTopicListener> getTopicListenerMap() {
        return topicListenerMap;
    }

    public void setTopicListenerMap(Map<String, MnsTopicListener> topicListenerMap) {
        this.topicListenerMap = topicListenerMap;
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }
}
