package com.sample.system.vault.service.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Generic logging for service and controller methods.
     * Logs class, method, arguments, execution time and result.
     *
     * IMPORTANT: We limit the pointcut to beans annotated with
     * {@link Service} or {@link RestController} to avoid proxying
     * configuration and @ConfigurationProperties beans like VaultProperties,
     * which are often final and cannot be subclassed by CGLIB.
     */
    @Around("execution(* com.sample.system.vault.service..*(..))"
          + " && (@within(org.springframework.stereotype.Service)"
          + "     || @within(org.springframework.web.bind.annotation.RestController))"
          + " && !within(com.sample.system.vault.service.config.LoggingAspect)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        Object[] args = joinPoint.getArgs();
        String argsString = Arrays.toString(args);

        log.info(">> {}.{} args={}", className, methodName, argsString);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            String resultString = String.valueOf(result);
            log.info("<< {}.{} took {} ms, result={}", className, methodName, duration, resultString);

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("!! {}.{} failed after {} ms: {}",
                    className, methodName, duration, ex.getMessage(), ex);
            throw ex;
        }
    }
}

