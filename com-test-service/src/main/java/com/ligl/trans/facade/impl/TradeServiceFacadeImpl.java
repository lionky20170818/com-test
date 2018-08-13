package com.ligl.trans.facade.impl;

import com.alibaba.fastjson.JSON;
import com.ligl.common.model.CoreResult;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;
import com.ligl.trans.facade.TradeServiceFacade;
import com.ligl.trans.service.RechargeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * 功能: 交易接口实现
 * 创建: liguoliang
 * 日期: 2017/06/07
 * 版本: V1.0
 */
@Service
public class TradeServiceFacadeImpl implements TradeServiceFacade {

    @Autowired
    private RechargeService rechargeService;

    private final Logger logger = LoggerFactory.getLogger(TradeServiceFacadeImpl.class);//反射机制获取日志打印

    @Override
    public CoreResult<RechargeRspDTO> usrRecharge(RechargeReqDTO req) {
    	CoreResult<RechargeRspDTO> coreResult = null;
        logger.info("====>用户充值 start...");
        logger.info("请求参数：" + JSON.toJSONString(req));
        try {
            coreResult = rechargeService.preRecharge(req);
        } catch (Exception e) {
            logger.error("充值异常:", e);
            if (e instanceof DuplicateKeyException) {
            	coreResult = CoreResult.fail("重复提交");
            } else {
            	coreResult = CoreResult.fail();
            }
	    }finally{
            logger.info("响应结果：" + JSON.toJSONString(coreResult));
            logger.info("===>用户充值 end");
        }
        return coreResult;
    }
}
