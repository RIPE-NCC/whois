package net.ripe.db.whois.update.autokey;

import net.ripe.db.whois.common.Message;

public class ClaimException extends Exception {
    private final Message errorMessage;

    public ClaimException(final Message errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Message getErrorMessage() {
        return errorMessage;
    }
}
