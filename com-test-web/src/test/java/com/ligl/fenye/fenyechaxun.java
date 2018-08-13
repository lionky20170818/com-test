package com.ligl.fenye;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.ligl.AbstractJunitTest;
import com.ligl.trans.dal.dao.UserOperationRecordDAO;
import com.ligl.trans.dal.entity.UserOperationRecord;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/12/6 0006 下午 4:28
 * Version: 1.0
 */
@Slf4j
public class fenyechaxun  extends AbstractJunitTest {

    @Autowired
    UserOperationRecordDAO userOperationRecordDAO;

    @Test
    public void test(){

        String s = "fs123fdsa";//String变量

        byte b[] = s.getBytes();//String转换为byte[]

        System.out.println("b");
        PageBounds pageBounds = new PageBounds(1, 10);
        PageList<UserOperationRecord> pageList = userOperationRecordDAO.listByPayServicePage("20170101", pageBounds);
        log.info("==="+pageList);
    }

    @Test
    public void testWhile(){
        PageBounds pageBounds = new PageBounds(1, 10);
        PageList<UserOperationRecord> pageList = userOperationRecordDAO.listByPayServicePage("20170101", pageBounds);
        while (!pageList.isEmpty()) {
            System.out.println("do something = " + pageList);
            pageList = userOperationRecordDAO.listByPayServicePage("20170101", pageBounds);
//            investEntityList=investDAO.listInvestByCashFlagSize(batchEntity.getProductCode(),1000);1000条执行一次
        }
        log.info("==="+pageList);
    }
}
