package com.ligl.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/*https://blog.csdn.net/weixin_40648117/article/details/80535947
https://www.cnblogs.com/programmer1/p/7994031.html
within:
  within标志符只接受类型声明，它将匹配指定类型下所有的Joinpoint。
例如：within(cn.spring.aop.target.*) 将会匹配 cn.spring.aop.target包下所有类型的方法级别的Joinpoint。
@annotation
  使用@annotation标志符会检查系统中所有对象的所有方法级别Joinpoint，如果被检测的方法标注有@annotation标志符所指定的注解类型，那么当前方法所在的Joinpoint将被Pointcut表达式匹配。例如：@pointcut("@annotation(com.test.aop.log.ALog)") 匹配所有使用了ALog注解的方法。
匹配表达式的维度有很多 上面只是一小部分常用的，并且这些维度是可以组合的 使用||或者$$等等
例如：@around("within(com.test.finance..*) && @annotation(com.test.finance.platform.intf.base.db.ReadOnly)")
在定义Advice的时候 我们匹配的维度可以直接写定义有@pointcut的方法名称 也可以直接使用定义@joinpoint的那一套东西来直接定义要在哪些地方织入（可以直接在Advice上指定匹配哪些方法）
* */
@Aspect
@Component
public class DistributedLockAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private RedissonClient redissonClient;

    @Around(value = "execution(public boolean com.xforceplus.athena..*.*(String,..)) && @annotation(com.xforceplus.athena.aspect.SingleDistributedLock)")
    public boolean singleDistributedLock(ProceedingJoinPoint point){
        boolean result = false;
        Object[] args = point.getArgs();
        String salesBillId = (String) args[0];
        RLock lock = redissonClient.getLock(salesBillId);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(2, -1, TimeUnit.SECONDS);
            if(isLocked) {
                result = (boolean) point.proceed(args);
            }
        } catch (Throwable t) {
            logger.error("{}", t);
        } finally {
            if(isLocked) {
                lock.unlock();
            }
        }
        return result;
    }

    @Around(value = "execution(public boolean com.xforceplus.athena.salesBill.SalesBillService.*(..)) && @annotation(com.xforceplus.athena.aspect.MultipartDistributedLock)")
    public boolean multipartDistributedLock(ProceedingJoinPoint point){
        boolean result = false;
        Object[] args = point.getArgs();
        List<String> salesBillIds = (List<String>) args[0];
        RLock[] locks = new RLock[salesBillIds.size()];
        for (int i = 0; i < salesBillIds.size(); i++) {
            locks[i] = redissonClient.getLock(salesBillIds.get(i));
        }
        RedissonMultiLock lock = new RedissonMultiLock(locks);
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if(isLocked) {
                result = (boolean) point.proceed(args);
            }
        } catch (Throwable t) {
            logger.error("{}", t);
        } finally {
            if(isLocked) {
                lock.unlock();
            }
        }
        return result;
    }
}