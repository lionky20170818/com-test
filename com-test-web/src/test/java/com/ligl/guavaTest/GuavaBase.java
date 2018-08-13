package com.ligl.guavaTest;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.ObjectUtils.max;
import static org.apache.commons.lang3.ObjectUtils.min;

/**
 * Function:
 * Author: created by liguoliang
 * Date: 2017/11/17 0017 上午 10:45
 * Version: 1.0
 * http://outofmemory.cn/java/guava/cache/deep-analystics这个讲解的很详细
 */
@Slf4j
public class GuavaBase {

    @Test
    public void BaseTest1() {
        //1，equals替换
        System.out.println("a1===" + Objects.equal("a", "a"));//true
        log.info("a2===" + Objects.equal(null, "a"));//false
        log.info("a3===" + Objects.equal("a", null));//false
        log.info("a4===" + Objects.equal(null, null));//true
        //2，比较大小
        log.info("b1===" + min(1, 2, 4, 5, 1));//1
        log.info("b2===" + max(0.1, 0.2, 0.6));//0.6
        //3，字符串连接
//        Joiner joiner = Joiner.on("; ").skipNulls();
        Joiner joiner = Joiner.on("; ").useForNull("AAA");
        log.info("===" + joiner.join("Harry", null, "Ron", "Hermione"));//Harry; AAA; Ron; Hermione
        String joinResult = Joiner.on(" ").join(new String[]{"hello", "world"});
        System.out.println("joinResult==" + joinResult);//hello world
        //4，判断字符串为""或null
        String input = null;
        boolean isNullOrEmpty = Strings.isNullOrEmpty(input);
        System.out.println("isNullOrEmpty===" + isNullOrEmpty);//true

        //4，左右补全字符串
        //右补字符串
        String padEndResult = Strings.padEnd("123", 6, 'b');
        System.out.println("padEndResult is " + padEndResult);//123bbb
        //左补字符串
        String padStartResult = Strings.padStart("1", 4, 'a');
        System.out.println("padStartResult is " + padStartResult);//aaa1

        //5，拆分字符串
        Iterable<String> splitResults = Splitter.onPattern("[,，]{1,}")//正则表达式
                .trimResults()
                .omitEmptyStrings()//忽略空字符串
                .split("hello,word,,世界，水平");

        for (String item : splitResults) {
            System.out.println("===" + item);
        }
//        ===hello
//        ===word
//        ===世界
//        ===水平
        //拆分为map的键值对
        String toSplitString = "a=b;c=d,e=f";
        //二次拆分首先是使用onPattern做第一次的拆分，然后再通过withKeyValueSeperator('')方法做第二次的拆分
        Map<String, String> kvs = Splitter.onPattern("[,;]{1,}").withKeyValueSeparator('=').split(toSplitString);
        for (Map.Entry<String, String> entry : kvs.entrySet()) {
            System.out.println("==Splitter==" + String.format("%s=%s", entry.getKey(), entry.getValue()));
        }
//        ==Splitter==a=b
//        ==Splitter==c=d
//        ==Splitter==e=f

        //6，合并
        Map<String, String> map = new HashMap<String, String>();
        map.put("a", "b");
        map.put("c", "d");
        String mapJoinResult = Joiner.on(",").withKeyValueSeparator("=").join(map);
        System.out.println("mapJoinResult==" + mapJoinResult);//a=b,c=d

        //5，List集合类方法
        List list1 = Lists.newArrayList();


    }

    @Test
    public void MultisetTest2() {
        //元素个数判断
        String strWorld = "wer|dffd|ddsa|dfd|dreg|de|dr|ce|ghrt|cf|gt|ser|tg|ghrt|cf|gt|" +
                "ser|tg|gt|kldf|dfg|vcd|fg|gt|ls|lser|dfr|wer|dffd|ddsa|dfd|dreg|de|dr|" +
                "ce|ghrt|cf|gt|ser|tg|gt|kldf|dfg|vcd|fg|gt|ls|lser|dfr";
        String[] words = strWorld.split("\\|");

        List<String> wordList = new ArrayList<String>();
        for (String word : words) {
            wordList.add(word);
        }

        //将数据集添加到Multiset中
        Multiset<String> wordsMultiset = HashMultiset.create();
        wordsMultiset.addAll(wordList);

        //elementSet(): 将不同的元素放入一个Set中
        //count(Object element)：返回给定参数元素的个数
        for (String key : wordsMultiset.elementSet()) {
            System.out.println(key + " count：" + wordsMultiset.count(key));
        }

        //还提供了其他存储
//        HashMultiset: 元素存放于 HashMap
//        LinkedHashMultiset: 元素存放于 LinkedHashMap，即元素的排列顺序由第一次放入的顺序决定
//        TreeMultiset:元素被排序存放于TreeMap
//        EnumMultiset: 元素必须是 enum 类型
//        ImmutableMultiset: 不可修改的 Mutiset
    }

