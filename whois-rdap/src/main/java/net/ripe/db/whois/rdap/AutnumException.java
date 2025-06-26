package net.ripe.db.whois.rdap;

public class AutnumException extends RdapException {

    public AutnumException(final String errorTitle, final String errorDescription, final int errorCode){
        super(errorTitle, errorDescription, errorCode);
    }
}
