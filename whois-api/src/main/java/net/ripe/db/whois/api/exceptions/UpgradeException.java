package net.ripe.db.whois.api.exceptions;

import javax.ws.rs.WebApplicationException;

public class UpgradeException extends WebApplicationException {

    private static final long serialVersionUID = -6362082162191216040L;

    public UpgradeException(String message) {
        super(message);
    }
}
