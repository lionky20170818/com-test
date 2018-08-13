package com.ligl.queue.mns;

import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudQueue;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.common.ClientException;
import com.aliyun.mns.common.ServiceException;
import com.aliyun.mns.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 功能: MNS消息接收线程
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/26 0026 16:07
 * 版本: V1.0
 */
public class MnsTopicThread extends Thread {

    /**
     * 日志器
     */
    private static Logger logger = LoggerFactory.getLogger(MnsTopicThread.class);

    /**
     * 阿里云授权信息
     */
    private CloudAccount cloudAccount;

    /**
     * 无消息时等待时间,默认5秒
     */
    private int waitSeconds = 5;

    /**
     * 监听队列名称
     */
    private String queueName;

    /**
     * 消息监听处理器
     */
    private MnsTopicListener mnsTopicListener;

    /**
     * 是否运行
     */
    private boolean runnable = true;

    public MnsTopicThread() {

    }

    public MnsTopicThread(CloudAccount cloudAccount, int waitSeconds,
                          String queueName, MnsTopicListener mnsTopicListener) {
        this.cloudAccount = cloudAccount;
        this.waitSeconds = waitSeconds;
        this.queueName = queueName;
        this.mnsTopicListener = mnsTopicListener;
    }

    @Override
    public void run() {
        logger.debug("正在监听:" + this.queueName);
        MNSClient client = null;
        try {
            client = cloudAccount.getMNSClient();
            CloudQueue queue = client.getQueueRef(this.queueName);
            while (this.runnable) {
                try {
                    Message popMsg = queue.popMessage(this.waitSeconds);
                    if (popMsg != null) {
                        //logger.debug(popMsg.getMessageBodyAsRawString());
                        String jsonStr = popMsg.getMessageBodyAsRawString();
                        logger.info("队列{}接收消息{}", this.queueName, jsonStr);
                        //处理消息
                        if (mnsTopicListener.consume(jsonStr) == Action.CommitMessage) {
                            for (int i = 0; i < 10; i++) {
                                try {
                                    queue.deleteMessage(popMsg.getReceiptHandle());
                                    break;
                                } catch (Exception ne) {
                                    logger.error("删除队列消息失败", ne);
                                    Thread.sleep(300);
                                }
                            }
                        }
                    }
                } catch (ClientException ce) {
                    logger.error("客户端异常,请检查网络设置和DNS有效性", ce);
                    Thread.sleep(this.waitSeconds * 1000);
                } catch (ServiceException se) {
                    logger.error("服务端异常,requestId:" + se.getRequestId(), se);
                    if (se.getErrorCode() != null) {
                        if (se.getErrorCode().equals("QueueNotExist")) {
                            logger.error(this.queueName + "队列不存在");
                        } else if (se.getErrorCode().equals("TimeExpired")) {
                            logger.error("请求过期,请检测本机服务器时间设置");
                        }
                    }
                    Thread.sleep(this.waitSeconds * 1000);
                } catch (Exception ne) {
                    logger.error("阿里云队列消费者出现异常", ne);
                    Thread.sleep(this.waitSeconds * 1000);
                }
            }
            logger.info("已停止:" + this.queueName);
        } catch (Exception e) {
            logger.error("连接阿里云MNS服务异常", e);
        }
    }

    public void shutdown() {
        logger.info("尝试停止" + this.queueName + "队列消费者");
        if (this.runnable) {
            this.runnable = false;
            //防止出现memory leak
            try {
                Thread.sleep(this.waitSeconds * 1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("已停止:" + this.queueName);
        }
    }

    public CloudAccount getCloudAccount() {
        return cloudAccount;
    }

    public void setCloudAccount(CloudAccount cloudAccount) {
        this.cloudAccount = cloudAccount;
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public MnsTopicListener getMnsTopicListener() {
        return mnsTopicListener;
    }

    public void setMnsTopicListener(MnsTopicListener mnsTopicListener) {
        this.mnsTopicListener = mnsTopicListener;
    }

    public boolean isRunnable() {
        return runnable;
    }

    public void setRunnable(boolean runnable) {
        this.runnable = runnable;
    }
}
