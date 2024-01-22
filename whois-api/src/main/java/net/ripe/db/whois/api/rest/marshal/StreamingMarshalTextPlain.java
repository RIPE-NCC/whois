package net.ripe.db.whois.api.rest.marshal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.api.rest.client.StreamingException;
import net.ripe.db.whois.common.Message;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static net.ripe.db.whois.api.rest.RestServiceHelper.getRequestURL;
import static net.ripe.db.whois.api.rest.domain.WhoisResources.TERMS_AND_CONDITIONS;


public class StreamingMarshalTextPlain implements StreamingMarshal {

    private final OutputStreamWriter outputStreamWriter;

    StreamingMarshalTextPlain(OutputStream outputStream) {
        outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.ISO_8859_1);
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
    public <T> void writeArray(T t) {
        try {
            outputStreamWriter.write(t.toString());
            outputStreamWriter.write('\n');
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

    @Override
    public void throwNotFoundError(
            final HttpServletRequest request,
            final List<Message> errorMessages
    ) {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(createErrorStringEntity(request, errorMessages))
                .build());
    }

    private String createErrorStringEntity(
            final HttpServletRequest request,
            final List<Message> errorMessages
    ) {
        return getRequestURL(request).replaceFirst("/whois", "") + "\n" +
                createErrorStringMessages(errorMessages) + "\n" +
                TERMS_AND_CONDITIONS;
    }

    private String createErrorStringMessages(final List<Message> messages) {
        StringBuilder sb = new StringBuilder();
        for (Message message : messages) {
            sb.append(message.getType() != null ? "Severity: " + message.getType().toString() + '\n' : null);
            sb.append("Text: ").append(message.getText());
            sb.append(message.getArgs() != null && message.getArgs().length != 0 ? Arrays.toString(message.getArgs()) : null);
        }
        return sb.toString();
    }

}
