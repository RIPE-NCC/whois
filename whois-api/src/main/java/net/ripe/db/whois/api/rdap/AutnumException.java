package net.ripe.db.whois.api.rdap;

public class AutnumException extends RuntimeException {

    private final String errorTitle;

    private final String errorDescription;

    private final int errorCode;

    public AutnumException(final String errorTitle, final String errorDescription, final int errorCode){
        this.errorTitle = errorTitle;
        this.errorDescription = errorDescription;
        this.errorCode = errorCode;
    }

    public String getErrorTitle() {
        return errorTitle;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public int getErrorCode() {
        return errorCode;
    }
}