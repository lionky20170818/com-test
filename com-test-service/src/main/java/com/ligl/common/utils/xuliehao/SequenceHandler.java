package com.ligl.common.utils.xuliehao;

import com.ligl.common.utils.DateUtils;

/**
 * Created by liguoliang on 2017/9/5.
 */
public abstract class SequenceHandler {

    /**
     * 获取sequence
     *
     * @param sequenceEnum
     * @return
     */
    public abstract long nextId(SequenceEnum sequenceEnum);


    /**
     * 生成业务编码   使用ZK，
     * 另一种使用redis自增 Jedis.incr(key)
     *
     * @param sequenceEnum
     * @return
     */
    public String getBusinessCode(SequenceEnum sequenceEnum, String channel) {
        String businessCode = null;
        String currentDate = DateUtils.format(DateUtils.now(), DateUtils.ymdhms2);
        //序列名称,前缀加年月 AMA + 1701
        long sequence = this.nextId(sequenceEnum);
        StringBuffer sb = new StringBuffer();
        sb.append(channel).append(sequenceEnum.getCode()).append(currentDate).append(String.format("%09d", sequence));
        businessCode = sb.toString();
        return businessCode;
    }
}
