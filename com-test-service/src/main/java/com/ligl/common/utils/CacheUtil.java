/*
 * Copyright (c) 2017, 资邦金服（上海）网络科技有限公司. All Rights Reserved.
 *
 *
 *
 */
package com.ligl.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.ligl.trans.dal.dao.UserOperationRecordDAO;
import com.ligl.trans.dal.entity.UserOperationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: CacheUtil <br/>
 * Function: 缓存工具类. <br/>
 * Date: 2017年8月23日 下午2:46:33 <br/>
 *
 * @author liguoliang
 * @since JDK 1.7
 */
@Component
public class CacheUtil {

    private static final Logger logger = LoggerFactory.getLogger(CacheUtil.class);

    @Autowired
    UserOperationRecordDAO userOperationRecordDao;

    /**
     * SERVICE_CONFIG_MAP: 服务请求配置.
     */
    public static final Map<String, UserOperationRecord> SERVICE_CONFIG_MAP = new HashMap<String, UserOperationRecord>();

    /**
     * INNER_CODE_MAP: 详细string.
     */
    public static final Map<String, String> SERVICE_STRING_MAP = new HashMap<String, String>();


    @PostConstruct
    public void init() {
        refreshCache();
    }

    public void refreshCache() {
        logger.info("本地缓存开始加载.....");

        // 服务配置加载
        List<UserOperationRecord> serviceList = userOperationRecordDao.listByPayService();
        if (serviceList != null) {
            for (UserOperationRecord recordPara : serviceList) {
                SERVICE_CONFIG_MAP.put(recordPara.getAccountNo(), recordPara);
                SERVICE_STRING_MAP.put(recordPara.getAccountNo(), recordPara.getOperationBy());
            }
        }
        logger.info("服务配置 ：{}", JSONObject.toJSONString(serviceList));

        logger.info("本地缓存加载完成.....");
    }

}
