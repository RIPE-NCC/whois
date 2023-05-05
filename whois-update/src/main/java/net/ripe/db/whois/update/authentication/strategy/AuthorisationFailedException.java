package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.exception.NestableRuntimeException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AuthorisationFailedException extends NestableRuntimeException {
    private final Collection<Message> authorisationMessages;
    private final Collection<RpslObject> candidates;

    public AuthorisationFailedException(final Message authenticationError, final Collection<RpslObject> candidates) {
        this.authorisationMessages = Collections.singletonList(authenticationError);
        this.candidates = candidates;
    }

    public AuthorisationFailedException(final List<Message> authenticationMessages, final Collection<RpslObject> candidates) {
        this.authorisationMessages = Collections.unmodifiableList(authenticationMessages);
        this.candidates = candidates;
    }

    public Collection<Message> getAuthorisationMessages() {
        return authorisationMessages;
    }

    public Collection<RpslObject> getCandidates() {
        return candidates;
    }
}
