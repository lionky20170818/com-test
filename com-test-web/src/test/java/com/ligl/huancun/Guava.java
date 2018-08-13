package com.ligl.huancun;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/10/19 0019 下午 4:04
 * Version: 1.0
 * http://blog.csdn.net/michaelwubo/article/details/50865185  缓存的几种形式
 */
public class Guava {

    public static final Long maxSize = Long.valueOf(10000);

    //http://blog.csdn.net/xlgen157387/article/details/47293517
    Cache<String, String> graphs = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .maximumSize(maxSize)
            .build();

//    private LoadingCache<DailyIncomeReq, CommonResp<DailyIncomeDTO>> loadingCache = CacheBuilder.newBuilder()
//            .maximumSize(10000 * 5)
//            .expireAfterWrite(30, TimeUnit.SECONDS)
//            .build(
//                    new CacheLoader<DailyIncomeReq, CommonResp<DailyIncomeDTO>>() {
//                        public CommonResp<DailyIncomeDTO> load(DailyIncomeReq key) throws Exception {
//                            return incomeCalcService.queryIncome(key);
//                        }
//                    });


    @Test
    public void guavaTest() {

        graphs.put("nihao", "20171019");
        graphs.put("nihaoma", "20171019");

        try {
            System.out.println("==1111==" + graphs.get("nihao", new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "wohenhao2018";
                }
            }));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        //个别清除：Cache.invalidate(key)
        //批量清除：Cache.invalidateAll(keys)
        //清除所有缓存项：Cache.invalidateAll()
        graphs.invalidate("nihaoma");

        try {
            System.out.println("==2222==" + graphs.get("nihaoma", new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return "nihaoma2018";
                }
            }));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }


}
