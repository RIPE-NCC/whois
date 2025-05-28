package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.FormatHelper;
import net.ripe.db.whois.update.domain.Origin;

public class SyncUpdate implements Origin {
    private final DateTimeProvider dateTimeProvider;
    private final String remoteAddress;

    public SyncUpdate(final DateTimeProvider dateTimeProvider, final String remoteAddress) {
        this.dateTimeProvider = dateTimeProvider;
        this.remoteAddress = remoteAddress;
    }

    @Override
    public boolean isDefaultOverride() {
        return false;
    }

    @Override
    public boolean allowAdminOperations() {
        return true;
    }

    @Override
    public String getId() {
        return getFrom();
    }

    @Override
    public String getFrom() {
        return remoteAddress;
    }

    @Override
    public String getResponseHeader() {
        return getHeader();
    }

    @Override
    public String getNotificationHeader() {
        return getHeader();
    }

    private String getHeader() {
        return String.format("" +
                " - From-Host: %s\n" +
                " - Date/Time: %s\n",
                remoteAddress,
                FormatHelper.dayDateTimeToUtcString(dateTimeProvider.getCurrentDateTime()));
    }

    @Override
    public String getName() {
        return "sync update";
    }

    @Override
    public String toString() {
        return "SyncUpdate(" + getId() + ")";
    }
}
