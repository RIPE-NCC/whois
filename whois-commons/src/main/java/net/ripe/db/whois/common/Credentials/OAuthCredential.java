package net.ripe.db.whois.common.Credentials;

import net.ripe.db.whois.common.oauth.OAuthSession;

public class OAuthCredential implements Credential {

    private final OAuthSession offeredOAuthSession;

    private OAuthCredential(final OAuthSession offeredOAuthSession) {
        this.offeredOAuthSession = offeredOAuthSession;
    }

    public static Credential createOfferedCredential(final OAuthSession offeredOAuthSession) {
        return new OAuthCredential(offeredOAuthSession);
    }

    public OAuthSession getOfferedOAuthSession() {
        return offeredOAuthSession;
    }
    @Override
    public String toString() {
        return String.format("APIKeyCredential{offeredUserSession=%s}", offeredOAuthSession);
    }
}
