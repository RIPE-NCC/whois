package net.ripe.db.whois.api.rest;

interface StreamingMarshal {
    void open();

    void start(String name);

    void end(String name);

    <T> void write(String name, T t);

    void close();

    <T> void singleton(T t);
}
