package net.ripe.db.whois.common.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Stopwatch {

    // whether to log method arguments
    boolean arguments() default true;

    // don't log unless threshold is exceeded
    long thresholdMs() default 0;
}
