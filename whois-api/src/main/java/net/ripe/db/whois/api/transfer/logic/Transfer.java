package net.ripe.db.whois.api.transfer.logic;

public abstract class Transfer<T> {

    private final T resource;
    private final boolean incoming;

    protected Transfer(final T resource, final boolean incoming) {
        this.resource = resource;
        this.incoming = incoming;
    }

    public T getResource() {
        return resource;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public boolean isOutgoing() {
        return !isIncoming();
    }
}
