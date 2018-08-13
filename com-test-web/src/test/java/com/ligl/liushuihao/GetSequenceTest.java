package com.ligl.liushuihao;

import com.ligl.AbstractJunitTest;
import com.ligl.common.redis.RedisSequence;
import com.ligl.common.utils.xuliehao.SequenceEnum;
import com.ligl.common.utils.xuliehao.ZKIncreaseSequenceHandler;
import com.ligl.sequence.service.SequenceService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;

/**
 * Function:生成ID流水号
 * Author: created by liguoliang
 * Date: 2017/9/27 0027 下午 1:56
 * Version: 1.0
 */
public class GetSequenceTest extends AbstractJunitTest {
    ZKIncreaseSequenceHandler sequenceHandler;
    @Value("${id.genrenate.zkAddress}")
    String zkAddress;
    @Value("${id.genrenate.path}")
    String path;
    @Value("${id.genrenate.seq}")
    String seq;

    @Autowired
    private RedisSequence redisSequence;
    @Autowired
    private SequenceService sequenceService;

    @PostConstruct
    public void init() {
        sequenceHandler = ZKIncreaseSequenceHandler.getInstance(zkAddress, path, seq);
    }

    @Test
    public void testZK() {
        //ZK方式
        String sequence = sequenceHandler.getBusinessCode(SequenceEnum.ACCOUNT, "NIHAO");
        System.out.println("===" + sequence);
        //redis自增
        for (int i = 0; i < 50; i++) {
            long sequence1 = redisSequence.incr("seq");
            System.out.println("=xxxx==" + sequence1);
        }
    }

    @Test
    public void testMYCAT() {
        //redis自增
        for (int i = 0; i < 50; i++) {
            long startTime = System.currentTimeMillis();
            Long seq = sequenceService.selectNext(SequenceEnum.ACCOUNT.getCode());
            long endTime = System.currentTimeMillis();
            System.out.println("=======" + (endTime - startTime));
        }
    }

}
