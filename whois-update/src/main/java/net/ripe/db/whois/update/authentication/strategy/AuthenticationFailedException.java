package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.exception.NestableRuntimeException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AuthenticationFailedException extends NestableRuntimeException {
    private final Collection<Message> authenticationMessages;
    private final Collection<RpslObject> candidates;

    public AuthenticationFailedException(final Message authenticationError, final Collection<RpslObject> candidates) {
        this.authenticationMessages = Collections.singletonList(authenticationError);
        this.candidates = candidates;
    }

    public AuthenticationFailedException(final List<Message> authenticationMessages, final Collection<RpslObject> candidates) {
        this.authenticationMessages = Collections.unmodifiableList(authenticationMessages);
        this.candidates = candidates;
    }

    public Collection<Message> getAuthenticationMessages() {
        return authenticationMessages;
    }

    public Collection<RpslObject> getCandidates() {
        return candidates;
    }
}
