package net.ripe.db.whois.api.httpserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.RestMessages;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.query.domain.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXParseException;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.UnmarshalException;
import java.util.Arrays;
import java.util.List;

@Provider
@Component
public class DefaultExceptionMapper implements ExceptionMapper<Exception> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExceptionMapper.class);

    @Override
    public Response toResponse(final Exception exception) {

        if (exception instanceof IllegalSourceException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(exception.getMessage())).build();
        }

        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(exception.getMessage())).build();
        }

        if (exception instanceof WebApplicationException) {
            if (exception.getCause() instanceof UnmarshalException) {
                if (exception.getCause().getCause() instanceof SAXParseException) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(
                        createErrorEntity(RestMessages.xmlProcessingError((SAXParseException)exception.getCause().getCause()))).build();
                }
            }

            return ((WebApplicationException) exception).getResponse();
        }

        if (exception instanceof JsonProcessingException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(
                createErrorEntity(RestMessages.jsonProcessingError((JsonProcessingException)exception))).build();
        }

        if (exception instanceof EmptyResultDataAccessException) {
            return Response.status(Response.Status.NOT_FOUND).entity(createErrorEntity(Response.Status.NOT_FOUND.toString())).build();
        }

        if (exception instanceof QueryException) {
            return Response.status(Response.Status.BAD_REQUEST).entity(createErrorEntity(((QueryException) exception).getMessages())).build();
        }

        LOGGER.error("Unexpected", exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(createErrorEntity(exception.getMessage())).build();
    }

    // helper methods

    private WhoisResources createErrorEntity(final String message) {
        return createErrorEntity(new Message(Messages.Type.ERROR, message));
    }

    private WhoisResources createErrorEntity(final Message ... messages) {
        return createErrorEntity(Arrays.asList(messages));
    }

    // TODO: [AH] no locator URI for error messages
    private WhoisResources createErrorEntity(final Iterable<Message> messages) {
        final WhoisResources whoisResources = new WhoisResources();

        final List<ErrorMessage> errorMessages = Lists.newArrayList();
        for (Message message : messages) {
            errorMessages.add(new ErrorMessage(message));
        }
        whoisResources.setErrorMessages(errorMessages);
        whoisResources.includeTermsAndConditions();
        return whoisResources;
    }

}
