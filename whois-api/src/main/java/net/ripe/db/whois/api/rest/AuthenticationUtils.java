package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.oauth.AbstractOAuthSession;
import net.ripe.db.whois.common.oauth.OidcSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Nullable;

public class AuthenticationUtils {

    @Nullable
    public static AbstractOAuthSession getOauthSession() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AbstractOAuthSession)) {
            return null;
        }
        return (AbstractOAuthSession) authentication.getPrincipal();
    }

    @Nullable
    public static OidcSession getOidcSession() {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof OidcSession)) {
            return null;
        }
        return (OidcSession) authentication.getPrincipal();
    }
}
