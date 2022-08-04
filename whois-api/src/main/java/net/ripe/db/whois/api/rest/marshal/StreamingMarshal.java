package net.ripe.db.whois.api.rest.marshal;

public interface StreamingMarshal {

    void open();

    void close();

    void start(String name);

    void end(String name);

    <T> void write(String name, T t);

    <T> void writeArray(T t);

    void startArray(String name);

    void endArray();

}
