package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.sso.SsoTokenTranslator;
import net.ripe.db.whois.common.sso.UserSession;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.Nullable;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Component
public class VersionsInternalUserResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionsInternalUserResolver.class);
    private final SsoTokenTranslator ssoTokenTranslator;
    private final IpRanges ipRanges;
    private final Set<String> allowedEmails;

    @Autowired
    public VersionsInternalUserResolver(
            final SsoTokenTranslator ssoTokenTranslator,
            final IpRanges ipRanges,
            @Value("${versions.internal.emails:}") final String... allowedEmails) {
        this.ssoTokenTranslator = ssoTokenTranslator;
        this.ipRanges = ipRanges;
        this.allowedEmails = new HashSet<>();

        for (String email : allowedEmails) {
            if (!email.isBlank()) {
                this.allowedEmails.add(email.trim().toLowerCase(Locale.ROOT));
            }
        }
    }

    public boolean isInternalUser(@Nullable final String crowdTokenKey, final InetAddress remoteAddress) {
        if (!isTrustedAddress(remoteAddress)) {
            return false;
        }

        final UserSession userSession = getActiveUserSession(crowdTokenKey);

        if (userSession == null
                || userSession.getUsername() == null
                || !this.allowedEmails.contains(userSession.getUsername().trim().toLowerCase(Locale.ROOT))) {
            return false;
        }

        LOGGER.info("Internal user {} granted full version history access from {}",
                userSession.getUsername(), remoteAddress.getHostAddress());

        return true;
    }

    private boolean isTrustedAddress(final InetAddress remoteAddress) {
        return ipRanges.isTrusted(IpInterval.asIpInterval(remoteAddress));
    }

    @Nullable
    private UserSession getActiveUserSession(@Nullable final String crowdTokenKey) {
        if (StringUtils.isEmpty(crowdTokenKey)) {
            return null;
        }

        final UserSession userSession = ssoTokenTranslator.translateSsoTokenOrNull(crowdTokenKey);

        return (userSession != null && userSession.isActive()) ? userSession : null;
    }
}