package com.ligl.sequence.service.impl;


import com.ligl.sequence.dal.dao.SequenceDAO;
import com.ligl.sequence.service.SequenceService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 功能: 序列服务
 * 创建: liguoliang - liguoliang
 * 日期: 2017/6/12 0012 09:34
 * 版本: V1.0
 */
@Service
public class SequenceServiceImpl implements SequenceService {

    @Autowired
    private SequenceDAO sequenceDAO;

    /**
     * 查询下一序列
     *
     * @param seqName 序列名称
     * @return 序列值
     */
    @Override
    public Long selectNext(String seqName) {
        return sequenceDAO.select(seqName);
    }

    /**
     * 查询下一序列,并左补零
     *
     * @param seqName    序列名称
     * @param leftPadLen 补齐长度
     * @return 序列值
     */
    @Override
    public String selectNext(String seqName, int leftPadLen) {
        return StringUtils.leftPad(String.valueOf(selectNext(seqName)), leftPadLen, '0');
    }
}
