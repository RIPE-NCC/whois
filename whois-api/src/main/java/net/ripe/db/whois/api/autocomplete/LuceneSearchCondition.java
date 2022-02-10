package net.ripe.db.whois.api.autocomplete;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class LuceneSearchCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if( !context.getEnvironment().containsProperty("elasticsearch.enabled")) {
            return true;
        }
        return !Boolean.parseBoolean(context.getEnvironment().getProperty("elasticsearch.enabled"));
    }
}
