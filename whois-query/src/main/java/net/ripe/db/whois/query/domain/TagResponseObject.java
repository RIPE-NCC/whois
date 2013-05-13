package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;

import java.io.IOException;
import java.io.OutputStream;

public class TagResponseObject implements ResponseObject {
    private final CIString objectKey;
    private final CIString type;
    private final String value;


    public TagResponseObject(final CIString objectKey, final CIString type, final String value) {
        this.objectKey = objectKey;
        this.type = type;
        this.value = value;
    }

    public CIString getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        out.write(toByteArray());
    }

    @Override
    public byte[] toByteArray() {
        if (getType().equals(CIString.ciString("unref"))) {
            return QueryMessages.unreferencedTagInfo(objectKey, getValue()).toString().getBytes();
        } else {
            return QueryMessages.tagInfo(getType(), getValue()).toString().getBytes();
        }
    }
}
