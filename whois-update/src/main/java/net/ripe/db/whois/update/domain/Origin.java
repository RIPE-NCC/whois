package net.ripe.db.whois.update.domain;

public interface Origin {
    boolean isDefaultOverride();

    boolean allowRipeOperations();

    String getId();

    String getFrom();

    String getResponseHeader();

    String getNotificationHeader();

    String getName();
}
