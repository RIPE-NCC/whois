package net.ripe.db.whois.api.rdap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.query.domain.QueryCompletionInfo;
import net.ripe.db.whois.query.domain.QueryException;
import org.glassfish.jersey.server.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
@Component
public class RdapExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RdapExceptionMapper.class);

    private final RdapObjectMapper rdapObjectMapper;

    @Autowired
    public RdapExceptionMapper(
            final RdapObjectMapper rdapObjectMapper) {
        this.rdapObjectMapper = rdapObjectMapper;
    }

    @Override
    public Response toResponse(final Exception exception) {
        final String cause = exception.getCause() != null ? exception.getCause().getMessage() : "Unknown error cause";
        if (exception instanceof IllegalSourceException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage(), cause)).build();
        }

        if (exception instanceof IllegalArgumentException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage(), cause)).build();
        }

        if (exception instanceof ParamException) {
            String parameterName = ((ParamException) exception).getParameterName();
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, "400 Bad Request", "unknown " + parameterName)).build();
        }

        if (exception instanceof NotFoundException) {
            return createErrorResponse(Response.Status.NOT_FOUND, exception.getMessage(), cause);
        }

        if (exception instanceof BadRequestException) {
            return createErrorResponse(Response.Status.BAD_REQUEST, exception.getMessage(), cause);
        }

        if (exception instanceof WebApplicationException) {
            return createErrorResponse(((WebApplicationException) exception).getResponse().getStatus(), exception.getMessage(), cause);
        }

        if (exception instanceof JsonProcessingException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage(), cause)).build();
        }

        if (exception instanceof EmptyResultDataAccessException) {
            return Response.status(HttpServletResponse.SC_NOT_FOUND).entity(createErrorEntity(HttpServletResponse.SC_NOT_FOUND, "404 Not Found", cause)).build();
        }

        if (exception instanceof QueryException) {
            if ( ((QueryException) exception).getCompletionInfo() == QueryCompletionInfo.BLOCKED) {
                return createErrorResponse(Response.Status.TOO_MANY_REQUESTS, exception.getMessage(), cause);
            }
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage(), cause)).build();
        }

        if (exception instanceof IllegalStateException) {
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(createErrorEntity(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage(), cause)).build();
        }

        LOGGER.error("Unexpected", exception);
        return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(createErrorEntity(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage(), cause)).build();
    }

    private RdapObject createErrorEntity(final int errorCode, final String errorTitle, final String errorTexts) {
        return rdapObjectMapper.mapError(errorCode, errorTitle, Lists.newArrayList(errorTexts));
    }

    private Response createErrorResponse(final Response.Status status, final String errorTitle, final String errorMessage) {
        return createErrorResponse(status.getStatusCode(), errorTitle, errorMessage);
    }

    private Response createErrorResponse(final int status, final String errorTitle, final String errorMessage) {
        return Response.status(status)
                .entity(createErrorEntity(status, errorTitle, errorMessage))
                .header(HttpHeaders.CONTENT_TYPE, "application/rdap+json")
                .build();
    }
}
