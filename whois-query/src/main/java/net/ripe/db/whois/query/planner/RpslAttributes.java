package net.ripe.db.whois.query.planner;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.io.ByteArrayOutput;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

// TODO: [AH] this should be moved to RpslObjectBuilder
public class RpslAttributes implements ResponseObject {
    private final Iterable<RpslAttribute> attributes;

    public RpslAttributes(final Iterable<RpslAttribute> attributes) {
        this.attributes = attributes;
    }

    @Override
    public void writeTo(final OutputStream out) throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(out, Charsets.ISO_8859_1);

        for (final RpslAttribute attribute : attributes) {
            attribute.writeTo(writer);
        }

        writer.flush();
    }

    @Override
    public byte[] toByteArray() {
        try {
            final ByteArrayOutput baos = new ByteArrayOutput();
            writeTo(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Should never occur", e);
        }
    }

    @Override
    public String toString() {
        return new String(toByteArray(), Charsets.ISO_8859_1);
    }

    public Iterable<RpslAttribute> getAttributes() {
        return attributes;
    }
}
