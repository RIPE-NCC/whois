package net.ripe.db.whois.api.rest;

interface StreamingMarshal {
    void open();

    void start(String name);

    void end();

    <T> void write(String name, T t);

    void close();
}
