package net.ripe.db.whois.update.domain;

public interface Origin {
    boolean isDefaultOverride();

    boolean allowAdminOperations();

    String getId();

    String getFrom();

    String getResponseHeader();

    String getNotificationHeader();

    String getName();
}
