package net.ripe.db.whois.update.domain;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import net.ripe.db.whois.crowd.UserSession;

public class SsoCredential implements Credential {
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ');

    private final String knownUuid;
    private final UserSession offeredUserSession;

    private SsoCredential(final String knownUuid, final UserSession offeredUserSession) {
        this.knownUuid = knownUuid;
        this.offeredUserSession = offeredUserSession;
    }

    public static SsoCredential createKnownCredential(final String auth) {
        return new SsoCredential(Iterators.getLast(SPACE_SPLITTER.split(auth).iterator()), null);
    }

    public static Credential createOfferedCredential(final UserSession offeredUserSession) {
        return new SsoCredential(null, offeredUserSession);
    }

    public String getKnownUuid() {
        return knownUuid;
    }

    public UserSession getOfferedUserSession() {
        return offeredUserSession;
    }
}
