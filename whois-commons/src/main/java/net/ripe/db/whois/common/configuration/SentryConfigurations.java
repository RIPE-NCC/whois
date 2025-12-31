package net.ripe.db.whois.common.configuration;

import io.sentry.Sentry;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.ClientErrorException;
import net.ripe.db.whois.common.ApplicationVersion;
import net.ripe.db.whois.common.Environment;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SentryConfigurations {

    private static final Logger LOGGER = LoggerFactory.getLogger(SentryConfigurations.class);

    private final ApplicationVersion applicationVersion;
    private final String sentryDsn;
    private Environment environment;

    @Autowired
    public SentryConfigurations(
            @Value("${sentry.dsn:}") final String sentryDsn,
            @Value("${whois.environment:}") final String environment,
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
            options.addIgnoredExceptionForType(ClientErrorException.class);
        });
    }
}
