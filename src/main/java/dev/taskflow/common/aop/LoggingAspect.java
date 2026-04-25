package dev.taskflow.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j

public class LoggingAspect {

    @Pointcut("execution(* dev.taskflow..*Service.*(..))")
    public void serviceLayer(){}

    @Around("serviceLayer()")
    public Object logServiceCall(ProceedingJoinPoint joinPoint) throws Throwable{
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.debug("-> {}.{}() called", className, methodName);
        long start = System.currentTimeMillis();

        try{
            Object result = joinPoint.proceed(); //call method
            long elapsed = System.currentTimeMillis();
            log.debug("<- {}.{}() completed in {}ms", className, methodName, elapsed);
            return result;
        } catch (Throwable ex){
            long elapsed = System.currentTimeMillis() - start;
            log.warn("x {}.{}() threw {} after {}ms", className, methodName, ex.getClass().getSimpleName(), elapsed);
            throw ex;
        }
    }

}
