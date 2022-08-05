package net.ripe.db.whois.api.rest.marshal;

import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.Message;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;


public interface StreamingMarshal {

    default void open() {}

    void close();

    default void startArray(String name) {}

    default void endArray() {}

    default void start(String name) {}

    default void end(String name) {}

    <T> void writeArray(T t);

    default <T> void write(String name, T t) {}

    default <T> void singleton(T t) {}

    default void throwNotFoundError(HttpServletRequest request, List<Message> errorMessages) {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(RestServiceHelper.createErrorEntity(request, errorMessages))
                .build());
    }

}
