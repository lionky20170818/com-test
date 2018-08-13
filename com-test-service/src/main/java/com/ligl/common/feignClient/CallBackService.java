package com.ligl.common.feignClient;

import feign.Headers;
import feign.RequestLine;

/**
 * 回调接口定义
 * Created by liguoliang on 2017/8/29.
 */
public interface CallBackService {
    /**
     * 兑付结果回调接口
     *
     * @param req
     * @return
     */
    @RequestLine("POST /p2p/order/callback/redemption")
    @Headers("Content-Type: application/json")
    NotifyResp notifyCashResult(NotifyCashResultReq req);
}
