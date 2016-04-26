package net.ripe.db.whois.api.transfer.logic;

public abstract class Transfer<T> {

    private final T resource;
    private final boolean income;

    protected Transfer(final T resource, final boolean income) {
        this.resource = resource;
        this.income = income;
    }

    public T getResource() {
        return resource;
    }

    public boolean isIncome() {
        return income;
    }

    public boolean isOutgoing() {
        return !isIncome();
    }


}
