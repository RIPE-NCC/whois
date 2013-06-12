package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.VersionDateTime;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;

import java.io.IOException;
import java.io.OutputStream;

public class VersionResponseObject implements ResponseObject {
    private final Operation operation;
    private final Integer version;
    private final VersionDateTime dateTime;
    private final ObjectType type;
    private final String key;
    private final int versionPadding;

    public VersionResponseObject(final int versionPadding, final Operation operation, final Integer version, final VersionDateTime dateTime, final ObjectType type, final String key) {
        this.versionPadding = versionPadding;
        this.operation = operation;
        this.version = version;
        this.dateTime = dateTime;
        this.type = type;
        this.key = key;
    }

    public Operation getOperation() {
        return operation;
    }

    public Integer getVersion() {
        return version;
    }

    public VersionDateTime getDateTime() {
        return dateTime;
    }

    public ObjectType getType() {
        return type;
    }

    public String getKey() {
        return key;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(toByteArray());
    }

    @Override
    public byte[] toByteArray() {
        return String.format("%-" + versionPadding + "d  %-16s  %-7s", version, dateTime, operation == Operation.UPDATE ? "ADD/UPD" : "DEL").getBytes();
    }

    @Override
    public String toString() {
        return new String(toByteArray());
    }
}
