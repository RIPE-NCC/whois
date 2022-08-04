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


public interface StreamingMarshal {

    default void open() {}

    void close();

    default void start(String name) {}

    default void end(String name) {}

    default <T> void write(String name, T t) {}

    <T> void writeArray(T t);

    default void startArray(String name) {}

    default void endArray() {}

    default void throwNotFoundError(HttpServletRequest request, List<Message> errorMessages) {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(RestServiceHelper.createErrorEntity(request, errorMessages))
                .build());
    }

    default <T> void singleton(T t) {}

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
