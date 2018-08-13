package com.ligl.common.mapJishu;

import lombok.Data;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/9/1 0001 下午 5:49
 * Version: 1.0
 */
@Data
public class MutableInteger {
    int value;

    public MutableInteger(int val) {
        this.value = val;
    }
}
