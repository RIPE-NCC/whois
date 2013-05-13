package net.ripe.db.whois.common.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RetryForAspect {
    @Around("@within(retryFor) && execution(public * *(..)) && !@annotation(RetryFor)")
    public Object retryForPublicMethodInAnnotatedType(final ProceedingJoinPoint pjp, final RetryFor retryFor) throws Throwable {
        return retryFor(pjp, retryFor);
    }

    @Around("@annotation(retryFor) && execution(* *(..))")
    public Object retryForAnnotatedMethod(final ProceedingJoinPoint pjp, final RetryFor retryFor) throws Throwable {
        return retryFor(pjp, retryFor);
    }

    private Object retryFor(final ProceedingJoinPoint pjp, final RetryFor retryFor) throws Throwable {
        return Retry.forExceptions(new Retry.Retryable() {
            @Override
            public Object attempt() throws Throwable {
                return pjp.proceed();
            }
        }, retryFor.attempts(), retryFor.intervalMs(), retryFor.value());
    }
}
