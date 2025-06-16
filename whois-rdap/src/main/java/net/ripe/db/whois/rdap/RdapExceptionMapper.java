package net.ripe.db.whois.rdap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import org.glassfish.jersey.server.ParamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        if (exception instanceof JsonProcessingException){
            return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
        if (exception instanceof ParamException){
            final String parameterName = ((ParamException) exception).getParameterName();
            return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, "Bad Request",
                    "unknown " + parameterName);
        }
        if (exception instanceof AutnumException){
            final AutnumException autnumException = (AutnumException) exception;
            return createAutnumErrorResponse(autnumException.getErrorCode(), autnumException.getErrorTitle(),
                    autnumException.getErrorDescription() == null? "Unknown error cause" :
                            autnumException.getErrorDescription());
        }

        if (exception instanceof RdapException){
            final RdapException rdapException = (RdapException) exception;
            return createErrorResponse(rdapException.getErrorCode(), rdapException.getErrorTitle(),
                    rdapException.getErrorDescription() == null? "Unknown error cause" :
                            rdapException.getErrorDescription());
        }
        if (exception instanceof NotFoundException) {
            // invalid path requested
            return createErrorResponse(HttpServletResponse.SC_NOT_FOUND, "Not Found");
        }
        if (exception instanceof NotAcceptableException) {
            return createErrorResponse(HttpServletResponse.SC_NOT_ACCEPTABLE, "Not Acceptable");
        }
        if (exception instanceof NotAllowedException) {
            return createErrorResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Not Allowed");
        }

        LOGGER.error("Unexpected", exception);
        return createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
    }

    private Response createErrorResponse(final int status, final String errorTitle, final String ... errorMessage) {
        return Response.status(status)
                .entity(createErrorEntity(status, errorTitle, errorMessage))
                .header(HttpHeaders.CONTENT_TYPE, "application/rdap+json")
                .build();
    }
    private Response createAutnumErrorResponse(final int status, final String errorTitle, final String ... errorMessage) {
        return Response.status(status)
                .entity(createAutnumErrorEntity(status, errorTitle, errorMessage))
                .header(HttpHeaders.CONTENT_TYPE, "application/rdap+json")
                .build();
    }

    private RdapObject createErrorEntity(final int errorCode, final String errorTitle, final String ... errorTexts) {
        return rdapObjectMapper.mapError(errorCode, errorTitle, Lists.newArrayList(errorTexts));
    }

    private RdapObject createAutnumErrorEntity(final int errorCode, final String errorTitle, final String ... errorTexts) {
        return rdapObjectMapper.mapAutnumError(errorCode, errorTitle, Lists.newArrayList(errorTexts));
    }
}
