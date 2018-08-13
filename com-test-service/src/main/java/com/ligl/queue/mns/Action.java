package com.ligl.queue.mns;

/**
 * 功能: 消费结果
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/26 0026 16:24
 * 版本: V1.0
 */
public enum Action {

    /**
     * 消费成功，继续消费下一条消息
     */
    CommitMessage,
    /**
     * 消费失败，告知服务器稍后再投递这条消息，继续消费其他消息
     */
    ReconsumeLater,
}
