package net.ripe.db.whois.api.rest.marshal;

import net.ripe.db.whois.api.rest.client.StreamingException;
import net.ripe.db.whois.common.Message;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class StreamingMarshalTextPlain implements StreamingMarshal {

    private final OutputStreamWriter outputStreamWriter;

    StreamingMarshalTextPlain(OutputStream outputStream) {
        outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1);
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
    public void close() {
        try {
            outputStreamWriter.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }

    public <T> void singleton(T t) {
        try {
            outputStreamWriter.write(t.toString());
            outputStreamWriter.close();
        } catch (IOException e) {
            throw new StreamingException(e);
        }
    }
}
