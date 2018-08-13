package com.ligl.common.aspect;

import com.ligl.common.exception.CommonException;
import com.ligl.common.utils.JsonUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.concurrent.*;

/**
 * Created by liguoliang on 2017/4/13 0013.
 * 使用注解的方式实现AOp
 */
@Aspect
@Component("LogAspect")
public class LogPersistInterceptor {
//    @Autowired
//    private ErrorLogService errorLogService;

    //多线程定义
    private ExecutorService executorService = new ThreadPoolExecutor(16, 32, 0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(2 << 16));
//异步处理通知类消息
//            executorService.execute(new Runnable() {
//        @Override
//        public void run() {
//            //占用库存
//            changeProductStock(orderReq, ChangeProductStockTypeEnum.OCCUPY.getCode());
//        }
//    });

    @Resource(name = "ThreadExecutor")
    private ScheduledExecutorService executor;

    private static Logger logger = LoggerFactory.getLogger(LogPersistInterceptor.class);

    /**
     * 异常日志持久化
     *
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around(value = "execution(* com.ligl.trans.service.*.*(..))||execution(* com.ligl.trans.service.*.*.*(..))", argNames = "pjp")
    public Object persist(ProceedingJoinPoint pjp) throws Throwable {
        Object result = null;
//        ErrorLogEntity errorLogEntity = new ErrorLogEntity();
        try {
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
//            errorLogEntity.setErrorMethod(method.getName());
            Object[] args = pjp.getArgs();
            for (int i = 0; i < args.length; i++) {
                Object obj = args[i];
                if (obj != null) {
                    String req = JsonUtils.getArgsString(obj);
                    logger.info("请求参数" + ":" + req);
//                    errorLogEntity.setRequestParam(obj.getClass().getSimpleName() + ":" + req);
                }
            }
            //执行目标代码
            result = pjp.proceed(pjp.getArgs());

        } catch (Exception e) {
            if (!(e instanceof CommonException)) {
                if (e.getMessage() != null) {
                    String errorMessage = e.getMessage().length() > 512 ? e.getMessage().substring(0, 511) : e.getMessage();
//                    errorLogEntity.setErrorMessage(errorMessage);
                }
//                errorLogEntity.setTrace(ExceptionUtils.getStackTrace(e).length()>=1024? ExceptionUtils.getStackTrace(e).substring(0,1023): ExceptionUtils.getStackTrace(e));
//                errorLogEntity.setErrorTime(new Date());
                try {
//                    final ErrorLogEntity log = errorLogEntity;

//                    executor.execute(new Runnable() {
//                        public void run() {
//                            errorLogService.save(log);
//                        }
//                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    logger.error("Log save throw exception:", ex);
                }
            }
            throw e;
        }
        return result;
    }
}
