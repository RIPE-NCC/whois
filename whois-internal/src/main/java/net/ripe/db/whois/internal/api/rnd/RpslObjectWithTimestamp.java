package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.query.VersionDateTime;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class RpslObjectWithTimestamp implements ResponseObject {
    private final RpslObject rpslObject;
    private final int sameTimestampCount;
    private final VersionDateTime versionDateTime;
    private List<ObjectVersion> outgoing;
    private List<ObjectVersion> incoming;

    public RpslObjectWithTimestamp(final RpslObject rpslObject, final int sameTimestampCount, final VersionDateTime versionDateTime) {
        this.rpslObject = rpslObject;
        this.sameTimestampCount = sameTimestampCount;
        this.versionDateTime = versionDateTime;
    }

    public RpslObjectWithTimestamp(final RpslObject rpslObject, final int sameTimestampCount, final VersionDateTime versionDateTime,
                                   final List<ObjectVersion> outgoing, List<ObjectVersion> incoming) {
        this(rpslObject, sameTimestampCount, versionDateTime);
        this.outgoing = outgoing;
        this.incoming = incoming;
    }

    public RpslObject getRpslObject() {
        return rpslObject;
    }

    public int getSameTimestampCount() {
        return sameTimestampCount;
    }

    public VersionDateTime getVersionDateTime() {
        return versionDateTime;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        rpslObject.writeTo(out);
    }

    @Override
    public byte[] toByteArray() {
        return rpslObject.toByteArray();
    }

    public List<ObjectVersion> getOutgoing() {
        return outgoing;
    }

    public List<ObjectVersion> getIncoming() {
        return incoming;
    }
}
