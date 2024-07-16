package net.ripe.db.whois.api.conditional;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class DoSFilterCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if( !context.getEnvironment().containsProperty("dos.filter.enabled")) {
            return false;
        }
        return Boolean.parseBoolean(context.getEnvironment().getProperty("dos.filter.enabled"));
    }
}
