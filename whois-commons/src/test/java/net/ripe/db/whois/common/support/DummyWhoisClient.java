package net.ripe.db.whois.common.support;

import net.ripe.db.whois.common.aspects.RetryFor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.*;
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

    public static String query(final int port, final String query, final int timeout) {
        try {
            return new DummyWhoisClient("127.0.0.1", port).sendQuery(query, Charset.forName("ISO-8859-1"), timeout);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to execute query");
        }
    }


    public DummyWhoisClient(int port) {
        this.port = port;
        this.host = "localhost";
    }

    public DummyWhoisClient(String host, int port) {
        this.port = port;
        this.host = host;
    }

    public String sendQuery(String query) throws IOException {
        return sendQuery(query, Charset.forName("ISO-8859-1"));
    }

    public String sendQuery(String query, Charset charset) throws IOException {
        return sendQuery(query, charset, -1);
    }

    @RetryFor(IOException.class)
    public String sendQuery(final String query, final Charset charset, final int timeout) throws IOException {
        final Socket socket = new Socket(host, port);
        if (timeout > 0) {
            socket.setSoTimeout(timeout);
        }

        PrintWriter serverWriter = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), charset));
        StringWriter responseWriter = new StringWriter();

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
