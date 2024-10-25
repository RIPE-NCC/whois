package net.ripe.db.nrtm4.client.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class Nrtm4ClientCondition implements Condition {

    private static final Logger LOGGER = LoggerFactory.getLogger(Nrtm4ClientCondition.class);

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if( !context.getEnvironment().containsProperty("nrtm4.client.enabled")) {
            LOGGER.info("Nrtm4 client is not enabled");
            return false;
        }
        LOGGER.info("Nrtm4 client is enabled {}", Boolean.parseBoolean(context.getEnvironment().getProperty("nrtm4.client.enabled")));
        return Boolean.parseBoolean(context.getEnvironment().getProperty("nrtm4.client.enabled"));
    }
}
