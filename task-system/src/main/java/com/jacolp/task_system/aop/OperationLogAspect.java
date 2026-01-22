package com.jacolp.task_system.aop;

import com.jacolp.task_system.anno.MethodLog;
import com.jacolp.task_system.entity.OperationLog;
import com.jacolp.task_system.mapper.OperationLogMapper;
import com.jacolp.task_system.utils.CurrentUserIdHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Aspect
@Component
public class OperationLogAspect {
    @Autowired private OperationLogMapper operationLogMapper;

    /**
     * 环绕通知，记录操作日志
     * @param joinPoint 切点
     * @return 返回结果
     * @throws Throwable 异常
     */
    @Around("@annotation(com.jacolp.task_system.anno.MethodLog)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        // 先获取方法 然后获取注解
        MethodLog methodLogAnnotation = ((MethodSignature)joinPoint.getSignature()).getMethod().getAnnotation(MethodLog.class);

        // 1. 先检查注解是否启用
        if (!methodLogAnnotation.enable()) {
            return joinPoint.proceed();
        }

        // 2. 创建日志实体
        OperationLog opLog = new OperationLog();

        // 判断userId是否存在异常
        Long userId = CurrentUserIdHolder.getCurrentUserId();
        if (userId == null) {
            log.warn("用户ID已从ThreadLocal中获取，但可能存在问题！其值为：{}", userId);
        }
        opLog.setUserId(userId);
        opLog.setTime(new Date());

        // 3. 设置类名（优先使用注解中的prefix）
        String className = joinPoint.getTarget().getClass().getName();
        opLog.setClassName(methodLogAnnotation.prefix().isEmpty() ? className :
                methodLogAnnotation.prefix() + "." + className);

        // 4. 设置方法名和参数
        opLog.setMethodName(joinPoint.getSignature().getName());
        opLog.setParams(sanitizeParams(joinPoint.getArgs())); // 安全处理参数

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long costTime = System.currentTimeMillis() - startTime;
            opLog.setCostTime(costTime);
            opLog.setReturnValue(result == null ? "null" : result.toString());
            return result;
        } catch (Throwable e) {
            long costTime = System.currentTimeMillis() - startTime;
            opLog.setCostTime(costTime);
            opLog.setException(e.getClass().getSimpleName() + ": " + e.getMessage());
            opLog.setReturnValue("ERROR");
            operationLogMapper.insert(opLog); // 出错时也要记录
            throw e;
        } finally {
            // 如果没有在catch中保存，这里保存正常操作日志
            if (opLog.getException() == null) {
                operationLogMapper.insert(opLog);
            }
            log.info("成功记录操作日志：{}", opLog);
        }
    }

    /**
     * 参数过滤
     * @param args 参数
     * @return 过滤后的参数
     */
    private String sanitizeParams(Object[] args) {
        if (args == null || args.length == 0) return "[]";

        // 避免记录敏感信息（如密码）或大对象
        return Arrays.stream(args)
                .map(arg -> {
                    if (arg == null) return "null";
                    // 处理大对象
                    if (arg instanceof String && ((String)arg).length() > 200) {
                        return ((String)arg).substring(0, 200) + "...";
                    }
                    // 过滤敏感字段
                    String str = arg.toString();
                    if (str.contains("password") || str.contains("secret")) {
                        return "[FILTERED]";
                    }
                    return str;
                })
                .collect(Collectors.joining(", ", "[", "]"));
    }

}
