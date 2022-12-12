package net.ripe.db.nrtm4.persist;

public class NotificationFile {
    private final long id;
    private final long versionId;
    private final String payload;
    private final long created;

    public NotificationFile(final long id, final long versionId, final String payload, final long created) {
        this.id = id;
        this.versionId = versionId;
        this.payload = payload;
        this.created = created;
    }

    public long getId() {
        return id;
    }

    public long getVersionId() {
        return versionId;
    }

    public String getPayload() {
        return payload;
    }

    public long getCreated() {
        return created;
    }

}
