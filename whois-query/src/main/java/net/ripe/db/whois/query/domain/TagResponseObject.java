package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.domain.Tag;
import net.ripe.db.whois.query.QueryMessages;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public final class TagResponseObject implements ResponseObject {
    private final CIString objectKey;
    private final List<Tag> tags;

    public TagResponseObject(final CIString objectKey, final List<Tag> tags) {
        this.objectKey = objectKey;
        this.tags = tags;
    }

    public List<Tag> getTags() {
        return tags;
    }

    @Override
    public String toString() {
        if (tags.isEmpty()) return "";

        final StringBuilder builder = new StringBuilder(128);
        builder.append(QueryMessages.tagInfoStart(objectKey));

        for (Tag tag : tags) {
            if (tag.getType().equals("unref")) {
                builder.append(QueryMessages.unreferencedTagInfo(objectKey, tag.getValue()));
            } else {
                builder.append(QueryMessages.tagInfo(tag.getType(), tag.getValue()));
            }
        }

        return builder.toString();
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        out.write(toByteArray());
    }

    @Override
    public byte[] toByteArray() {
        if (tags.isEmpty()) return CollectionHelper.EMPTY_BYTE_ARRAY;

        return toString().getBytes();
    }
}
