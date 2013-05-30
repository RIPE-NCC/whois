package net.ripe.db.whois.nrtm.client;


import net.ripe.db.whois.common.aspects.RetryFor;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.dao.jdbc.JdbcRpslObjectOperations;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.Source;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.nrtm.dao.NrtmClientDao;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
class NrtmClientFactory {
    private static final Pattern OPERATION_AND_SERIAL_PATTERN = Pattern.compile("^(ADD|DEL)[ ](\\d+)$");

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmClientFactory.class);

    private final SourceContext sourceContext;
    private final SerialDao serialDao;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final NrtmClientDao nrtmClientDao;

    @Autowired
    public NrtmClientFactory(final SourceContext sourceContext,
                             final SerialDao serialDao,
                             final RpslObjectUpdateDao rpslObjectUpdateDao,
                             final NrtmClientDao nrtmClientDao) {
        this.sourceContext = sourceContext;
        this.serialDao = serialDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.nrtmClientDao = nrtmClientDao;
    }

    public NrtmClient createNrtmClient(final CIString source, final String host, final int port) {
        return new NrtmClient(source, host, port);
    }

    public class NrtmClient implements Runnable {
        private final CIString source;
        private final String host;
        private final int port;

        public NrtmClient(final CIString source, final String host, final int port) {
            this.source = source;
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            try {
                sourceContext.setCurrent(Source.master(source));

                while (true) {
                    Socket socket = null;
                    try {
                        socket = connect();
                        final InputStreamReader reader = new InputStreamReader(socket.getInputStream());
                        final OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
                        readHeader(reader);
                        writeMirrorCommand(writer);
                        readMirrorResult(reader);
                        readUpdates(reader);
                    } catch (IllegalStateException e) {
                        LOGGER.error(e.getMessage());
                        break;
                    } catch (SocketException ignored) {
                        // try to reconnect
                    } catch (IOException e) {
                        LOGGER.info("Caught exception while connected, ignoring", e);
                    } catch (RuntimeException e) {
                        LOGGER.info("Caught exception while connected, ignoring", e);
                    } finally {
                        IOUtils.closeQuietly(socket);
                    }
                }
            } finally {
                sourceContext.removeCurrentSource();
            }
        }

        @RetryFor(value = IOException.class, attempts = 100, intervalMs = 10 * 1000)
        private Socket connect() throws IOException {
            try {
                final Socket socket = new Socket(host, port);
                LOGGER.info("Connected to {}:{}", host, port);
                return socket;
            } catch (UnknownHostException e) {
                throw new IllegalStateException(e);
            }
        }

        private void readHeader(final InputStreamReader reader) throws IOException {
            // TODO [AK] Read comments until empty line occurs
            readLine(reader, "%");
            readLine(reader, "%");
            readEmptyLine(reader);
        }

        private String readLine(final InputStreamReader reader, final String expected) throws IOException {
            final String line = readLine(reader);

            if (!line.contains(expected)) {
                throw new IllegalStateException("Expected to read: \"" + expected + "\", but actually read: \"" + line + "\"");
            }
            return line;
        }

        private String readEmptyLine(final InputStreamReader reader) throws IOException {
            return readLine(reader, "");
        }

        private String readLine(final InputStreamReader reader) throws IOException {
            final StringBuilder builder = new StringBuilder();

            while (true) {
                while (!reader.ready()) {
                    try {
                        Thread.sleep(100);  // TODO: make sleep configurable
                    } catch (InterruptedException e) {
                        throw new IllegalStateException("Thread was interrupted");
                    }
                }

                final int c = reader.read();

                switch (c) {
                    case -1:
                        throw new SocketException("Unexpected end of stream from NRTM server connection.");
                    case '\n':
                        return builder.toString();
                    default:
                        builder.append((char) c);
                }
            }
        }

        private void writeMirrorCommand(final OutputStreamWriter writer) throws IOException {
            writeLine(writer, String.format("-g %s:3:%d-LAST -k",
                    sourceContext.getCurrentSource().getName(),
                    serialDao.getSerials().getEnd()));
        }

        private void readMirrorResult(final InputStreamReader reader) throws IOException {
            final String result = readLine(reader, "%START");
            readEmptyLine(reader);
            LOGGER.info(result);
        }

        private void writeLine(final OutputStreamWriter writer, final String line) throws IOException {
            writer.write(line);
            writer.write('\n');
            writer.flush();
        }

        private void readUpdates(final InputStreamReader reader) throws IOException {
            while (true) {
                final OperationSerial operationSerial = readOperationAndSerial(reader);
                final RpslObject object = readObject(reader);
                update(operationSerial.getOperation(), operationSerial.getSerial(), object);
            }
        }

        public void update(final Operation operation, final int serialId, final RpslObject rpslObject) {
            try {
                switch (operation) {
                    case UPDATE:
                        try {
                            final RpslObjectUpdateInfo updateInfo = rpslObjectUpdateDao.lookupObject(rpslObject.getType(), rpslObject.getKey().toString());
                            if (!nrtmClientDao.objectExistsWithSerial(serialId, updateInfo.getObjectId())) {
                                LOGGER.info("UPDATE {}", serialId);
                                nrtmClientDao.updateObject(rpslObject, updateInfo, serialId);
                            }
                        } catch (EmptyResultDataAccessException e) {
                            LOGGER.info("ADD {}", serialId);
                            nrtmClientDao.createObject(rpslObject, serialId);
                        }
                        break;

                    case DELETE:
                        try {
                            final RpslObjectUpdateInfo updateInfo = rpslObjectUpdateDao.lookupObject(rpslObject.getType(), rpslObject.getKey().toString());
                            if (!nrtmClientDao.objectExistsWithSerial(serialId, updateInfo.getObjectId())) {
                                LOGGER.info("DELETE {}", serialId);
                                nrtmClientDao.deleteObject(updateInfo, serialId);
                            }
                        } catch (EmptyResultDataAccessException e) {
                            throw new IllegalStateException("DELETE serial:" + serialId + " but object:" + rpslObject.getKey().toString() + " doesn't exist");
                        }
                        break;
                }
            } catch (DataAccessException e) {
                throw new IllegalStateException("Unexpected error on " + operation + " " + serialId, e);
            }
        }

        private OperationSerial readOperationAndSerial(final InputStreamReader reader) throws IOException {
            final String line = readLine(reader, " ");

            final Matcher matcher = OPERATION_AND_SERIAL_PATTERN.matcher(line);
            if (!matcher.find()) {
                throw new IllegalStateException("Unexpected response from NRTM server: \"" + line + "\"");
            }

            final Operation operation = Operation.getByName(matcher.group(1));
            final String serial = matcher.group(2);
            readEmptyLine(reader);
            return new OperationSerial(operation, serial);
        }

        private RpslObject readObject(final InputStreamReader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            String line;
            while (!(line = readLine(reader)).isEmpty()) {
                builder.append(line);
                builder.append('\n');
            }
            return RpslObject.parse(builder.toString());
        }

        private class OperationSerial {
            private final Operation operation;
            private final int serial;

            private OperationSerial(final Operation operation, final String serial) {
                this.operation = operation;
                this.serial = Integer.parseInt(serial);
            }

            private Operation getOperation() {
                return operation;
            }

            private int getSerial() {
                return serial;
            }
        }
    }
}
