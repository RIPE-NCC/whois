package net.ripe.db.whois.common.domain;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An object that can be sent back to the client. Either a message (notice,
 * warning, or error) or an RPSL object.
 */
public interface ResponseObject {
    void writeTo(OutputStream out) throws IOException;

    byte[] toByteArray();
}
