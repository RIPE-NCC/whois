package net.ripe.db.nrtm4.client.scheduler;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class Nrtmv4ClientCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if( !context.getEnvironment().containsProperty("nrtm4.client.enabled")) {
            return false;
        }
        return Boolean.parseBoolean(context.getEnvironment().getProperty("nrtm4.client.enabled"));
    }
}
