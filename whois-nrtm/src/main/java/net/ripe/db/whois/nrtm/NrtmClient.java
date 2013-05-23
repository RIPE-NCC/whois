package net.ripe.db.whois.nrtm;


import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcSerialDao;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import sun.net.ConnectionResetException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.*;
import java.net.BindException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NrtmClient {
    private static final Pattern OPERATION_AND_SERIAL_PATTERN = Pattern.compile("^(ADD|DEL)[ ](\\d+)$");

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmClient.class);
    private static final int MAX_RETRY = 3;
    private static final int RETRY_WAIT = 50000;

    private final SourceContext sourceContext;
    private final SerialDao serialDao;
    private final RpslObjectDao rpslObjectDao;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final String nrtmHost;
    private final int nrtmPort;

    private NrtmClientThread clientThread = null;

    @Autowired
    public NrtmClient(final SourceContext sourceContext,
                      @Qualifier("whoisMasterNrtmClientDataSource") final DataSource datasource,
                      final DateTimeProvider dateTimeProvider,
                      final RpslObjectDao objectDao,
                      final RpslObjectUpdateDao rpslObjectUpdateDao,
                      @Value("${nrtm.client.host:}") final String nrtmHost,
                      @Value("${nrtm.client.port:-1}") final int nrtmPort) {
        this.sourceContext = sourceContext;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.serialDao = new JdbcSerialDao(datasource, dateTimeProvider);
        this.rpslObjectDao = objectDao;
        this.nrtmHost = nrtmHost;
        this.nrtmPort = nrtmPort;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isNotBlank(nrtmHost) && nrtmPort > 0) {
            start(nrtmHost, nrtmPort);
        }
    }

    public void start(final String nrtmHost, final int nrtmPort) {
        Validate.notNull(nrtmHost);
        Validate.isTrue(nrtmPort > 0);
        Validate.isTrue(clientThread == null);

        clientThread = new NrtmClientThread(nrtmHost, nrtmPort);
        new Thread(clientThread).start();
    }

    private final class NrtmClientThread implements Runnable {
        private Socket socket;
        private boolean running;
        private int retried = 0;
        private String host;
        private int port;

        public NrtmClientThread(final String nrtmHost, final int nrtmPort) {
            this.host = nrtmHost;
            this.port = nrtmPort;
            init();
        }

        private void init() {
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
                while (retried <= MAX_RETRY) {
                    if (!running) {
                        init();
                    }

                    try {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                        readHeader(reader);
                        writeMirrorCommand(writer);
                        readMirrorResult(reader);
                        readUpdates(reader);
                        retried = MAX_RETRY;
                    } catch (ConnectException e) {
                        retried++;
                        Thread.sleep(RETRY_WAIT);
                    } catch (BindException e) {
                        retried++;
                        Thread.sleep(RETRY_WAIT);
                    } catch (ConnectionResetException e) {
                        retried++;
                        Thread.sleep(RETRY_WAIT);
                    } finally {
                        stop();
                    }
                }
            } catch (SocketException e) {
                LOGGER.error("Unexpected network error", e);
            } catch (IllegalStateException e) {
                LOGGER.error("Unexpected response from NRTM server", e);
            } catch (Exception e) {
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
            for (; ; ) {
                // TODO: first object read from NTRM will be the latest object already in the database
                readOperationAndSerial(reader);
                final RpslObject rpslObject = readObject(reader);
                LOGGER.info("Object: {}", rpslObject.getKey());
            }
        }

        private OperationSerial readOperationAndSerial(final BufferedReader reader) throws IOException {
            final String line = readLine(reader, " ");
            final Matcher matcher = OPERATION_AND_SERIAL_PATTERN.matcher(line);
            if (!matcher.find()) {
                throw new IllegalStateException("Unexpected response from NRTM server: \"" + line + "\"");
            }
            final Operation operation = Operation.getByName(matcher.group(1));
            final String serial = matcher.group(2);
            LOGGER.info("Operation:{} Serial:{}", operation, serial);
            readEmptyLine(reader);
            return new OperationSerial(operation, serial);
        }

        /*
        Reader.ready() returns true when data can be read without blocking. Period.
        InputStreams and Readers are blocking. Period.
        Everything here is working as designed.
        If you want more concurrency with these APIs you will have to use multiple threads.
        Or Socket.setSoTimeout() and its near relation in HttpURLConnection.
         */
        private RpslObject readObject(final BufferedReader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            String line;

            while (!reader.ready()) {
            } // TODO: due to readLine being blocking - let this try forever? sleep between tries?

            while (((line = reader.readLine()) != null) && (!line.isEmpty())) {
                builder.append(line);
                builder.append('\n');
            }
            return RpslObject.parse(builder.toString());
        }

        private class OperationSerial {
            private final Operation operation;
            private final String serial;

            private OperationSerial(final Operation operation, final String serial) {
                this.operation = operation;
                this.serial = serial;
            }

            private Operation getOperation() {
                return operation;
            }

            private String getSerial() {
                return serial;
            }
        }
    }

    @PreDestroy
    public void cleanup() {
        if (clientThread != null) {
            clientThread.stop();
        }
    }
}
