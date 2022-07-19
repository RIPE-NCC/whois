package net.ripe.db.whois.api.rest.marshal;

import net.ripe.db.whois.api.rest.client.StreamingException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class StreamingMarshalTextPlain implements StreamingMarshal {

    private final OutputStreamWriter outputStreamWriter;

    StreamingMarshalTextPlain(OutputStream outputStream) {
        outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1);
    }
    @Override
    public void open() {
        // deliberately not implemented
    }

    @Override
    public void start(String name) {
        // deliberately not implemented
    }

    @Override
    public void end(String name) {
        // deliberately not implemented
    }

    @Override
    public <T> void write(String name, T t) {
        // deliberately not implemented
    }

    @Override
    public <T> void writeArray(T t) {
        try {
            outputStreamWriter.write(t.toString());
            outputStreamWriter.write('\n');
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public <T> void startArray(String name) {
        // deliberately not implemented
    }

    @Override
    public <T> void endArray() {
        // deliberately not implemented
    }

    @Override
    public void close() {
        try {
            outputStreamWriter.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    @Override
    public <T> void singleton(T t) {
        try {
            outputStreamWriter.write(t.toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }
}
