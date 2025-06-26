package net.ripe.db.nrtm4.domain;

public record NotificationFile(long id, long versionId, long created, String payload) {

    public static NotificationFile of(final long versionId, final long created, final String payload) {
        return new NotificationFile(0, versionId, created, payload);
    }

    public static NotificationFile of(final long id, final long versionId, final long created, final String payload) {
        return new NotificationFile(id, versionId, created, payload);
    }

    public static NotificationFile of(final NotificationFile notificationFile, final long created) {
        return NotificationFile.of(notificationFile.versionId(), created, notificationFile.payload());
    }

}
