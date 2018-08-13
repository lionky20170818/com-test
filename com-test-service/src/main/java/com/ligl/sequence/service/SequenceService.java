package com.ligl.sequence.service;

/**
 * 功能: 序列服务
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/12 0012 09:33
 * 版本: V1.0
 */
public interface SequenceService {

    /**
     * 查询下一序列
     *
     * @param seqName 序列名称
     * @return 序列值
     */
    Long selectNext(String seqName);

    /**
     * 查询下一序列,并左补零
     *
     * @param seqName    序列名称
     * @param leftPadLen 补齐长度
     * @return 序列值
     */
    String selectNext(String seqName, int leftPadLen);
}
