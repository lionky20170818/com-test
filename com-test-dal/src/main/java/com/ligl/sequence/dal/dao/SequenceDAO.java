package com.ligl.sequence.dal.dao;

/**
 * 功能: 序列器数据访问层
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/12 0012 09:28
 * 版本: V1.0
 */
public interface SequenceDAO {

    /**
     * 查询下一序列
     *
     * @param seqName 序列名称
     * @return 序列值
     */
    Long select(String seqName);
}
