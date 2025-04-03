package net.ripe.db.whois.smtp;


import net.ripe.db.whois.common.aspects.RetryFor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.SocketChannel;

class SmtpClient implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpClient.class);

    private final SocketChannel socketChannel;
    private final SocketChannelFactory.Reader reader;
    private final SocketChannelFactory.Writer writer;

    public SmtpClient(final String host, final int port) {
        this.socketChannel = connect(host, port);
        this.reader = SocketChannelFactory.createReader(socketChannel);
        this.writer = SocketChannelFactory.createWriter(socketChannel);
    }

    @RetryFor(value = IOException.class, attempts = 100, intervalMs = 10 * 1000)
    private SocketChannel connect(final String host, final int port) {
        try {
            return SocketChannelFactory.createSocketChannel(host, port);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public String readLine() throws IOException {
        return reader.readLine();
    }

    public void writeLine(final String line) throws IOException{
        writer.writeLine(line);
    }

    public void writeLines(final String lines) throws IOException {
        writer.writeLines(lines);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(socketChannel);
    }

}
