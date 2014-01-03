package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.update.domain.Origin;

public class InternalJob implements Origin {
    private final String id;

    public InternalJob(final String id) {
        this.id = id;
    }

    @Override
    public boolean isDefaultOverride() {
        return true;
    }

    @Override
    public boolean allowAdminOperations() {
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getFrom() {
        return id;
    }

    @Override
    public String getResponseHeader() {
        return "Maintenance job: " + getFrom() + "\n";
    }

    @Override
    public String getNotificationHeader() {
        return getResponseHeader();
    }

    @Override
    public String getName() {
        return "maintenance job";
    }

    @Override
    public String toString() {
        return "InternalJob(" + getId() + ")";
    }
}
