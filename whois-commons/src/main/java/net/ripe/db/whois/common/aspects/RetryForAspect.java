package net.ripe.db.whois.common.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// @Order is not supported with compile-time weaving, so we resorted to AspectJ annotation

@Aspect
@DeclarePrecedence("RetryForAspect, *")
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
        int attempt = 0;
        Exception originalException = null;

        while (true) {
            try {
                return pjp.proceed();
            } catch (Exception e) {
                if (isRetryAfterException(e, retryFor.value())) {
                    if (originalException == null) {
                        originalException = e;
                    }

                    final Logger logger = LoggerFactory.getLogger(pjp.getSignature().getDeclaringType());
                    final String signature = pjp.getSignature().toShortString();

                    final int attempts = retryFor.attempts();
                    if (++attempt < attempts) {
                        logger.error("{} attempt {}/{} failed, retrying in {} ms", signature, attempt, attempts, retryFor.intervalMs(), e);
                        Thread.sleep(retryFor.intervalMs());
                    } else {
                        logger.error("{} attempt {}/{} failed", signature, attempt, attempts, e);
                        throw originalException;
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    private static boolean isRetryAfterException(final Exception e, final Class<? extends Exception>... exceptionClasses) {
        for (final Class<? extends Exception> exceptionClass : exceptionClasses) {
            if (exceptionClass.isAssignableFrom(e.getClass())) {
                return true;
            }
        }

        return false;
    }
}
