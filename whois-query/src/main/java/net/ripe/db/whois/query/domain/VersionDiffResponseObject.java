package net.ripe.db.whois.query.domain;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class VersionDiffResponseObject implements ResponseObject {
    private final String formattedText;
    private final Message message;

    public VersionDiffResponseObject(final String formattedText) {
        this.formattedText = formattedText;
        this.message = null;
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        out.write(toByteArray());
    }

    @Override
    public byte[] toByteArray() {
        return formattedText.getBytes(StandardCharsets.ISO_8859_1);
    }

    @Override
    public String toString() {
        return formattedText;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final VersionDiffResponseObject that = (VersionDiffResponseObject) o;

        return Objects.equals(formattedText, that.formattedText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formattedText);
    }
}