    //双向map
    @Test
    public void biMapTest2() {
        BiMap<String, String> weekNameMap = HashBiMap.create();
        weekNameMap.put("星期一", "Monday");
        weekNameMap.put("星期二", "Tuesday");
        weekNameMap.put("星期三", "Wednesday");
        weekNameMap.put("星期四", "Thursday");
        weekNameMap.put("星期五", "Friday");
        weekNameMap.put("星期六", "Saturday");
        weekNameMap.put("星期日", "Sunday");

        System.out.println("星期日的英文名是" + weekNameMap.get("星期日"));
        System.out.println("Sunday的中文是" + weekNameMap.inverse().get("Sunday"));

//        BiMap的常用实现有：
//        HashBiMap: key 集合与 value 集合都有 HashMap 实现
//        EnumBiMap: key 与 value 都必须是 enum 类型
//        ImmutableBiMap: 不可修改的 BiMap
    }

    @Test
    //java中key值可以重复的map：IdentityHashMap
    public void MultimapTest() {
//        Multimap提供了丰富的实现，所以你可以用它来替代程序里的Map<K, Collection<V>>，具体的实现如下：
//        实现	Key实现	Value实现
//        ArrayListMultimap	HashMap	ArrayList
//        HashMultimap	HashMap HashSet
//        LinkedListMultimap	LinkedHashMap LinkedList
//        LinkedHashMultimap	LinkedHashMap LinkedHashSet
//        TreeMultimap	TreeMap	TreeSet
//        ImmutableListMultimap	ImmutableMap	ImmutableList
//        ImmutableSetMultimap	ImmutableMap	ImmutableSet
        Multimap<String, String> myMultimap = ArrayListMultimap.create();

        // 添加键值对
        myMultimap.put("Fruits", "Bannana");
        //给Fruits元素添加另一个元素
        myMultimap.put("Fruits", "Apple");
        myMultimap.put("Fruits", "Pear");
        myMultimap.put("Vegetables", "Carrot");

        // 获得multimap的size
        int size = myMultimap.size();
        System.out.println("size=" + size);  // 4

        // 获得Fruits对应的所有的值
        Collection<String> fruits = myMultimap.get("Fruits");
        System.out.println("fruits=" + fruits); // [Bannana, Apple, Pear]

        Collection<String> vegetables = myMultimap.get("Vegetables");
        System.out.println("vegetables=" + vegetables); // [Carrot]

        //遍历Mutlimap
        for (String value : myMultimap.values()) {
            System.out.println("value=" + value);
        }

        // Removing a single value
        myMultimap.remove("Fruits", "Pear");
        System.out.println(myMultimap.get("Fruits")); // [Bannana, Pear]

        // Remove all values for a key
        myMultimap.removeAll("Fruits");
        System.out.println(myMultimap.get("Fruits")); // [] (Empty Collection!)

    }

//    ListenableFuture顾名思义就是可以监听的Future，它是对java原生Future的扩展增强。
//    我们知道Future表示一个异步计算任务，当任务完成时可以得到计算结果。
//    如果我们希望一旦计算完成就拿到结果展示给用户或者做另外的计算，就必须使用另一个线程不断的查询计算状态。
//    这样做，代码复杂，而且效率低下。
//    使用ListenableFuture Guava帮我们检测Future是否完成了，如果完成就自动调用回调函数，这样可以减少并发程序的复杂度。

    /**
     * 基于java future的优化，推荐使用这种方式创建多线程,异步处理
     */
    @Test
    public void ListenableFutureTest() {
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        for (int i = 0; i < 10; i++) {
            final ListenableFuture<Integer> listenableFuture = executorService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    System.out.println("call execute..");
                    log.info("线程 %s  ", Thread.currentThread().getId());

                    TimeUnit.SECONDS.sleep(1);
                    return 7;
                }
            });

//        通过Futures的静态方法addCallback给ListenableFuture添加回调函数
            Futures.addCallback(listenableFuture, new FutureCallback<Integer>() {
                @Override
                public void onSuccess(Integer result) {
                    //do success
                    System.out.println("get listenable future's result with callback " + result);
                }

                @Override
                public void onFailure(Throwable t) {
                    //do failure
                    System.out.println("addCallback failure ");
                    t.printStackTrace();
                }
            });


        }

    }


}
