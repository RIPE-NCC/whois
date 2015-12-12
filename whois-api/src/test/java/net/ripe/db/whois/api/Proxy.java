package net.ripe.db.whois.api;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class Proxy extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxy.class);

    private final ServerSocket serverSocket;
    private final String host;
    private final int port;

    private final AtomicBoolean RUNNING = new AtomicBoolean(true);

    public Proxy(final String host, final int port) {
        this.host = host;
        this.port = port;
        try {
            this.serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void run() {
        for (;;) {
            try {
                final Socket srcSocket = serverSocket.accept();
                if (!RUNNING.get()) {
                    srcSocket.close();
                    continue;
                }
                final Socket destSocket = new Socket(host, port);
                new ProxySocket(srcSocket.getInputStream(), destSocket.getOutputStream()).start();
                new ProxySocket(destSocket.getInputStream(), srcSocket.getOutputStream()).start();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
                throw new IllegalStateException(e);
            }
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void setRunning(boolean running) {
        RUNNING.set(running);
    }

    protected class ProxySocket extends Thread {

        private final InputStream in;
        private final OutputStream out;

        public ProxySocket(final InputStream in, final OutputStream out) {
            this.in = in;
            this.out = out;
        }

        @Override
        public void run() {
            int read;
            try {
                while (RUNNING.get()) {
                    while ((in.available() > 0) && (read = in.read()) != -1) {
                        out.write(read);
                        out.flush();
                    }
                    Thread.yield();
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

}
