package net.ripe.db.whois.nrtm.integration;

import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class DummyNrtmServer {
    private static final String HEADER =
            "% The RIPE Database is subject to Terms and Conditions.\n" +
            "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
            "\n";

    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private boolean running = true;

    private final Map<String, String> responseMap = Maps.newHashMap();

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    openServerSocket();
                    for (;running;) {
                        try {
                            socket = openSocket();

                            final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                            writer.write(HEADER);
                            writer.flush();

                            for (;running;) {
                                final String request = reader.readLine();
                                final String response = responseMap.get(request);

                                if (response != null) {
                                    writer.write(response);
                                    writer.flush();
                                }

                                if (!request.contains("-k")) {
                                    break;
                                }
                            }
                        } catch (Exception ignored) {
                            break;
                        } finally {
                            closeSocket();
                        }
                    }
                } finally {
                    closeServerSocket();
                }
            }
        }).start();
    }

    public void stop() {
        running = false;
        closeSocket();
    }

    private Socket openSocket() throws IOException {
        final Socket socket = serverSocket.accept();
        socket.setReuseAddress(true);
        return socket;
    }

    private void closeSocket() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            } finally {
                socket = null;
            }
        }
    }

    private void openServerSocket() {
        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void closeServerSocket() {
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void when(final String request, final String response) {
        responseMap.put(request, response);
    }
}
