package com.ligl.trans.service;

import com.ligl.common.model.CoreResult;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;

/**
 * ClassName: RechargeService <br/>
 * Function: 充值服务service接口. <br/>
 * Date: 2017年6月7日 下午5:15:40 <br/>
 *
 * @author liguoliang
 * @since JDK 1.7
 */
public interface RechargeService {

	/**
	 * 充值预处理
	 * @return
	 */
	CoreResult<RechargeRspDTO> preRecharge(RechargeReqDTO req);

}
