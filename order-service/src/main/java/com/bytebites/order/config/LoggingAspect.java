package com.bytebites.order.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class LoggingAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingAspect.class);

    @Before("execution(* com.bytebites.order..*(..))")
    public void logMethodEntry(JoinPoint joinPoint) {
        LOGGER.debug("Entering {} with arguments {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(value = "execution(* com.bytebites.order..*(..))", returning = "result")
    public void logMethodExit(JoinPoint joinPoint, Object result) {
        LOGGER.debug("Exiting {} with result {}", joinPoint.getSignature(), result);
    }
}
