package com.ligl.common.feignClient;

import lombok.Data;

/**
 * Created by liguoliang on 2017/9/20.
 */
@Data
public class NotifyCashResultReq {
    String memberId;//会员id
    String productCode;//产品编码
    String amount;//兑付本金
    String profit;//兑付收益
    String serialNo;//兑付流失号
}
