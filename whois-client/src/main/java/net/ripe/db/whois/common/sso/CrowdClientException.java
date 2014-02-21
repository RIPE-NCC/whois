package net.ripe.db.whois.common.sso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;

public class CrowdClientException extends RuntimeException {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdClientException.class);

    public CrowdClientException(final String message) {
        super(message);
    }

    public CrowdClientException(final Exception e) {
        super(getMessage(e), e);
    }

    private static String getMessage(final Exception e) {
        if (e instanceof ClientErrorException) {
            final CrowdClient.CrowdError crowdError = ((ClientErrorException)e).getResponse().readEntity(CrowdClient.CrowdError.class);
            LOGGER.info("{} : {} ({})", e.getClass().getName(), crowdError.getMessage(), crowdError.getReason());
            return crowdError.getMessage();
        } else {
            if (e instanceof WebApplicationException) {
                final String cause = String.format("%s (%s)", e.getClass().getName(), ((WebApplicationException)e).getResponse().readEntity(String.class));
                LOGGER.info(cause);
                return cause;
            } else {
                final String cause = String.format("%s (%s)", e.getClass().getName(), e.getMessage());
                LOGGER.info(cause);
                return cause;
            }
        }
    }


}
