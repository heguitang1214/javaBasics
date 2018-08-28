package spring.aspectj.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogInterceptor {

//    前置增强
   /* @Before("execution(public * spring.aspectj.dao.impl..*.save(..))")
    public void before() {
        System.out.println("method before..............");
    }*/


//    @Pointcut("execution(public void spring.aspectj.service.add(spring.aspectj.model.User))")
    @Pointcut("execution(public * spring.aspectj.dao..*.*(..))")
    public void myMethod() {
    }

    @Before("myMethod()")
    public void before() {
        System.out.println("method before");
    }

    @Around("myMethod()")
    public void aroundMethod(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("method around start");
        pjp.proceed();
        System.out.println("method around end");
    }

}
