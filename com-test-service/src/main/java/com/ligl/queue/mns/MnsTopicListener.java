package com.ligl.queue.mns;

/**
 * 功能: MNS主题消息监听器
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/26 0026 15:57
 * 版本: V1.0
 */
public interface MnsTopicListener {

    /**
     * 消费消息接口，由应用来实现<br>
     *
     * @param jsonStr 消息
     * @return 消费结果，如果应用抛出异常或者返回Null等价于返回Action.ReconsumeLater
     */
    Action consume(final String jsonStr) throws Exception;
}
