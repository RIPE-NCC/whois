package net.ripe.db.nrtm4;

public class NrtmDataInconsistencyException extends RuntimeException {

    public NrtmDataInconsistencyException(final String message) {
        super(message);
    }

    public NrtmDataInconsistencyException(final Exception e) {
        super(e);
    }

    public NrtmDataInconsistencyException(final String message, final Exception e) {
        super(message, e);
    }

}
