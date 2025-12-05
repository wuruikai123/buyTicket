package com.buyticket.exception;

import com.buyticket.utils.JsonData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public JsonData handle(Exception e) {
        log.error("[系统异常] {}", e.getMessage(), e);
        return JsonData.buildError("系统异常: " + e.getMessage(), -1);
    }

    // 可以在这里添加自定义异常处理，如 BusinessException
}
