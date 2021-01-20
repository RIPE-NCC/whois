package net.ripe.db.whois.api.rdap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.common.source.IllegalSourceException;
import net.ripe.db.whois.query.domain.QueryException;
import org.glassfish.jersey.server.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

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

        if (exception instanceof IllegalSourceException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage())).build();
        }

        if (exception instanceof IllegalArgumentException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage())).build();
        }

        if (exception instanceof ParamException) {
            String parameterName = ((ParamException) exception).getParameterName();
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, "unknown " + parameterName)).build();
        }

        if (exception instanceof NotFoundException) {
            return createErrorResponse(Response.Status.NOT_FOUND, exception.getMessage());
        }

        if (exception instanceof BadRequestException) {
            return createErrorResponse(Response.Status.BAD_REQUEST, exception.getMessage());
        }

        if (exception instanceof WebApplicationException) {
            return createErrorResponse(((WebApplicationException) exception).getResponse().getStatus(), exception.getMessage());
        }

        if (exception instanceof JsonProcessingException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage())).build();
        }

        if (exception instanceof EmptyResultDataAccessException) {
            return Response.status(HttpServletResponse.SC_NOT_FOUND).entity(createErrorEntity(HttpServletResponse.SC_NOT_FOUND, "not found")).build();
        }

        if (exception instanceof QueryException) {
            return Response.status(HttpServletResponse.SC_BAD_REQUEST).entity(createErrorEntity(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage())).build();
        }

        if (exception instanceof IllegalStateException) {
            return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(createErrorEntity(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage())).build();
        }

        LOGGER.error("Unexpected", exception);
        return Response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR).entity(createErrorEntity(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage())).build();
    }

    private RdapObject createErrorEntity(final int errorCode, final String errorTitle, String ... errorTexts) {
        return rdapObjectMapper.mapError(errorCode, errorTitle, Lists.newArrayList(errorTexts));
    }

    private Response createErrorResponse(final Response.Status status, final String errorTitle) {
        return createErrorResponse(status.getStatusCode(), errorTitle);
    }

    private Response createErrorResponse(final int status, final String errorTitle) {
        return Response.status(status)
                .entity(createErrorEntity(status, errorTitle))
                .header(HttpHeaders.CONTENT_TYPE, "application/rdap+json")
                .build();
    }
}
