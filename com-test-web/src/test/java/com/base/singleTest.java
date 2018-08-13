package com.base;

import com.ligl.common.utils.xuliehao.SequenceEnum;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/12/25 0025 下午 2:59
 * Version: 1.0
 */
public class singleTest {
    public static void main(String[] args) {

        String abd = "20170102";
        String abc = "20170102";
        if (abd.compareTo(abc)>0 || abd.compareTo(abc)==0) {
            System.out.println("222====");
        } else {
            System.out.println("1111====");

        }

        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("语文", 1);
        map.put("数学", 2);
        map.put("英语", 3);
        map.put("历史", 4);
        map.put("政治", 5);
        map.put("地理", 6);
        map.put("生物", 7);
        map.put("化学", 8);
        map.put("生物", 7);
        for(Map.Entry<String, Integer> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }

        List abcList = new ArrayList<>(Arrays.asList("1","2","3"));
        abcList.add(1);
        abcList.add("1a2b");

        List synList = Collections.synchronizedList(new ArrayList<>());
        synList.add("nihao1123");
        synList.add("1876");
        synList.add("");
        synList.remove(2);
        synList.add("123");
        synList.add("123");
        synList.add("acvb");

        ConcurrentHashMap ccHash = new ConcurrentHashMap();
        ccHash.put("abc","123");
        ccHash.remove("abc");

        //Hashtable阻塞测试
        final Hashtable<String,Integer> table = new Hashtable<String,Integer>();
        table.put("a",1);
        table.put("b",2);
        ExecutorService executor = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executor.execute(new Runnable() {
                public void run() {
                    table.put("c",3);
                    System.out.println("线程123="+Thread.currentThread().getId());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long endTime = System.currentTimeMillis();
                    System.out.println("==1=="+endTime);
                }
            });
            executor.execute(new Runnable() {
                public void run() {
                    table.put("d",3);
                    System.out.println("线程123="+Thread.currentThread().getId());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long endTime = System.currentTimeMillis();
                    System.out.println("==2=="+endTime);
                }
            });
        }

        ConcurrentHashMap chash = new ConcurrentHashMap();



    }
}
