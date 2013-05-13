package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;
import java.io.OutputStream;

public class VersionWithRpslResponseObject implements ResponseObject {
    private final RpslObject rpslObject;
    private final int version;

    public VersionWithRpslResponseObject(final RpslObject rpslObject, final int version) {
        this.rpslObject = rpslObject;
        this.version = version;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        rpslObject.writeTo(out);
    }

    @Override
    public byte[] toByteArray() {
        return rpslObject.toByteArray();
    }
}
