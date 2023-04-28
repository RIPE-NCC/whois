package net.ripe.db.whois.api.nrtm4;

import net.ripe.db.whois.api.exceptions.UpgradeException;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

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

        if (exception instanceof UpgradeException) {
            return createErrorResponse(HttpStatus.UPGRADE_REQUIRED_426, exception.getMessage());
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
