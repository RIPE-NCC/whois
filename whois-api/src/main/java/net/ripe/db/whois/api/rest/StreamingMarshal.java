package net.ripe.db.whois.api.rest;

import java.io.OutputStream;

interface StreamingMarshal {
    void open(OutputStream outputStream, String root);

    void start(String name);

    void end();

    <T> void write(String name, T t);

    void close();
}
