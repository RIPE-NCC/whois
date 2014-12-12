package net.ripe.db.whois.query.domain;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.ResponseObject;

import java.io.IOException;
import java.io.OutputStream;

public class MessageObject implements ResponseObject {
    private final String formattedText;
    private final Message message;

    public MessageObject(final String formattedText) {
        this.formattedText = formattedText;
        this.message = null;
    }

    public MessageObject(final Message message) {
        this.formattedText = message.getFormattedText();
        this.message = message;
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
        return formattedText.getBytes(Charsets.UTF_8);
    }

    @Override
    public String toString() {
        return formattedText;
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
        return formattedText.equals(that.formattedText);
    }

    @Override
    public int hashCode() {
        return formattedText.hashCode();
    }
}
