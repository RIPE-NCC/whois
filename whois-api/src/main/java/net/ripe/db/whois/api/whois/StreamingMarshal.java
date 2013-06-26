package net.ripe.db.whois.api.whois;

import java.io.OutputStream;

public interface StreamingMarshal {
    void open(OutputStream outputStream);

    void start(String name);

    void end();

    <T> void write(String name, T t);

    void writeRaw(String str);

    <T> void writeObject(T t);

    void close();

}
