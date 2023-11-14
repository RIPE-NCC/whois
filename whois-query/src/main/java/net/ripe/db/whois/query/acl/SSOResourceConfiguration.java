package net.ripe.db.whois.query.acl;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SSOResourceConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SSOResourceConfiguration.class);

    private static final int UPDATE_IN_SECONDS = 120;

    private static final int DEFAULT_LIMIT = 5000;

    private final Loader loader;

    private List<String> denied;

    @Autowired
    public SSOResourceConfiguration(final Loader loader) {
        this.loader = loader;
    }

    public boolean isDenied(final String ssoId) {
        return ssoId == null ? false : denied.contains(ssoId);
    }

    public int getLimit() {
        return DEFAULT_LIMIT;
    }

    @PostConstruct
    @Scheduled(fixedDelay = UPDATE_IN_SECONDS * 1000)
    public synchronized void reload() {
        try {
            denied = loader.loadSSODenied();
        } catch (RuntimeException e) {
            LOGGER.warn("Refresh failed due to {}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    /**
     * Implement the Loader interface to load the values into the IpResourceConfiguration.
     */
    public interface Loader {
        /**
         * @return All SSO account denied entries.
         */
        List<String> loadSSODenied();
    }
}
