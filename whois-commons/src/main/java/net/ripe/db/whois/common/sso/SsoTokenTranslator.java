package net.ripe.db.whois.common.sso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class SsoTokenTranslator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SsoTokenTranslator.class);

    private final CrowdClient crowdClient;

    @Autowired
    public SsoTokenTranslator(final CrowdClient crowdClient) {
        this.crowdClient = crowdClient;
    }

    @Nullable
    public UserSession translateSsoToken(final String ssoToken) {
        try {
            final UserSession userSession = crowdClient.getUserSession(ssoToken);
            if (!userSession.isActive()) {
                LOGGER.info("SSO token deactivated: " + ssoToken);
                return null;
            }
            userSession.setUuid(crowdClient.getUuid(userSession.getUsername()));
            return userSession;
        } catch (IllegalArgumentException e) {
            LOGGER.info("can't translate sso token", e);
            return null;
        }
    }
}
