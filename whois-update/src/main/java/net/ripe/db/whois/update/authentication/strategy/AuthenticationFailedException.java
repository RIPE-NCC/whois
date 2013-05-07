package net.ripe.db.whois.update.authentication.strategy;

import net.ripe.db.whois.common.Message;
import org.apache.commons.lang.exception.NestableRuntimeException;

import java.util.Collections;
import java.util.List;

public class AuthenticationFailedException extends NestableRuntimeException {
    private final List<Message> authenticationMessages;

    public AuthenticationFailedException(final Message authenticationError) {
        this.authenticationMessages = Collections.singletonList(authenticationError);
    }

    public AuthenticationFailedException(final List<Message> authenticationMessages) {
        this.authenticationMessages = Collections.unmodifiableList(authenticationMessages);
    }

    public List<Message> getAuthenticationMessages() {
        return authenticationMessages;
    }
}
