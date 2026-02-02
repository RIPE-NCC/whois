package net.ripe.db.whois.scheduler.task.grs.scheduled;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class GrsScheduledImportCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if( !context.getEnvironment().containsProperty("grs.import.enabled")) {
            return false;
        }
        return Boolean.parseBoolean(context.getEnvironment().getProperty("grs.import.enabled"));
    }
}
