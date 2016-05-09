package net.ripe.db.whois.api.whois.rdap;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class RdapEnabled implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        return Boolean.TRUE.equals(Boolean.valueOf(context.getEnvironment().getProperty("rdap")));
    }
}
