package com.campus.secondhand.common.web.aspect;

import com.campus.secondhand.common.core.log.OperationLog;
import com.campus.secondhand.common.security.context.UserContext;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final int MAX_TEXT_LENGTH = 300;

    @Around("@annotation(operationLog)")
    public Object record(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = ((MethodSignature) joinPoint.getSignature()).toShortString();
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        String role = UserContext.getRole();
        String argsText = formatArgs(joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            log.info("[操作日志] 动作={} 用户ID={} 用户名={} 角色={} 方法={} 参数={} 结果=成功 耗时={}ms",
                operationLog.value(), userId, username, role, methodName, argsText, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("[操作日志] 动作={} 用户ID={} 用户名={} 角色={} 方法={} 参数={} 结果=失败 异常={} 耗时={}ms",
                operationLog.value(), userId, username, role, methodName, argsText, ex.getMessage(),
                System.currentTimeMillis() - start);
            throw ex;
        }
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        String joined = Arrays.stream(args)
            .map(this::safeValue)
            .collect(Collectors.joining(", ", "[", "]"));
        return joined.length() > MAX_TEXT_LENGTH ? joined.substring(0, MAX_TEXT_LENGTH) + "..." : joined;
    }

    private String safeValue(Object arg) {
        if (arg == null) {
            return "null";
        }
        String text = String.valueOf(arg);
        return text.length() > 120 ? text.substring(0, 120) + "..." : text;
    }
}
