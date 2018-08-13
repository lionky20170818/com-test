package com.ligl.controller;

import com.alibaba.fastjson.JSON;
import com.ligl.common.exception.BusinessException;
import com.ligl.common.model.CoreResult;
import com.ligl.common.utils.CacheUtil;
import com.ligl.trans.dal.entity.UserOperationRecord;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;
import com.ligl.trans.facade.TradeServiceFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by liguoliang on 2017/6/9.
 */
@RestController
@RequestMapping("/trade")
public class TradeController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TradeServiceFacade tradeServiceFacade;

	@ResponseBody
	@RequestMapping(value="/recharge",method= RequestMethod.POST)
	public CoreResult<RechargeRspDTO> invest(@RequestBody RechargeReqDTO req){
		logger.info("TradeController.recharge.req:" + JSON.toJSONString(req));

		//本地缓存测试start
		UserOperationRecord recordResult = CacheUtil.SERVICE_CONFIG_MAP.get("FAST0001");
		String stringResult = CacheUtil.SERVICE_STRING_MAP.get("q");
		//本地缓存测试end

		CoreResult<RechargeRspDTO> coreResult =  CoreResult.succ();
		try {
			coreResult = tradeServiceFacade.usrRecharge(req);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			if (e instanceof BusinessException) {
				BusinessException be = (BusinessException) e;
				coreResult = CoreResult.build(be.getCode(), be.getMessage());
			} else {
				coreResult = CoreResult.fail();
			}
		} finally {
			logger.info("TradeController.recharge.resp:" + JSON.toJSONString(coreResult));
		}

		return coreResult;
	}

}
