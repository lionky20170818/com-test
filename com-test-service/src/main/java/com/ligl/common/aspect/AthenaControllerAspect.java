package com.ligl.common.aspect;

import com.google.common.collect.Maps;
import com.xforceplus.athena.annotation.SkipAuth;
import com.xforceplus.athena.exception.AuthenticationException;
import com.xforceplus.xplat.code.domain.Context;
import com.xforceplus.xplat.configuration.token.JsonWebTokenSettings;
import com.xforceplus.xplat.core.api.ContextHolder;
import com.xforceplus.xplat.core.api.ContextService;
import com.xforceplus.xplat.core.api.TokenService;
import com.xforceplus.xplat.security.api.SignatureService;
import com.xforceplus.xplat.token.domain.TokenBody;
import io.jsonwebtoken.impl.TextCodec;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Created by mumut on 2017/3/20.
 */
@Aspect
@Component
public class AthenaControllerAspect {
    private static Logger logger = LoggerFactory.getLogger(AthenaControllerAspect.class);
    @Autowired
    private ContextService contextService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ContextHolder contextHolder;
    @Autowired
    private JsonWebTokenSettings jsonWebTokenSettings;
    @Autowired
    private SignatureService signatureService;

    @Around("@within(com.xforceplus.xplat.annotation.ApiV1) || @within(com.xforceplus.athena.annotation.DoAuth)")
    public Object doFilter(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Annotation[] annotations = method.getAnnotations();
        Context context = new Context();
        Map<String, Object> additionalProperties = Maps.newHashMap();
        /**
         * skip
         */
        if (Arrays.stream(annotations).anyMatch(a -> a.annotationType().equals(SkipAuth.class))) {
            getTokenBody(context, additionalProperties);
            context.setAttributes(additionalProperties);
            contextHolder.putContext(context);
            Object returnObj = joinPoint.proceed();
            return returnObj;
        }

        Object returnObj = null;

        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = sra.getRequest();
        StringBuffer url = request.getRequestURL();
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            // 校验
            Assert.notNull(request, "请求信息不能为空");
            //添加日志打印
            String requestId = String.valueOf(UUID.randomUUID());
            MDC.put("requestId", requestId);
            logger.info("==>Request: url= {}，method= {}，ip= {}，params= {}", url, request.getMethod(), request.getRemoteAddr(), Arrays.toString(joinPoint.getArgs()));
            logger.info("authSetting on");
            TokenBody result = getTokenBody(context, additionalProperties);
            String operationToken = contextService.getAttributeByType("X-Operation-Token", String.class);
            if (StringUtils.isEmpty(operationToken)) {
                throw new AuthenticationException(result.getSubject(), "未指定操作令牌");
            }
//            logger.info("OperationToken: {}", operationToken);
            String[] tokenParts = operationToken.split(":");
            if (tokenParts.length != 2) {
                throw new AuthenticationException(result.getSubject(), "操作令牌不合法");
            }
            String operationName = tokenParts[0];
            String[] apiSignatures = tokenParts[1].split("\\.");

            String apiName = joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
            String funcs = (String) result.getAdditionalProperties().get("fuc");
            if (funcs == null || funcs.isEmpty()) {
                // 无权限操作？
                throw new AuthenticationException(result.getSubject(), "无操作权限");
            }
            String[] operations = funcs.split(",");

            if (Stream.of(operations).noneMatch(opt -> operationName.equals(opt))) {
                throw new AuthenticationException(result.getSubject(), "无权限操作");
            }
            logger.debug("operationToken={} apiName==={} apiSignatures={}  result.getSubject()={} joinPoint={}", operationToken, apiName, apiSignatures, result.getSubject(), joinPoint);
            if (Stream.of(apiSignatures).noneMatch(sign -> validate(operationName, apiName, result.getSubject(), sign, joinPoint))) {
                throw new AuthenticationException(result.getSubject(), "操作权限不合法");
            }
            context.setOperationName(operationName);
            context.setAttributes(additionalProperties);
            /**
             * set new one
             * will clear in apiSecurityInterceptor
             */
            contextHolder.putContext(context);
            returnObj = joinPoint.proceed();
        } catch (Throwable e) {
            logger.error("请求异常", e);
            throw e;
        } finally {
            watch.stop();
            logger.info("==>Response：[{}]ms，Response：{},url= {} method:{}", watch.getTime(), null, url,request.getMethod());
        }
        return returnObj;
    }

    private TokenBody getTokenBody(Context context, Map<String, Object> additionalProperties) {
        String accessToken = contextService.getAttributeByType("X-Access-Token", String.class);
//            logger.info("AccessToken: {}", accessToken);
        TokenBody result = tokenService.validateToken(accessToken);
//            logger.info("TokenBody: {}", JsonUtils.toJson(result));
        context.setUsername(result.getSubject());
        context.setDisplayName(result.getDisplayName());

        if (result.getAdditionalProperties().containsKey("TENANT_ID")) {
            additionalProperties.put("TENANT_ID", new Long(result.getAdditionalProperties().get("TENANT_ID").toString()));
        }
        if (result.getAdditionalProperties().containsKey("TENANT_CODE")) {
            additionalProperties.put("TENANT_CODE", new String(result.getAdditionalProperties().get("TENANT_CODE").toString()));
        }
        if (result.getAdditionalProperties().containsKey("MOBILE")) {
            additionalProperties.put("MOBILE", new String(result.getAdditionalProperties().get("MOBILE").toString()));
        }
        if (result.getAdditionalProperties().containsKey("ACCOUNT_ID")) {
            additionalProperties.put("ACCOUNT_ID", new Long(result.getAdditionalProperties().get("ACCOUNT_ID").toString()));
        }
        context.setAttributes(additionalProperties);
        return result;
    }

    /**
     * signature format is
     * nvl(params,"") $$ ((API.ENAME-(API_PATH $$ NODE_PARAM)).signWith(jsonKey + AccountName))
     * extract the params
     *
     * @param operationName
     * @param apiName
     * @param operatorName
     * @param signature
     * @return
     */
    protected boolean validate(String operationName, String apiName, String operatorName, String signature, JoinPoint joinPoint) {
        try {
            /**
             * first validate the signature to avoid illegal expression execution
             */
            String[] signatureArray = signature.split("\\$\\$");
            String params = signatureArray.length == 2 ? signatureArray[0] : "";
            String realSignature = signatureArray.length == 2 ? signatureArray[1] : signatureArray[0];
            String decodedParams = StringUtils.isEmpty(params) ? "" : TextCodec.BASE64URL.decodeToString(params);
            String content = operationName + "-" + apiName + "$$" + decodedParams;
            String secret = jsonWebTokenSettings.getSigningKey() + "." + operatorName;
            String thisSign = signatureService.createHS256Signature(content, secret);
            logger.info("content={} secret={} thisSign={} real={}", content, secret, thisSign, realSignature);
            if (!signatureService.validateHS256Signature(content,
                    secret,
                    realSignature)) {
                return false;
            }

            if (!StringUtils.isEmpty(decodedParams)) {
                try {
                    ExpressionParser parser = new SpelExpressionParser();
                    if (!parser.parseExpression(decodedParams).getValue(joinPoint.getArgs(), Boolean.class)) {
                        throw new RuntimeException();
                    }
                } catch (EvaluationException ex) {
                    return false;
                }
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
}
