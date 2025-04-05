package com.epam.agency.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(public * com.epam.agency.controller.*.*(..))")
    private void publicMethodsFromControllerPackage() {
    }

    @Pointcut("execution(public * com.epam.agency.service.*.*(..))")
    private void publicMethodsFromServicePackage() {
    }

    @Before("publicMethodsFromControllerPackage()")
    public void doBeforeController(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest httpServletRequest = null;

        if (attributes != null) {
            httpServletRequest = attributes.getRequest();
        }

        if (httpServletRequest != null) {
            log.info("NEW REQUEST: IP: {}, URL: {}, HTTP_METHOD: {}, CONTROLLER_METHOD: {}.{}",
                    httpServletRequest.getRemoteAddr(),
                    httpServletRequest.getRequestURL().toString(),
                    httpServletRequest.getMethod(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
        }
    }

    @Before("publicMethodsFromServicePackage()")
    public void doBeforeService(JoinPoint joinPoint) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        Object[] args = joinPoint.getArgs();
        String argsString = args.length > 0 ? Arrays.toString(args) : "METHOD HAS NO ARGUMENTS";

        log.info("RUN SERVICE: SERVICE_METHOD: {}.{}\nMETHOD_ARGUMENTS: [{}]",
                className, methodName, argsString);
    }

    @AfterReturning(returning = "returnObject", pointcut = "publicMethodsFromControllerPackage()")
    public void doAfterReturningController(Object returnObject) {
        log.info("Controller method returned value: {}", returnObject);
    }

    @AfterReturning(returning = "returnObject", pointcut = "publicMethodsFromServicePackage()")
    public void doAfterReturningService(Object returnObject) {
        log.info("Service method returned value: {}", returnObject);
    }

    @After("publicMethodsFromControllerPackage()")
    public void doAfter(JoinPoint joinPoint) {
        log.info("Controller Method executed successfully: {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
    }

    @Around("publicMethodsFromControllerPackage()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - start;

        log.info("Execution method: {}.{}. Execution time: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                executionTime);

        return proceed;
    }

    @AfterThrowing(throwing = "exception", pointcut = "publicMethodsFromControllerPackage()")
    public void throwsException(JoinPoint joinPoint, Exception exception) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.error("Execution in {}.{} with arguments {}. Exception message: {}",
                className, methodName, Arrays.toString(joinPoint.getArgs()), exception.getMessage());
    }
}
