package net.ripe.db.whois.common.aspects;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RetryFor {
    Class<? extends Exception>[] value();

    int attempts() default 3;

    long intervalMs() default 1000;
}
