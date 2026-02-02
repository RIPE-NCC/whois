package net.ripe.db.whois.common.domain.io;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;

@Component("jaxRSDownloader")
public class JaxRSDownloader implements Downloader {

    private static final int TIMEOUT = 10_000;

    private final Client client;

    public JaxRSDownloader() {
        this.client = ClientBuilder.newBuilder()
            .property(ClientProperties.CONNECT_TIMEOUT, TIMEOUT)
            .property(ClientProperties.READ_TIMEOUT, TIMEOUT)
            .build();
    }

    @Override
    public void downloadToWithMd5Check(final Logger logger, final URL url, final Path path) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    @RetryFor(value = Exception.class, attempts = 10, intervalMs = 10000)
    public void downloadTo(final Logger logger, final URL url, final Path path) throws IOException {
        logger.debug("Downloading {} from {}", path, url);

        try {
            final Invocation.Builder request = client.target(url.toString()).request();

            if ("https".equals(url.getProtocol()) && ! StringUtils.isEmpty(url.getUserInfo())) {
                request.header(HttpHeaders.AUTHORIZATION,
                    String.format("Basic %s",
                        Base64.getEncoder().encodeToString(url.getUserInfo().getBytes(StandardCharsets.UTF_8))));
            }

            final Response response = request.get();

            try (final InputStream inputStream = response.readEntity(InputStream.class)) {
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
                setFileTimesAndWarn(logger, response.getHeaderString(HttpHeaders.LAST_MODIFIED), path);
            }
        } catch (Exception e) {
            logger.error("Error downloading or setting connection for url {}", url, e);
            throw e;
        }
    }


}
