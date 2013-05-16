package net.ripe.db.whois.api.whois;

import java.io.OutputStream;

interface StreamingMarshal {
    void open(OutputStream outputStream);

    void start(String name);

    void end();

    <T> void write(String name, T t);

    void close();

}
