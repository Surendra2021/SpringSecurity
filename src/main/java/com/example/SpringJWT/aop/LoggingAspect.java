package com.example.SpringJWT.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

// @Aspect — tells Spring this class contains AOP logic
// @Component — registers it as a Spring bean
@Aspect
@Component
public class LoggingAspect {

    // Pointcut — "execution(* com.example.SpringJWT.service.*.*(..))" means:
    // *          → any return type
    // service.*  → any class in service package
    // *(..)      → any method with any parameters
    // So: run this before EVERY method in EVERY service class

    // @Before — runs BEFORE the method executes
    @Before("execution(* com.example.SpringJWT.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        // joinPoint.getSignature().getName() → gives the method name
        System.out.println("Method called: " + joinPoint.getSignature().getName());
    }

    // @AfterThrowing — runs only when method throws an exception
    @AfterThrowing(
        pointcut = "execution(* com.example.SpringJWT.service.*.*(..))",
        throwing = "exception"
    )
    public void logException(JoinPoint joinPoint, Exception exception) {
        System.out.println("Exception in: " + joinPoint.getSignature().getName()
                + " → " + exception.getMessage());
    }

    // @Around — runs BEFORE and AFTER the method
    // used here to measure how long each method takes
    @Around("execution(* com.example.SpringJWT.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        // proceed() — actually runs the real method
        Object result = joinPoint.proceed();

        long duration = System.currentTimeMillis() - start;
        System.out.println(joinPoint.getSignature().getName()
                + " took " + duration + "ms");

        return result;
    }
}
