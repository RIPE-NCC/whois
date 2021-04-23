package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.Message;

public class NrtmException extends RuntimeException {

    private final String message;

    public NrtmException(final Message message) {
        this.message = message.toString();
    }

    public NrtmException(final String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
