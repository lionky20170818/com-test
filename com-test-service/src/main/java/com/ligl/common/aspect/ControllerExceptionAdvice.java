package com.ligl.common.aspect;


import com.xforceplus.athena.common.ExceptionResponse;
import com.xforceplus.xplat.core.service.jwt.TokenExpiredException;
import com.xforceplus.xplat.core.service.jwt.TokenValidateException;
import com.xforceplus.xplat.diagnose.DiagnoseService;
import com.xforceplus.xplat.domain.Response;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * 项目名称: 票易通IMSC
 * 模块名称:
 * 说明:
 * JDK 版本: JDK1.8
 * 作者：liwei
 * 创建日期：2017/2/28
 */
@ControllerAdvice(annotations = RestController.class, basePackages = "com.xforceplus.athena")
public class ControllerExceptionAdvice {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Optional<DiagnoseService> diagnoseService;

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<Response> handResourceNotFoundException(HttpServletRequest req, ResourceNotFoundException exception) {
        logger.error("指定的资源不存在！",exception);
        ExceptionResponse response = new ExceptionResponse();
        response.setPath(req.getRequestURI());
        response.setCode(-1);
        response.setMessage(exception.getMessage());
        response.setResult(ExceptionUtils.getStackTrace(exception));
        response.setException(exception.getClass().getCanonicalName());
        return new ResponseEntity<Response>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Response> defaultErrorHander(HttpServletRequest req, Exception exception) {
        if(!(exception instanceof TokenExpiredException)) {
            logger.error("服务内部异常",exception);
        }
        ExceptionResponse response = new ExceptionResponse();
        response.setPath(req.getRequestURI());
        response.setCode(-1);
        //response.setMessage(exception.getMessage());
        response.setMessage(exception.getMessage());
        response.setResult(ExceptionUtils.getStackTrace(exception));
        response.setException(exception.getClass().getCanonicalName());
        ResponseStatus annotation = findMergedAnnotation(exception.getClass(), ResponseStatus.class);
        if (annotation != null) {
            return new ResponseEntity<Response>(response,annotation.value());
        }
        if(exception instanceof HttpMessageNotReadableException) {
            return new ResponseEntity<Response>(response, HttpStatus.BAD_REQUEST);
        }
        if(exception instanceof TokenExpiredException) {
            return new ResponseEntity<Response>(response, HttpStatus.UNAUTHORIZED);
        }
        if(exception instanceof AuthenticationException) {
            return new ResponseEntity<Response>(response, HttpStatus.FORBIDDEN);
        }
        if(exception instanceof TokenValidateException) {
            return new ResponseEntity<Response>(response, HttpStatus.UNAUTHORIZED);
        }
        if(exception instanceof SalesBillException) {
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }
        if(exception instanceof IllegalArgumentException) {
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }
        if(exception instanceof  FileUploadException) {
            return new ResponseEntity<Response>(response, HttpStatus.OK);
        }

        /**
         * unhandled exceptions
         */
        diagnoseService.ifPresent(service -> service.append(req, exception));

        return new ResponseEntity<Response>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
