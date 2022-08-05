package net.ripe.db.whois.api.rest.marshal;

import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.Message;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

import static net.ripe.db.whois.api.rest.RestServiceHelper.getRequestURL;
import static net.ripe.db.whois.api.rest.domain.WhoisResources.TERMS_AND_CONDITIONS;

public abstract class AbstractStreamingMarshal implements StreamingMarshal {

    public void open() {
        // deliberately not implemented
    }

    public void start(String name) {
        // deliberately not implemented
    }

    public void end(String name) {
        // deliberately not implemented
    }

    public <T> void write(String name, T t) {
        // deliberately not implemented
    }

    public <T> void writeArray(T t) {
        // deliberately not implemented
    }

    public void startArray(String name) {
        // deliberately not implemented
    }

    public void endArray() {
        // deliberately not implemented
    }

    public <T> void throwNotFoundError(HttpServletRequest request, List<Message> errorMessages) {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(RestServiceHelper.createErrorEntity(request, errorMessages))
                .build());
    }

    public void close() {
        // deliberately not implemented
    }

    public <T> void singleton(T t) {
        // deliberately not implemented
    }

    static String createErrorStringEntity(final HttpServletRequest request,
                                                 final List<Message> errorMessages) {
        return getRequestURL(request).replaceFirst("/whois", "") + "\n"+
                createErrorStringMessages(errorMessages) + "\n" +
                TERMS_AND_CONDITIONS;
    }

    static String createErrorStringMessages(final List<Message> messages){
        StringBuilder sb = new StringBuilder();
        for (Message message : messages) {
            sb.append(message.getType()!= null ? "Severity: " + message.getType().toString() + '\n' : null);
            sb.append("Text: ").append(message.getText());
            sb.append(message.getArgs()!= null && message.getArgs().length!=0 ? Arrays.toString(message.getArgs()) : null);
        }
        return sb.toString();
    }
}
