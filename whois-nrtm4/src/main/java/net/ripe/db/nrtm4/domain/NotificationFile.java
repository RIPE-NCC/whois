package net.ripe.db.nrtm4.domain;

public record NotificationFile(long id, long versionId, long created, String payload) {

    public static NotificationFile of(final long versionId, final long created, final String payload) {
        return new NotificationFile(0, versionId, created, payload);
    }

}
