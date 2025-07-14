package net.ripe.db.nrtm4;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class Nrtmv4Condition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if( !context.getEnvironment().containsProperty("nrtm4.enabled")) {
            return false;
        }
        return Boolean.parseBoolean(context.getEnvironment().getProperty("nrtm4.enabled"));
    }
}
