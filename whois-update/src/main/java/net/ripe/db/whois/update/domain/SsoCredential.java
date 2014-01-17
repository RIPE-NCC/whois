package net.ripe.db.whois.update.domain;

import com.google.common.base.Splitter;
import net.ripe.db.whois.common.sso.UserSession;

public class SsoCredential implements Credential {
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');

    private final String knownUuid;
    private final UserSession offeredUserSession;

    private SsoCredential(String knownUuid, UserSession offeredUserSession) {
        this.knownUuid = knownUuid;
        this.offeredUserSession = offeredUserSession;
    }

    public static SsoCredential createKnownCredential(final String auth) {
        return new SsoCredential(SPACE_SPLITTER.split(auth).iterator().next(), null);
    }

    public static Credential createOfferedCredential(UserSession offeredUserSession) {
        return new SsoCredential(null, offeredUserSession);
    }

    public String getKnownUuid() {
        return knownUuid;
    }

    public UserSession getOfferedUserSession() {
        return offeredUserSession;
    }
}
