package net.ripe.db.whois.common.credentials;

import net.ripe.db.whois.common.oauth.AbstractOAuthSession;

public class OAuthCredential implements Credential {

    private final AbstractOAuthSession offeredAbstractOAuthSession;

    private OAuthCredential(final AbstractOAuthSession offeredAbstractOAuthSession) {
        this.offeredAbstractOAuthSession = offeredAbstractOAuthSession;
    }

    public static Credential createOfferedCredential(final AbstractOAuthSession offeredAbstractOAuthSession) {
        return new OAuthCredential(offeredAbstractOAuthSession);
    }

    public AbstractOAuthSession getOfferedOAuthSession() {
        return offeredAbstractOAuthSession;
    }
    @Override
    public String toString() {
        return String.format("OAuthCredential{offeredUserSession=%s}", offeredAbstractOAuthSession);
    }
}
