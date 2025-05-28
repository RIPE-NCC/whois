package net.ripe.db.whois.api.mail.exception;

import org.eclipse.angus.mail.iap.ParsingException;

import java.io.Serial;

public class MailParsingException extends ParsingException {

    @Serial
    private static final long serialVersionUID = -1263525433251664387L;

    public MailParsingException(final String message) {
        super(message);
    }

    public MailParsingException(final String message, final Throwable cause) {
        super(message);
        super.initCause(cause);
    }

}
