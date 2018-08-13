package com.ligl.queue.mns;

import com.alibaba.fastjson.JSON;
import com.aliyun.mns.client.CloudAccount;
import com.aliyun.mns.client.CloudTopic;
import com.aliyun.mns.client.MNSClient;
import com.aliyun.mns.model.RawTopicMessage;
import com.aliyun.mns.model.TopicMessage;
import com.ligl.common.enums.TopicNameEnum;
import com.ligl.common.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 功能: MNS服务主题发布者
 * 创建: liguoliang - liguoliang
 * 日期: 2017/3/9 0009 10:14
 * 版本: V1.0
 */
public class MnsTopicPublisher {

    /**
     * 日志器
     */
    private static Logger logger = LoggerFactory.getLogger(MnsTopicPublisher.class);

    /**
     * 阿里云授权信息
     */
    private CloudAccount cloudAccount;

    /**
     * 主题名称列表
     */
    private Map<String, String> topicNameMap;

    /**
     * 发送主题消息
     *
     * @param topicNameEnum 消息主题名称枚举
     * @param message       消息体
     * @return 消息唯一标识
     */
    public <T> String publishMessage(TopicNameEnum topicNameEnum, T message) {
        MNSClient client = null;
        try {
            String topicName = topicNameMap.get(topicNameEnum.getCode());
            if (StringUtils.isBlank(topicName)) {
                throw new BusinessException("未获取到主题名称");
            }
            client = cloudAccount.getMNSClient();
            CloudTopic cloudTopic = client.getTopicRef(topicName);
            TopicMessage topicMessage = new RawTopicMessage();
            topicMessage.setBaseMessageBody(JSON.toJSONString(message));
            topicMessage = cloudTopic.publishMessage(topicMessage);
            return topicMessage.getMessageId();
        } catch (Exception e) {
            logger.error("发布主题消息异常", e);
        }
        return null;
    }

    public CloudAccount getCloudAccount() {
        return cloudAccount;
    }

    public void setCloudAccount(CloudAccount cloudAccount) {
        this.cloudAccount = cloudAccount;
    }

    public Map<String, String> getTopicNameMap() {
        return topicNameMap;
    }

    public void setTopicNameMap(Map<String, String> topicNameMap) {
        this.topicNameMap = topicNameMap;
    }
}
