package net.ripe.db.whois.common.domain;

import net.ripe.db.whois.common.rpsl.RpslAttribute;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * An object that can be sent back to the client. Either a message (notice,
 * warning, or error) or an RPSL object.
 */
public interface ResponseObject {
    void writeTo(OutputStream out) throws IOException;

    default void writeTo(OutputStream out, Charset charset) throws IOException {
        writeTo(out);
    }

    default void writeTo(final Writer writer, final List<RpslAttribute> rpslAttributes) throws IOException {
        for (final RpslAttribute attribute : rpslAttributes) {
            attribute.writeTo(writer);
        }

        writer.flush();
    }

    byte[] toByteArray();
}
