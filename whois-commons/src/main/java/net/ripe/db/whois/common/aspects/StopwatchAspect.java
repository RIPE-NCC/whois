package net.ripe.db.whois.common.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclarePrecedence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

@Aspect
@DeclarePrecedence("StopwatchAspect, *")
public class StopwatchAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopwatchAspect.class);

    @Around("@annotation(stopwatch) && execution(* *(..))")
    public Object stopwatchOnAnnotatedMethod(final ProceedingJoinPoint pjp, final Stopwatch stopwatch) throws Throwable {
        final com.google.common.base.Stopwatch elapsed = com.google.common.base.Stopwatch.createStarted();
        try {
            return pjp.proceed();
        } finally {
            final long elapsedMs = elapsed.elapsed(TimeUnit.MILLISECONDS);
            if (elapsedMs >= stopwatch.thresholdMs()) {
                if (stopwatch.arguments()) {
                    LOGGER.info("Class {} Method {} took {} ms\n{}",
                        pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                        pjp.getStaticPart().getSignature().getName(),
                        elapsedMs,
                        joinArguments(pjp.getArgs()));
                } else {
                    LOGGER.info("Class {} Method {} took {} ms",
                        pjp.getStaticPart().getSignature().getDeclaringTypeName(),
                        pjp.getStaticPart().getSignature().getName(),
                        elapsedMs);
                }
            }
        }
    }

    private static String joinArguments(final Object[] args) {
        final StringBuilder builder = new StringBuilder();

        for (Iterator i = Arrays.asList(args).iterator(); i.hasNext(); ) {
            final Object arg = i.next();
            builder.append('\t').append("Argument: ");
            if (arg != null) {
                builder.append(arg.toString()).append(" (").append(arg.getClass().getName()).append(")");
            } else {
                builder.append("null");
            }
            if (i.hasNext()) {
                builder.append('\n');
            }
        }

        return builder.toString();
    }

}
