package com.ligl.common.utils;

import java.math.BigDecimal;

/**
 * Created by liguoliang on 2017/3/14.
 * BigDecimal工具类
 */
public class BigDecimalUtils {
    /**
     * 保留4位小数,舍去多余的位数
     *
     * @return
     */
    public static BigDecimal round4(BigDecimal arg) {
        if (arg == null) return BigDecimal.ZERO;
        return arg.setScale(4, BigDecimal.ROUND_DOWN);
    }

    /**
     * 金额转换
     *
     * @param arg
     * @return
     */
    public static Long convertAmt(BigDecimal arg) {
        if (arg == null)
            return Long.valueOf(0);
        else
            return Long.valueOf(arg.multiply(new BigDecimal(100)).longValue());
    }

    /**
     * 金额转换
     *
     * @param arg
     * @return
     */
    public static BigDecimal convertAmt(Long arg) {
        if (arg == null)
            return BigDecimal.ZERO;
        else
            return BigDecimal.valueOf(arg / 100);
    }
}
