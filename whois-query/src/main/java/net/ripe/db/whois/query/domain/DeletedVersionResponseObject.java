package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.VersionDateTime;
import net.ripe.db.whois.common.rpsl.ObjectType;

import java.io.IOException;
import java.io.OutputStream;

public class DeletedVersionResponseObject implements ResponseObject {
    private final VersionDateTime deletedDate;
    private final ObjectType type;
    private final String key;

    public DeletedVersionResponseObject(final VersionDateTime deletedDate, final ObjectType type, final String key) {
        this.deletedDate = deletedDate;
        this.type = type;
        this.key = key;
    }

    public VersionDateTime getDeletedDate() {
        return deletedDate;
    }

    public ObjectType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        out.write(toByteArray());
    }

    @Override
    public byte[] toByteArray() {
        return QueryMessages.versionDeleted(deletedDate.toString()).toString().getBytes();
    }
}
