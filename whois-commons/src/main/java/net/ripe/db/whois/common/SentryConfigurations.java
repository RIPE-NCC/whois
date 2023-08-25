package net.ripe.db.whois.common;

import io.sentry.Sentry;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SentryConfigurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentryConfigurations.class);

    private final ApplicationVersion applicationVersion;
    private final String sentryDsn;
    private Environment environment;

    @Autowired
    public SentryConfigurations(
            @Value("${sentry.dsn:}") final String sentryDsn,
            @Value("${sentry.environment:}") final String environment,
            final ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
        this.sentryDsn = sentryDsn;
        this.environment = null;
        try {
            this.environment = Environment.valueOf(environment.toUpperCase());
        } catch (IllegalArgumentException ex){
            // We do not set any environment or return an error in this case. Will be tackle in the init method
        }
    }

    @PostConstruct
    public void init() {
        if(StringUtils.isEmpty(sentryDsn) || environment == null) {
            LOGGER.info("Sentry is not enabled");
            return;
        }
        Sentry.init(options -> {
            options.setRelease(String.format("%s@%s",environment, applicationVersion.getCommitId()));
            options.setDsn(sentryDsn);
            options.setEnvironment(environment.name().toLowerCase());
        });
    }
}
