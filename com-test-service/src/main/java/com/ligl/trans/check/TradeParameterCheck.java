package com.ligl.trans.check;

import com.ligl.common.enums.PayChannelEnum;
import com.ligl.common.enums.PayResultEnum;
import com.ligl.common.model.CoreResult;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;
import com.ligl.trans.query.dto.QueryRechargeReqDTO;
import com.ligl.trans.query.dto.QueryRechargeRspDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * ClassName: TradeParameterCheck <br/>
 * Function: 交易相关入参校验类. <br/>
 * Date: 2017年6月8日 下午4:43:43 <br/>
 *
 * @author liguoliang
 * @since JDK 1.7
 */
@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class TradeParameterCheck {

	 /**
     * 充值交易入参校验
     * @param req
     * @return
     */
    public CoreResult<RechargeRspDTO> checkRecharge(RechargeReqDTO req) {
        if (req == null) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "请求对象不能为空");
        }
        if (StringUtils.isBlank(req.getPayChannel())) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "支付渠道不能为空");
        }
        if (StringUtils.isBlank(req.getCifMemberId())) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "CIF会员ID不能为空");
        }
        if (StringUtils.isBlank(req.getMemberId())) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "商户会员ID不能为空");
        }
        if (StringUtils.isBlank(req.getRefNo())) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "交易流水号不能为空");
        }
        if (req.getTradeAmt() == null) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "交易金额不能为空");
        }
        if (req.getTradeAmt().intValue() == 0) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "交易金额不能为零");
        }
        if (StringUtils.isBlank(req.getSourceId())) {
            return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "系统来源不能为空");
        }
        if(PayChannelEnum.QUICK.getCode().equals(req.getPayChannel())){
        	if (StringUtils.isBlank(req.getMobile())) {
        		return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "手机号不能为空");
        	}
        	if (StringUtils.isBlank(req.getSmsCode())) {
        		return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "短信验证码不能为空");
        	}
        }
        if(PayChannelEnum.B2C.getCode().equals(req.getPayChannel()) || PayChannelEnum.B2B.getCode().equals(req.getPayChannel())){
        	if (StringUtils.isBlank(req.getBankCode())) {
        		return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "银行简码不能为空");
        	}
        }
        return CoreResult.succ();
    }

   /**
    * 充值查询入参校验
    * @param req
    * @return
    */
   public CoreResult<QueryRechargeRspDTO> checkQueryRecharge(QueryRechargeReqDTO req) {
	   if (req == null) {
           return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "请求对象不能为空");
       }
       if (StringUtils.isBlank(req.getRefNo())) {
           return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "交易流水号不能为空");
       }
       if (StringUtils.isBlank(req.getCifMemberId())) {
           return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "CIF会员号不能为空");
       }
       if (StringUtils.isBlank(req.getSourceId())) {
           return CoreResult.build(PayResultEnum.RSP_FAIL.getRspCode(), "系统来源不能为空");
       }
       return CoreResult.succ();
   }

}
