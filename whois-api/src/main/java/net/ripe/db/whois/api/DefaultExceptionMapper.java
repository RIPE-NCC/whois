package net.ripe.db.whois.api;

import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.client.ClientHandlerException;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.query.domain.QueryException;
import org.codehaus.jackson.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Component
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(final Exception exception) {
        if (exception instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        if (exception instanceof IllegalSourceException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(exception.getMessage()).build();
        }

        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }

        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        if (exception instanceof JsonProcessingException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).build();
        }

        if (exception instanceof ClientHandlerException) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(exception.getMessage()).build();
        }

        if (exception instanceof EmptyResultDataAccessException) {
            return Response.status(HttpServletResponse.SC_NOT_FOUND).build();
        }

        if (exception instanceof QueryException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(exception.getMessage()).build();
        }

        LOGGER.error("Unexpected", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}
