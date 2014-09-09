package net.ripe.db.whois.common.support;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;

public class TelnetWhoisClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelnetWhoisClient.class);

    public static final int DEFAULT_PORT = 43;
    public static final String DEFAULT_HOST = "localhost";
    public static final Charset DEFAULT_CHARSET = Charsets.ISO_8859_1;
    private static final int DEFAULT_TIMEOUT = -1;

    private final String host;
    private final int port;

    public static String queryLocalhost(final int port, final String query) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            LOGGER.info("Query {} attempt {}", query, attempt);
            TelnetWhoisClient client = new TelnetWhoisClient("127.0.0.1", port);
            try {
                return client.sendQuery(query);
            } catch (IOException e) {
                LOGGER.warn("Query {} attempt {} failed", query, attempt);
            }
        }

        throw new IllegalStateException("Unable to execute query");
    }

    public static String queryLocalhost(final int port, final String query, final int timeoutMs) {
        try {
            return new TelnetWhoisClient("127.0.0.1", port).sendQuery(query, DEFAULT_CHARSET, timeoutMs);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to execute query");
        }
    }

    public TelnetWhoisClient(final String host) {
        this.port = DEFAULT_PORT;
        this.host = host;
    }

    public TelnetWhoisClient(final int port) {
        this.port = port;
        this.host = DEFAULT_HOST;
    }

    public TelnetWhoisClient(final String host, final int port) {
        this.port = port;
        this.host = host;
    }

    public String sendQuery(final String query) throws IOException {
        return sendQuery(query, DEFAULT_CHARSET);
    }

    public String sendQuery(final String query, final Charset charset) throws IOException {
        return sendQuery(query, charset, DEFAULT_TIMEOUT);
    }

    public String sendQuery(final String query, final Charset charset, final int timeoutMs) throws IOException {
        return sendQuery(query, passThroughFunction, charset, timeoutMs).orNull();
    }

    private final Function<BufferedReader, Optional<String>> passThroughFunction = new Function<BufferedReader, Optional<String>>() {
        @Override
        public Optional<String> apply(final BufferedReader input) {
            final StringWriter responseWriter = new StringWriter();
            try {
                FileCopyUtils.copy(input, responseWriter);
                return Optional.of(responseWriter.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @RetryFor(IOException.class)
    public Optional<String> sendQuery(final String query, final Function<BufferedReader, Optional<String>> function, final Charset charset, final int timeoutMs) throws IOException {

        try (final Socket socket = new Socket(host, port);
             final PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
             final BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset))) {

            if (timeoutMs > 0) socket.setSoTimeout(timeoutMs);

            serverWriter.println(query);
            return function.apply(serverReader);

        } catch (SocketTimeoutException | SocketException e) {
            LOGGER.warn("Error querying for {}: {}", query, e.getMessage());
            throw e;
        }
    }

    public Optional<String> sendQuery(final String query, final Function<BufferedReader, Optional<String>> function, final Charset charset) throws IOException {
        return sendQuery(query, function, charset, DEFAULT_TIMEOUT);
    }

    public Optional<String> sendQuery(final String query, final Function<BufferedReader, Optional<String>> function) throws IOException {
        return sendQuery(query, function, DEFAULT_CHARSET);
    }

}
