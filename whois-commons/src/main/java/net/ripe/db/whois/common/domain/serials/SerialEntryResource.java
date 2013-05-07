package net.ripe.db.whois.common.domain.serials;

public class SerialEntryResource<T> {
    private final Operation operation;
    private final T resource;

    public SerialEntryResource(final Operation operation, final T resource) {
        this.operation = operation;
        this.resource = resource;
    }

    public Operation getOperation() {
        return operation;
    }

    public T getResource() {
        return resource;
    }
}
