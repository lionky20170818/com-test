package com.ligl;

import com.google.common.collect.Lists;
import com.ligl.common.model.CoreResult;
import com.ligl.common.sms.SMSClient;
import com.ligl.trans.dto.req.RechargeReqDTO;
import com.ligl.trans.dto.rsp.RechargeRspDTO;
import com.ligl.trans.facade.TradeServiceFacade;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * ClassName: CoreFxioServiceTest <br/>
 * Function: 充值接口单元测试. <br/>
 * Date: 2017年6月30日 上午10:49:26 <br/>
 *
 * @author liguoliang
 * @since JDK 1.7
 */
@Slf4j
public class RechargeTest extends AbstractJunitTest {

    @Autowired
    private TradeServiceFacade tradeServiceFacade;

    String baseStr = "abc";
    /**
     * baseTest
     */
    @Test
    public void baseTest() {
        BigDecimal and = new BigDecimal("0.1199");
        BigDecimal loanTotalAmount = and.setScale(2, BigDecimal.ROUND_DOWN);

        for (int i = 0; i < 5; i++) {
            if ("abc".equals(baseStr)) {
                System.out.println("不执行==");
                baseStr = "def";
            } else {
                System.out.println("执行==");
            }

        }
    }

    /**
     * 网银充值
     */
    @Test
    public void webRechargeTest() {
        try {
            List<RechargeReqDTO> contractEntities = Lists.newArrayList();
            for (int i = 0; i < 100; i++) {
                RechargeReqDTO contractEntity = new RechargeReqDTO();
                contractEntity.setRefNo("999999AAAA");
                contractEntities.add(contractEntity);
            }
            //guava将list切分
            List<List<RechargeReqDTO>> batchList = Lists.partition(contractEntities, 10);
            for (List<RechargeReqDTO> contractEntityList : batchList) {
//                <update id="updateFrozenAmountForBatchById" parameterType="java.util.List">
//                        update p2p_reservation_order
//                set frozen_amount = CASE id
//                        <foreach collection="list" item="item">
//                        WHEN #{item.id} THEN #{item.frozenAmount}
//                </foreach>
//                        END,
//                        reservation_status = CASE id
//                        <foreach collection="list" item="item">
//                        WHEN #{item.id} THEN #{item.reservationStatus}
//                </foreach>
//                        END
//                WHERE id IN
//                        <foreach collection="list" item="item" open="(" separator="," close=")">
//                                #{item.id}
//                </foreach>
//                </update>
                System.out.println("11====");
            }
            RechargeReqDTO req = new RechargeReqDTO();
            req.setCifMemberId("1498187626598");//CIF会员ID
            req.setMemberId("1498187626598");//商户会员号
            req.setCardNo("6225801223710000");//银行卡号
            req.setBankCode("ABC");//银行简码
            req.setRefNo(System.currentTimeMillis() + "");//交易流水号
            req.setTradeAmt(new BigDecimal(1.00));//交易金额
            req.setTradeTime(new Date());//交易时间
            req.setPayChannel("B2C");//支付渠道  QUICK 快捷, B2C 个人网银, B2B 企业网银
            req.setSourceId("YW");//系统来源
            req.setPageReturnUrl("www.baidu.com");//页面返回地址
            CoreResult<RechargeRspDTO> coreResult = tradeServiceFacade.usrRecharge(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 快捷充值
     */
    @Test
    public void quickRechargeTest() {
        try {
            RechargeReqDTO req = new RechargeReqDTO();
            req.setCifMemberId("1498187626598");//CIF会员ID
            req.setMemberId("1498187626598");//商户会员号
            req.setCardNo("6225801223710000");//银行卡号
            req.setRefNo(System.currentTimeMillis() + "");//交易流水号
            req.setMobile("13052170396");//手机号
            req.setSmsCode("666666");//短信验证码
            req.setTradeAmt(new BigDecimal(1.00));//交易金额
            req.setTradeTime(new Date());//交易时间
            req.setPayChannel("QUICK");//支付渠道  QUICK 快捷, B2C 个人网银, B2B 企业网银
            req.setSourceId("YW");//系统来源
            CoreResult<RechargeRspDTO> coreResult = tradeServiceFacade.usrRecharge(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void sendNotifyMsg(){
        String phone="18301892719";
        String content="亲爱的会员，您投资的xxx产品已成功回款，投资金额yyy元已回到您的银行卡，请注意查收。";
        log.info("短信结果"+ SMSClient.sendNotifyMsg(phone,content));

    }
}
