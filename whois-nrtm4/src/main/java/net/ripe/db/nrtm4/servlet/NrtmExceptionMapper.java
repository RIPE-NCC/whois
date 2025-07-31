package net.ripe.db.nrtm4.servlet;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Provider
@Component
public class NrtmExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmExceptionMapper.class);

    @Override
    public Response toResponse(final Exception exception) {
        if (exception instanceof BadRequestException){
            return createErrorResponse(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        }
        if (exception instanceof NotFoundException) {
            return createErrorResponse(HttpServletResponse.SC_NOT_FOUND, exception.getMessage());
        }
        if (exception instanceof NotAcceptableException) {
            return createErrorResponse(HttpServletResponse.SC_NOT_ACCEPTABLE, exception.getMessage());
        }
        if (exception instanceof NotAllowedException) {
            return createErrorResponse(HttpServletResponse.SC_METHOD_NOT_ALLOWED, exception.getMessage());
        }

        LOGGER.error("Unexpected", exception);
        return createErrorResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
    }

    private Response createErrorResponse(final int status, final String errorMessage) {
        return Response.status(status)
                .entity(errorMessage)
                .build();
    }
}
