package net.ripe.db.whois.update.log;

import java.io.IOException;
import java.io.OutputStream;

public interface LogCallback {
    void log(OutputStream outputStream) throws IOException;
}
