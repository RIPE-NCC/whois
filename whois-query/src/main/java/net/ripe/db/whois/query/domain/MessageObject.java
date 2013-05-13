package net.ripe.db.whois.query.domain;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;

import java.io.IOException;
import java.io.OutputStream;

public class MessageObject implements ResponseObject {
    private final String text;

    public MessageObject(final String text) {
        this.text = text;
    }

    public MessageObject(final Message message) {
        this(message.toString());
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        out.write(toByteArray());
    }

    @Override
    public byte[] toByteArray() {
        return text.getBytes(Charsets.UTF_8);
    }

    @Override
    public String toString() {
        return text;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageObject that = (MessageObject) o;
        return text.equals(that.text);

    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
