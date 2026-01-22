package com.jacolp.task_system.exception;

import com.jacolp.task_system.controller.TaskController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice   // 全局异常处理
public class GlobalExceptionHandler {

    /**
     * 捕获认证相关的异常 （401/403）
     * 并告诉前端 Token 无效了
     * @param e 捕获到的异常
     * @return 错误信息
     */
    @ExceptionHandler({AuthenticationException.class,
            AccessDeniedException.class,
            BadCredentialsException.class,
            AuthRuntimeException.class})
    public ResponseEntity<String> handleAuthException(Exception e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("认证失败：" + e.getMessage());
    }

    /**
     * 捕获任务处理相关的异常
     * @param e 捕获到的异常
     * @return 错误信息
     */
    @ExceptionHandler({TaskException.class})
    public ResponseEntity<String> handleTaskException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器任务处理错误：" + e.getMessage());
    }

    /**
     * 捕获物品处理相关的异常
     * @param e 捕获到的异常
     * @return 错误信息
     */
    @ExceptionHandler({ItemException.class})
    public ResponseEntity<String> handleItemException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("物品处理错误：" + e.getMessage());
    }

    /**
     * 捕获用户处理相关的异常
     * @param e 捕获到的异常
     * @return 错误信息
     */
    @ExceptionHandler({UserException.class})
    public ResponseEntity<String> handleUserException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("用户处理错误：" + e.getMessage());
    }

    /**
     * 捕获其他异常
     * @param e 捕获到的异常
     * @return 错误信息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("捕获到异常：" + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误：" + e.getMessage());
    }
}
