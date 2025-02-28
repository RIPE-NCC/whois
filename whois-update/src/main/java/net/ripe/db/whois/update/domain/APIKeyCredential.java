package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.apiKey.OAuthSession;
import net.ripe.db.whois.common.sso.UserSession;

public class APIKeyCredential implements Credential {

    private final OAuthSession offeredOAuthSession;

    private APIKeyCredential(final OAuthSession offeredOAuthSession) {
        this.offeredOAuthSession = offeredOAuthSession;
    }

    public static Credential createOfferedCredential(final OAuthSession offeredOAuthSession) {
        return new APIKeyCredential(offeredOAuthSession);
    }

    public OAuthSession getOfferedOAuthSession() {
        return offeredOAuthSession;
    }
    @Override
    public String toString() {
        return String.format("APIKeyCredential{offeredUserSession=%s}", offeredOAuthSession);
    }
}
