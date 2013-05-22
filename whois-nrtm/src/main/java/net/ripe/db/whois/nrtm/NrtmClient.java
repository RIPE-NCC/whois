package net.ripe.db.whois.nrtm;


import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NrtmClient {
    private static final Pattern OPERATION_AND_SERIAL_PATTERN = Pattern.compile("^(ADD|DEL)[ ](\\d+)$");

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmClient.class);

    private final SourceContext sourceContext;
    private final SerialDao serialDao;
    private String nrtmHost;
    private int nrtmPort;

    private NrtmClientThread clientThread = null;

    @Autowired
    public NrtmClient(final SourceContext sourceContext, final SerialDao serialDao, @Value("${nrtm.client.host:}") final String nrtmHost, @Value("${nrtm.client.port:-1}") final int nrtmPort) {
        this.sourceContext = sourceContext;
        this.serialDao = serialDao;
        this.nrtmHost = nrtmHost;
        this.nrtmPort = nrtmPort;
    }

    @PostConstruct
    public void init() {
        // TODO: check if host & port have been initialised
        // start(nrtmHost, nrtmPort);
    }

    public void start(final String nrtmHost, final int nrtmPort) {
        Validate.notNull(nrtmHost);
        Validate.isTrue(nrtmPort > 0);
        Validate.isTrue(clientThread == null);

        clientThread = new NrtmClientThread(nrtmHost, nrtmPort);
        new Thread(clientThread).start();
    }

    private final class NrtmClientThread implements Runnable {
        private final Socket socket;
        private boolean running;

        public NrtmClientThread(final String host, final int port) {
            try {
                socket = new Socket(host, port);
                running = true;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                readHeader(reader);
                writeMirrorCommand(writer);
                readMirrorResult(reader);
                readUpdates(reader);
            }
            catch (SocketException e) {
                if (running) {
                    // TODO: retry connection on network outage
                    LOGGER.error("Unexpected network error", e);
                }
            }
            catch (IllegalStateException e) {
                LOGGER.error("Unexpected response from NRTM server", e);
            }
            catch (Exception e) {
                LOGGER.error("Error in NRTM server connection", e);
            } finally {
                stop();
            }
        }

        public void stop() {
            if (running) {
                if (socket != null) {
                    if (!socket.isClosed()) {
                        try {
                            socket.close();
                        } catch (IOException ignored) {
                        }
                    }
                }
                running = false;
            }
        }

        private void readHeader(final BufferedReader reader) throws IOException {
            readLine(reader, "% The RIPE Database is subject to Terms and Conditions.");
            readLine(reader, "% See http://www.ripe.net/db/support/db-terms-conditions.pdf");
            readEmptyLine(reader);
        }

        private String readLine(final BufferedReader reader, final String expected) throws IOException {
            final String line = reader.readLine();
            if (line == null) {
                throw new IllegalStateException("Unexpected end of stream from NRTM server connection.");
            }
            if (!line.contains(expected)) {
                throw new IllegalStateException("Expected to read: \"" + expected + "\", but actually read: \"" + line + "\"");
            }
            return line;
        }

        private String readEmptyLine(final BufferedReader reader) throws IOException {
            return readLine(reader, "");
        }

        private void writeMirrorCommand(final BufferedWriter writer) throws IOException {
            writeLine(writer, String.format("-g %s:3:%d-LAST -k",
                    sourceContext.getCurrentSource().getName(),
                    serialDao.getSerials().getEnd()));
        }

        private void readMirrorResult(final BufferedReader reader) throws IOException {
            final String result = readLine(reader, "%START");
            readEmptyLine(reader);
            LOGGER.info(result);
        }

        private void writeLine(final BufferedWriter writer, final String line) throws IOException {
            writer.write(line);
            writer.newLine();
            writer.flush();
        }

        private void readUpdates(final BufferedReader reader) throws IOException {
            for (;;) {
                // TODO: first object read from NTRM will be the latest object already in the database
                readOperationAndSerial(reader);
                final RpslObject rpslObject = readObject(reader);
                LOGGER.info("Object: {}", rpslObject.getKey());
            }
        }

        private void readOperationAndSerial(final BufferedReader reader) throws IOException {
            final String line = readLine(reader, " ");
            final Matcher matcher = OPERATION_AND_SERIAL_PATTERN.matcher(line);
            if (!matcher.find()) {
                throw new IllegalStateException("Unexpected response from NRTM server: \"" + line + "\"");
            }
            final Operation operation = Operation.getByName(matcher.group(1));
            final String serial = matcher.group(2);
            LOGGER.info("Operation:{} Serial:{}", operation, serial);
            readEmptyLine(reader);
        }

        private RpslObject readObject(final BufferedReader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            String line;
            while (((line = reader.readLine()) != null) && (!line.isEmpty())) {     // TODO: readLine() is a blocking operation
                builder.append(line);
                builder.append('\n');
            }
            return RpslObject.parse(builder.toString());
        }
    }

    @PreDestroy
    public void cleanup() {
        if (clientThread != null) {
            clientThread.stop();
        }
    }
}
