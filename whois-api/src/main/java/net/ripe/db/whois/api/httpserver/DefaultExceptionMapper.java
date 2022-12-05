package net.ripe.db.whois.api.httpserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ripe.db.whois.api.rest.RestMessages;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXParseException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.UnmarshalException;

import static net.ripe.db.whois.api.rest.RestServiceHelper.createError;
import static net.ripe.db.whois.api.rest.RestServiceHelper.createErrorObjectEntity;
import static net.ripe.db.whois.api.rest.RestServiceHelper.createErrorWhoisEntity;

@Provider
@Component
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Context
    private HttpHeaders headers;

    @Context
    private HttpServletRequest request;

    @Override
    public Response toResponse(final Exception exception) {

        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createError(request,
                    new Message(Messages.Type.ERROR, exception.getMessage()))).build();
        }

        if (exception instanceof WebApplicationException) {
            if (exception.getCause() instanceof UnmarshalException) {
                if (exception.getCause().getCause() instanceof SAXParseException) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                            createErrorObjectEntity(RestMessages.xmlProcessingError((SAXParseException)exception.getCause().getCause()))).build();
                }
            }
            return ((WebApplicationException) exception).getResponse();
        }

        if (exception instanceof JsonProcessingException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    createErrorObjectEntity(RestMessages.jsonProcessingError((JsonProcessingException)exception))).build();
        }

        if (exception instanceof EmptyResultDataAccessException) {
            return Response.status(Response.Status.NOT_FOUND).entity(createError(request,
                    new Message(Messages.Type.ERROR, exception.getMessage()))).build();
        }

        if (exception instanceof QueryException) {
            if (((QueryException) exception).getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                return Response.status(Response.Status.TOO_MANY_REQUESTS).entity(createErrorWhoisEntity(((QueryException) exception).getMessages())).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorWhoisEntity(((QueryException) exception).getMessages())).build();
        }

        if (exception instanceof IllegalStateException) {
            if (LocalizationMessages.FORM_PARAM_CONTENT_TYPE_ERROR().equals(exception.getMessage())) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
        LOGGER.error("Unexpected", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createError(request,
                new Message(Messages.Type.ERROR, exception.getMessage()))).build();
    }
}
