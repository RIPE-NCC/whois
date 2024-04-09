package net.ripe.db.whois.api.mail.exception;

import org.eclipse.angus.mail.iap.ParsingException;

public class MailParsingException extends ParsingException {

    protected final String message;

    public MailParsingException(final String message) {
        super(message);
        this.message = message;
    }

}
