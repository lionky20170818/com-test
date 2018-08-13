package com.ligl.trans.service.impl;

import com.ligl.common.enums.PayChannelEnum;
import com.ligl.common.enums.TopicNameEnum;
import com.ligl.common.model.CoreResult;
import com.ligl.queue.mns.MnsTopicPublisher;
import com.ligl.sequence.service.SequenceService;
import com.ligl.trans.check.TradeParameterCheck;
import com.ligl.trans.dal.dao.UserOperationRecordDAO;
import com.ligl.trans.dal.entity.UserOperationRecord;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;
import com.ligl.trans.service.RechargeService;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ClassName: RechargeServiceImpl <br/>
 * Function: 充值服务service接口实现类. <br/>
 * Date: 2017年6月7日 下午5:17:05 <br/>
 *
 * @author liguoliang
 * @since JDK 1.7
 */
@Service("rechargeServiceImpl")
@SuppressWarnings("unchecked")
public class RechargeServiceImpl implements RechargeService {

    private final Logger logger = LoggerFactory.getLogger(RechargeServiceImpl.class);

    @Autowired
    private TradeParameterCheck tradeParamCheck;
//    @Autowired
//    private CoreFxioService coreFxioService;
    @Autowired
    SequenceService sequenceService;
	@Autowired
	UserOperationRecordDAO UserOperationRecordDao;
	@Autowired
	private MnsTopicPublisher publisher;
    
    /**
	 *【充值】预处理 
	 */
	@Override
	public CoreResult<RechargeRspDTO> preRecharge(RechargeReqDTO req) {

//		//获取mycat唯一ID
//		Long id = sequenceService.selectNext("TEST20170824");
//		System.out.println("id====="+id);

		// Returns results by default
		CoreResult<RechargeRspDTO> coreResult = null;
		//dto复制
		UserOperationRecord queryPara = new UserOperationRecord();
		try {
			PropertyUtils.copyProperties(req, queryPara);
		} catch (Exception e) {
			coreResult.fail();
		}

		// Parameter check
        coreResult = tradeParamCheck.checkRecharge(req);
        if (!CoreResult.IsSucc(coreResult)) {
            return coreResult;
        }
		// Data save
		UserOperationRecordDao.insert(queryPara);
        
        // Type judgment
        if (PayChannelEnum.QUICK.getCode().equals(req.getPayChannel())){
        	coreResult = publishMsg(req);//自己发消息给自己
        } else if (PayChannelEnum.B2C.getCode().equals(req.getPayChannel()) || PayChannelEnum.B2B.getCode().equals(req.getPayChannel())){
//        	CoreResult<AcctTransRspDTO> acctResult = acctRegist(req);
        	CoreResult<RechargeRspDTO> acctResult = null;
    		if (!CoreResult.IsSucc(acctResult)){
    			return CoreResult.fail(acctResult.getRspMsg());
    		}
//    		coreResult = coreFxioService.cyberBankRecharge(req);
    		coreResult = null;
		} else{
			coreResult = CoreResult.fail();
		}
        
        // Return result
		return coreResult;
	}

	/**
	 * 消息入队列
	 * @param req
	 * @return
	 */
	protected CoreResult<RechargeRspDTO> publishMsg(RechargeReqDTO req) {
		CoreResult<RechargeRspDTO> coreResult = CoreResult.fail();
		String messageId = publisher.publishMessage(TopicNameEnum.RECHARGE, req);
		if(messageId != null){
			coreResult = CoreResult.process();
		}
		return coreResult;
	}

}
