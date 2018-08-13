package com.ligl.common.feignClient;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by liguoliang on 2017/9/4.
 */
@Data
public class NotifyResp<T> implements Serializable {
    private String msg;
    private String code;
    T data;
}
