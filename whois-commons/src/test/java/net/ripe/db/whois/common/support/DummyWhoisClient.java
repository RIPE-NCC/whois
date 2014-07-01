package net.ripe.db.whois.common.support;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.aspects.RetryFor;
import org.apache.commons.io.IOUtils;
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

public class DummyWhoisClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummyWhoisClient.class);

    private String host;
    private int port;

    public static String query(final int port, final String query) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            LOGGER.info("Query {} attempt {}", query, attempt);
            DummyWhoisClient client = new DummyWhoisClient("127.0.0.1", port);
            try {
                return client.sendQuery(query);
            } catch (IOException e) {
                LOGGER.warn("Query {} attempt {} failed", query, attempt);
            }
        }

        throw new IllegalStateException("Unable to execute query");
    }

    public static String query(final int port, final String query, final int timeoutMs) {
        try {
            return new DummyWhoisClient("127.0.0.1", port).sendQuery(query, Charsets.ISO_8859_1, timeoutMs);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to execute query");
        }
    }


    public DummyWhoisClient(final int port) {
        this.port = port;
        this.host = "localhost";
    }

    public DummyWhoisClient(final String host, final int port) {
        this.port = port;
        this.host = host;
    }

    public String sendQuery(final String query) throws IOException {
        return sendQuery(query, Charsets.ISO_8859_1);
    }

    public String sendQuery(final String query, final Charset charset) throws IOException {
        return sendQuery(query, charset, -1);
    }

    @RetryFor(IOException.class)
    public String sendQuery(final String query, final Charset charset, final int timeoutMs) throws IOException {
        final Socket socket = new Socket(host, port);
        if (timeoutMs > 0) {
            socket.setSoTimeout(timeoutMs);
        }

        final PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
        final BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
        final StringWriter responseWriter = new StringWriter();

        serverWriter.println(query);

        try {
            FileCopyUtils.copy(serverReader, responseWriter);
        } catch (SocketTimeoutException | SocketException ignored) {
            LOGGER.warn("IO error", ignored);
        } finally {
            IOUtils.closeQuietly(serverWriter);
            IOUtils.closeQuietly(serverReader);
            IOUtils.closeQuietly(socket);
        }

        return responseWriter.toString();
    }
}
