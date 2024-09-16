package net.ripe.db.whois.common.domain;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * An object that can be sent back to the client. Either a message (notice,
 * warning, or error) or an RPSL object.
 */
public interface ResponseObject {
    void writeTo(OutputStream out) throws IOException;
    default void writeTo(OutputStream out, Charset charset) throws IOException {
        writeTo(out);
    }
    byte[] toByteArray();
}
