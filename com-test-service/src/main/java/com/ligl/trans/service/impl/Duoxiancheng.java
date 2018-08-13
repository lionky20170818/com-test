package com.ligl.trans.service.impl;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Function:继承Thread或实现Runnable接口或多线程创建第三种方式：Future和Callable
 * Author: created by liguoliang
 * Date: 2017/10/25 0025 下午 2:07
 * Version: 1.0
 */
public class Duoxiancheng {

    @Resource(name = "ThreadExecutor")
    private ScheduledExecutorService taskExecutor;
    //    ExecutorService ser = Executors.newFixedThreadPool(2);

    public void testThread() {
        //    List<LadderBatchEntity> ladderBatchList = ladderBatchDao.listInitBatchByBizCode(BizCodeEnum.INVEST_REQUEST.getCode(), LadderBatchStatusEnum.CONFIRM_SUCCESS.getCode());
        List<String> strList = new ArrayList<>();
        if (strList != null && strList.size() > 0) {
            List<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
            for (final String str : strList) {
                futureList.add(taskExecutor.submit(new Callable<Integer>() {
                    @Override
                    public Integer call() {
                        //                    process(ladderBatchEntity);
                        return 1;
                    }
                }));
            }
            for (Future<Integer> ft : futureList) {
                try {
                    Integer i = ft.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {

    }
}
