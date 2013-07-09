package net.ripe.db.whois.common.profiles;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WhoisVariantContext {

    /**
     * The whois variant for which this component will be included in the spring container if the value is set.
     */
    WhoisVariant.Type includeWhen() default WhoisVariant.Type.NONE;

    /**
     * The whois variant for which this component will be excluded in the spring container if the value is set.
     */
    WhoisVariant.Type excludeWhen() default WhoisVariant.Type.NONE;

}
