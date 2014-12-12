package net.ripe.db.whois.api.rest;

public interface StreamingMarshal {
    void open();

    void start(String name);

    void end(String name);

    <T> void write(String name, T t);

    <T> void writeArray(T t);
    <T> void startArray(String name);
    <T> void endArray();

    void close();

    // TODO: [AH] handle streaming on a higher level; e.g. have strategies for different object types (WhoisObjectStreamer) and input (streaming query, from memory)
    <T> void singleton(T t);
}
