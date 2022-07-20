package net.ripe.db.whois.api.rest.marshal;

import net.ripe.db.whois.api.rest.RestServiceHelper;
import net.ripe.db.whois.common.Message;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.List;

public class AbstractStreamingMarshalCommon implements  StreamingMarshal{
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
        // deliberately not implemented
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
    public <T> void returnCustomError(HttpServletRequest request, List<Message> errorMessages) {
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND)
                .entity(RestServiceHelper.createErrorEntity(request, errorMessages))
                .build());
    }

    @Override
    public void close() {
        // deliberately not implemented
    }

    @Override
    public <T> void singleton(T t) {
        // deliberately not implemented
    }
}
