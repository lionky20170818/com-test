package com.ligl.guavaTest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;
import com.ligl.AbstractJunitTest;
import com.ligl.trans.dto.req.RechargeReqDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Function:guavaTest学习
 * Author: created by liguoliang
 * Date: 2017/11/8 0008 上午 10:34
 * Version: 1.0
 */
@Slf4j
public class BaseTest extends AbstractJunitTest {

    @Test
    public void test() {
        List<RechargeReqDTO> contractEntities = Lists.newArrayList();
        for (int i = 0; i < 100; i++) {
            RechargeReqDTO contractEntity = new RechargeReqDTO();
            contractEntity.setRefNo("999999AAAA");
            contractEntities.add(contractEntity);
        }
        //guava将list切分
        List<List<RechargeReqDTO>> batchList = Lists.partition(contractEntities, 10);
        log.info("batchList==" + batchList.size());


        //new对象的方法
        List<String> list = Lists.newArrayList("alpha", "beta", "gamma");
        Map<String, Student> map = Maps.newLinkedHashMap();




    }


}
