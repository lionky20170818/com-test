package com.ligl.trans.listener;

import com.alibaba.fastjson.JSON;
import com.ligl.common.enums.PayResultEnum;
import com.ligl.common.model.CoreResult;
import com.ligl.queue.mns.Action;
import com.ligl.queue.mns.MnsTopicListener;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;
import com.ligl.trans.service.RechargeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 功能: 充值消息监听
 * 创建: liguoliang
 * 日期: 2017/6/26 0026 16:48
 * 版本: V1.0
 */
public class RechargeTopicListener implements MnsTopicListener {
	
	private final Logger logger = LoggerFactory.getLogger(RechargeTopicListener.class);
	
	@Autowired
	private RechargeService rechargeService;
	
	@Override
    public Action consume(String jsonStr) throws Exception {
		CoreResult<RechargeRspDTO> coreResult = null;
		try {
			logger.info("===>消费MQ【充值】 start...");
    		JSON json = (JSON) JSON.parse(jsonStr);
            RechargeReqDTO req = JSON.toJavaObject(json, RechargeReqDTO.class);
//            coreResult = rechargeService.doRecharge(req);//真正的充值处理
			if(PayResultEnum.RSP_FAIL.getRspCode().equals(coreResult.getRspCode())){
//            	notifyFront(coreResult);//回调上游系统
			}
            logger.info("===>消费MQ【充值】 end");
	    }catch(Exception e) {
	    	logger.error("消费MQ【充值】异常，稍后将重新发送，参数{}",jsonStr);
	    	return Action.ReconsumeLater;
	    }
        return Action.CommitMessage;
    }

	
}
