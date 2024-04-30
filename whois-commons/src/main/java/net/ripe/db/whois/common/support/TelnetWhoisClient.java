package net.ripe.db.whois.common.support;

import net.ripe.db.whois.common.aspects.RetryFor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class TelnetWhoisClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(TelnetWhoisClient.class);

    public static final int DEFAULT_PORT = 43;
    public static final String DEFAULT_HOST = "localhost";
    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    private static final int DEFAULT_TIMEOUT = (int)TimeUnit.MINUTES.toMillis(5);
    private final String host;
    private final int port;

    private final int timeout;

    private final Charset charset;


    public TelnetWhoisClient(final String host) {
        this(host, DEFAULT_PORT, DEFAULT_CHARSET, DEFAULT_TIMEOUT);
    }

    public TelnetWhoisClient(final int port) {
        this(DEFAULT_HOST, port, DEFAULT_CHARSET, DEFAULT_TIMEOUT);
    }

    public TelnetWhoisClient(final String host, final int port) {
        this(host, port, DEFAULT_CHARSET, DEFAULT_TIMEOUT);
    }

    public TelnetWhoisClient(final int port, final Charset charset) {
        this(DEFAULT_HOST, port, charset, DEFAULT_TIMEOUT);
    }

    public TelnetWhoisClient(final String host, final int port, final Charset charset) {
        this(host, port, charset, DEFAULT_TIMEOUT);
    }

    public TelnetWhoisClient(final String host, final int port, final Charset charset, final int timeout) {
        this.port = port;
        this.host = host;
        this.charset = charset;
        this.timeout = timeout;

    }

    public static String queryLocalhost(final int port, final String query) {
        TelnetWhoisClient client = new TelnetWhoisClient("127.0.0.1", port, DEFAULT_CHARSET);
        return client.sendQuery(query);
    }

    public static String queryLocalhost(final int port, final String query, final int timeoutMs) {
        return new TelnetWhoisClient("127.0.0.1", port, DEFAULT_CHARSET).sendQuery(query, timeoutMs);
    }

    public String sendQuery(final String query) {
        return sendQuery(query, timeout);
    }

    /**
     * Sends query, reads server's reply using specified charset and socket timeout (SO_TIMEOUT), and returns it as a String.
     * @param query string to send to the server (without trailing <CR><LF>)
     * @param timeoutMs timeout in milliseconds. 0 means never time out. Specify -1 to use system timeout.
     * @return
     */
    @Nullable
    public String sendQuery(final String query, final int timeoutMs) {
        return sendQuery(query, passThroughFunction, timeoutMs).orElse(null);
    }

    private final Function<BufferedReader, Optional<String>> passThroughFunction = new Function<BufferedReader, Optional<String>>() {
        @Override
        public Optional<String> apply(final BufferedReader input) {
            final StringWriter responseWriter = new StringWriter();
            try {
                FileCopyUtils.copy(input, responseWriter);
                return Optional.of(responseWriter.toString());
            } catch (IOException e) {
                // will not be retried in case of error while reading reply
                // also supports keep-alive mode (-k, NRTM), by returning what has been received so far upon SocketTimeoutException
                LOGGER.info(e.getMessage());
                return Optional.of(responseWriter.toString());
            }
        }
    };

    public Optional<String> sendQuery(final String query, final Function<BufferedReader, Optional<String>> function, final int timeoutMs) {
        try {
            return sendQueryWithRetry(query, function, timeoutMs);
        } catch (IOException e) {
            final String message = String.format("Error querying for '%s' at '%s':%d %s", query, host, port, e.getMessage());
            throw new IllegalStateException(message, e);
        }
    }

    @RetryFor(IOException.class)
    private Optional<String> sendQueryWithRetry(final String query, final Function<BufferedReader, Optional<String>> function, final int timeoutMs) throws IOException {

        try (final Socket socket = new Socket(host, port);
             final PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
             final BufferedReader serverReader =
                     new BufferedReader(new InputStreamReader(socket.getInputStream(), charset))) {

            if (timeoutMs >= 0) socket.setSoTimeout(timeoutMs);

            serverWriter.print(query + "\r\n");
            serverWriter.flush();
            return function.apply(serverReader);
        }
    }
}
