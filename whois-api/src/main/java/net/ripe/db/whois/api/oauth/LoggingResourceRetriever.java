package net.ripe.db.whois.api.oauth;

import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.Resource;
import com.nimbusds.jose.util.ResourceRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class LoggingResourceRetriever implements ResourceRetriever {

    private static final Logger logger =
            LoggerFactory.getLogger(LoggingResourceRetriever.class);

    private final ResourceRetriever delegate;

    public LoggingResourceRetriever(final DefaultResourceRetriever defaultResourceRetriever) {
        this.delegate = defaultResourceRetriever;
    }

    @Override
    public Resource retrieveResource(URL url) throws IOException {

        logger.debug("Fetching JWK set from URL: {}", url);

        try {
            Resource resource = delegate.retrieveResource(url);

            logger.debug(
                    "Successfully fetched JWK set from {}",
                    url
            );

            return resource;

        } catch (IOException e) {

            logger.warn(
                    "Failed to fetch JWK set from {}",
                    url,
                    e
            );

            throw e;
        }
    }
}