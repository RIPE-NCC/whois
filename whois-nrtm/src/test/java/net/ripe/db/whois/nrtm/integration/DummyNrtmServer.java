package net.ripe.db.whois.nrtm.integration;

import com.google.common.collect.Lists;
import com.jayway.awaitility.Awaitility;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.is;

public class DummyNrtmServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DummyNrtmServer.class);

    private static final Pattern MIRROR_COMMAND_PATTERN = Pattern.compile("^-g TEST:3:(\\d+)-LAST -k$");

    private static final String HEADER =
            "% The RIPE Database is subject to Terms and Conditions.\n" +
            "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
            "\n";

    private final List<Update> updates = Collections.synchronizedList(Lists.<Update>newArrayList());
    private final int port;

    private Thread thread = null;
    private boolean running = false;

    public DummyNrtmServer(final int port) {
        this.port = port;
    }

    public void start() {
        if (running) {
            throw new IllegalStateException();
        }
        NrtmServerThread nrtmServerThread = new NrtmServerThread();
        thread = new Thread(nrtmServerThread);
        thread.start();
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException();
        }
        running = false;
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return thread.isAlive();
            }
        }, is(false));
    }

    public int getPort() {
        return port;
    }

    public void addObject(final int serial, final RpslObject rpslObject) {
        updates.add(new Update(Operation.UPDATE, serial, rpslObject));
    }

    public void deleteObject(final int serial, final RpslObject rpslObject) {
        updates.add(new Update(Operation.DELETE, serial, rpslObject));
    }

    private class NrtmServerThread implements Runnable {
        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = openServerSocket();
                running = true;
                while (running) {
                    Socket socket = null;
                    try {
                        socket = acceptSocket(serverSocket);

                        final InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                        final OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());

                        writer.write(HEADER);
                        writer.flush();

                        final String command = readLine(reader);

                        final Matcher matcher = MIRROR_COMMAND_PATTERN.matcher(command);
                        if (matcher.find()) {
                            final int startSerial = Integer.parseInt(matcher.group(1));

                            if (startSerial > updates.get(updates.size() - 1).getSerial()) {
                                writer.write(String.format("%%ERROR:401: invalid range: Not within %d-%d\n", updates.get(0).getSerial(), updates.get(updates.size() - 1).getSerial()));
                                writer.flush();
                                break;
                            }

                            writer.write(String.format("%%START Version: 3 RIPE %d-%d\n\n", startSerial, updates.get(updates.size() - 1).getSerial()));

                            int index = 0;
                            while (running) {
                                for (; index < updates.size(); index++) {
                                    if (updates.get(index).getSerial() >= startSerial) {
                                        writer.write(updates.get(index).toString());
                                        writer.flush();
                                    }
                                }
                                Thread.sleep(100);
                            }
                        } else {
                            writer.write("%ERROR:405: no flags passed\n");
                            writer.flush();
                        }
                    } catch (SocketException ignored) {
                    } catch (Exception e) {
                        LOGGER.error("Caught exception", e);
                    } finally {
                        closeSocket(socket);
                    }
                }
            } finally {
                closeServerSocket(serverSocket);
            }
        }

        private String readLine(final InputStreamReader reader) throws IOException {
            final StringBuilder builder = new StringBuilder();

            for (;;) {
                while (!reader.ready()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException("Thread was interrupted");
                    }
                }

                final int c = reader.read();

                switch (c) {
                    case -1: throw new SocketException("Unexpected end of stream from NRTM server connection.");
                    case '\n': return builder.toString();
                    default: builder.append((char)c);
                }
            }
        }


        private Socket acceptSocket(final ServerSocket serverSocket) throws IOException {
            serverSocket.bind(new InetSocketAddress(port));
            return serverSocket.accept();
        }

        private void closeSocket(final Socket socket) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }

        private ServerSocket openServerSocket() {
            try {
                final ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                return serverSocket;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private void closeServerSocket(final ServerSocket serverSocket) {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private class Update {
        private Operation operation;
        private int serial;
        private RpslObject rpslObject;

        public Update(final Operation operation, final int serial, final RpslObject rpslObject) {
            this.operation = operation;
            this.serial = serial;
            this.rpslObject = rpslObject;
        }

        public int getSerial() {
            return serial;
        }

        @Override
        public String toString() {
            return String.format("%s %d\n\n%s\n", operation.toString(), serial, rpslObject.toString());
        }
    }
}
