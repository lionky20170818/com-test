package com.ligl.trans.facade;

import com.ligl.common.model.CoreResult;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;

/**
 * 功能: 交易服务接口
 * 创建: liguoliang
 * 日期: 2017/06/06
 * 版本: V1.0
 */
public interface TradeServiceFacade {
	
	/**
	 * 充值
	 * @return
	 */
	CoreResult<RechargeRspDTO> usrRecharge(RechargeReqDTO req);

}
