package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


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
    public void writeTo(final OutputStream out, final Charset charset) throws IOException {
        writeTo(new OutputStreamWriter(out, charset), rpslObject.getAttributes());
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        rpslObject.writeTo(out, StandardCharsets.UTF_8);
    }


    @Override
    public byte[] toByteArray() {
        return rpslObject.toByteArray();
    }
}
