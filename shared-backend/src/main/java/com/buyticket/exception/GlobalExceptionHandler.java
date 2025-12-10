package com.buyticket.exception;

import com.buyticket.utils.JsonData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理静态资源未找到异常（favicon.ico、.well-known等浏览器自动请求）
     * 这些请求不应该记录为错误
     */
    @ExceptionHandler(value = NoResourceFoundException.class)
    public void handleNoResourceFound(NoResourceFoundException e, 
                                      HttpServletRequest request, 
                                      HttpServletResponse response) {
        String resourcePath = e.getResourcePath();
        
        // 只对常见的浏览器自动请求不记录日志，其他资源请求记录为debug
        if (resourcePath != null && (
            resourcePath.equals("favicon.ico") ||
            resourcePath.startsWith(".well-known/") ||
            resourcePath.equals("robots.txt")
        )) {
            // 静默处理，不记录日志，直接返回404
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } else {
            // 其他静态资源请求记录为debug级别
            log.debug("[静态资源未找到] {}", resourcePath);
            response.setStatus(HttpStatus.NOT_FOUND.value());
        }
    }

    /**
     * 处理404异常（NoHandlerFoundException）
     */
    @ExceptionHandler(value = NoHandlerFoundException.class)
    public JsonData handleNoHandlerFound(NoHandlerFoundException e) {
        log.debug("[接口未找到] {}", e.getRequestURL());
        return JsonData.buildError("接口不存在", 404);
    }

    /**
     * 处理其他所有异常
     */
    @ExceptionHandler(value = Exception.class)
    public JsonData handle(Exception e) {
        log.error("[系统异常] {}", e.getMessage(), e);
        return JsonData.buildError("系统异常: " + e.getMessage(), -1);
    }

    // 可以在这里添加自定义异常处理，如 BusinessException
}
