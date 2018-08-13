/*
 * Copyright (c) 2017, 资邦金服（上海）网络科技有限公司. All Rights Reserved.
 *
 */
package com.ligl.common.sms;


/**
 * ClassName: CommonConstants <br/>
 * Function: 公用常量.  <br/>
 * Date: 2017年9月23日 下午1:30:56 <br/>
 *
		 * @author mengkai@zb.com
 * @version
 * @since JDK 1.7
		*/
public class CommonConstants {

	private CommonConstants(){

	}

	/** 交易系统操作员*/
	public static final String TRADE_OPERATE_SYSTEM = "system";

	public static final String DEFAULT_ENCODING = "UTF-8";


    /**
     * 一次取出处理投资请求数量
     */
	public static final int INVEST_SIZE = 1000;
	/**
	 * 一次处理每日收益数量
	 */
	public static final int INCOME_SIZE = 1000;
	/**
	 * 一次处理兑付计划数量
	 */
	public static final int CASHPLAN_SIZE = 1000;
	/**
	 *  已生成兑付计划，每日收益标志
	 */
	public static final String FLAG_TRUE="TRUE";
	/**
	 *  未生成兑付计划，每日收益标志
	 */
	public static final String FLAG_FALSE="FALSE";

    /**
     * 放款标志
     */
	public static final String LOAN_FLAG_ERROR="REEOR";

	public static final String LENDING_SNO="LENDINGSNO";

	public static final String MSG="亲爱的会员，您投资的xxx产品已成功回款，投资金额yyy元已回到您的银行卡，请注意查收。";

	public static final int RETRY_TIMES= 5;

	public static final int MEMO_200 = 200;

	public static final int MEMO_500 = 500;

	public static final String PRODUCTCODE_IS_NULL="产品编码不能为空";

	public static final String PRODUCTNAME_IS_NULL="产品名称不能为空";

	public static final String SALECHANNEL_IS_NULL ="渠道不能为空";

	public static final String SALECHANNEL_IS_ERROR="渠道不合法";

	public static final String AMS_RESP_CODE = "0000";

	public static final String PAY_RESP_CODE = "0000";

	public static final String PAY_RESP_3T05="3T05";

	public static final String PMS_RESP_CODE = "0000";

	public static final String TXS_RESP_CODE = "0000";

    public static final String MEMBER_RESP_CODE = "0000";
	
	public static final String ZK_LOCK_KEY_GENERATE_CONTRACT = "generate_contract";
	
	
}
