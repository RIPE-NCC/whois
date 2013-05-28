package net.ripe.db.whois.nrtm;


import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.nrtm.dao.NrtmClientDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NrtmClient implements ApplicationService {

    private static final Pattern OPERATION_AND_SERIAL_PATTERN = Pattern.compile("^(ADD|DEL)[ ](\\d+)$");

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmClient.class);

    private static final int MAX_RETRIES = 100;

    private final SourceContext sourceContext;
    private final SerialDao serialDao;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final NrtmClientDao nrtmClientDao;
    private final String nrtmHost;
    private final int nrtmPort;

    private NrtmClientThread clientThread = null;

    //TODO make sure this is not run at the same time as "normal" updates

    @Autowired
    public NrtmClient(final SourceContext sourceContext,
                      //@Qualifier("whoisMasterNrtmClientDataSource") final DataSource datasource,        // TODO: will transactional work with this datasource?
                      final SerialDao serialDao,
                      final RpslObjectUpdateDao rpslObjectUpdateDao,
                      final NrtmClientDao nrtmClientDao,
                      @Value("${nrtm.client.host:}") final String nrtmHost,
                      @Value("${nrtm.client.port:-1}") final int nrtmPort) {
        this.sourceContext = sourceContext;
        this.serialDao = serialDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.nrtmClientDao = nrtmClientDao;
        this.nrtmHost = nrtmHost;
        this.nrtmPort = nrtmPort;
    }

    @Override
    public void start() {
        start(nrtmHost, nrtmPort);
    }

    public void start(final String host, final int port) {
        if (StringUtils.isNotBlank(host) && port > 0) {
            LOGGER.info("Connecting NRTM client to {}:{}", host, port);
            clientThread = new NrtmClientThread(host, port);
            new Thread(clientThread).start();
        } else {
            LOGGER.info("Not starting NRTM client");
        }
    }

    @Override
    public void stop() {
        if (clientThread != null) {
            clientThread.stop();
        }
    }

    private final class NrtmClientThread implements Runnable {
        private Socket socket;
        private boolean running;
        private String host;
        private int port;
        private Random random = new Random();

        public NrtmClientThread(final String host, final int port) {
            this.host = host;
            this.port = port;
            init();
        }

        private void init() {
            if (socket != null && socket.isConnected()) {
                return;
            }

            for (int attempt = 0; ; attempt++) {
                try {
                    LOGGER.info("Connecting to {}:{}", host, port);
                    socket = new Socket(host, port);
                    socket.setReuseAddress(true);
                    running = true;
                    break;
                }
                catch (Exception e) {
                    if (attempt >= MAX_RETRIES) {
                        throw new IllegalStateException(e);
                    }

                    try {
                        Thread.sleep(100 + random.nextInt(500));
                    } catch (InterruptedException ignored) {
                    }
                }
            }
        }

        @Override
        public void run() {
            while (running) {
                try {
                    init();

                    final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    readHeader(reader);
                    writeMirrorCommand(writer);
                    readMirrorResult(reader);
                    readUpdates(reader);

                } catch (IllegalStateException e) {
                    LOGGER.info("Encountered Illegal state, stopping.", e);
                    running = false;
                } catch (SocketException e) {
                    // expected - disconnected from server - will retry
                } catch (Exception e) {
                    LOGGER.info("Caught exception while connected", e);
                } finally {
                    stop();
                }
            }
        }

        public void stop() {
            if (socket != null) {
                if (!socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException ignored) {
                    } finally {
                        socket = null;
                    }
                }
            }
        }

        private void readHeader(final BufferedReader reader) throws IOException {
            readLine(reader, "%");
            readLine(reader, "%");
            readEmptyLine(reader);
        }

        private String readLine(final BufferedReader reader, final String expected) throws IOException {
            final String line = reader.readLine();
            if (line == null) {
                throw new SocketException("Unexpected end of stream from NRTM server connection.");
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
            try {
                for (;;) {
                    final OperationSerial operationSerial = readOperationAndSerial(reader);
                    final RpslObject object = readObject(reader);
                    update(operationSerial.getOperation(), operationSerial.getSerial(), object);
                }
            } finally {
                stop();
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

        private OperationSerial readOperationAndSerial(final BufferedReader reader) throws IOException {
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

        private RpslObject readObject(final BufferedReader reader) throws IOException {
            StringBuilder builder = new StringBuilder();
            String line;

            while (((line = reader.readLine()) != null) && (!line.isEmpty())) {
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
