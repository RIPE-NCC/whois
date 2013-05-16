package net.ripe.db.whois.api.whois;

import java.io.OutputStream;

interface StreamingMarshal {
    void open(OutputStream outputStream, String... parentElementNames);

    <T> void write(String name, T t);

    void close();
}
