package net.ripe.db.whois.common;


import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class Slf4JLogConfiguration {
    private Slf4JLogConfiguration() {
        // do not instantiate
    }

    public static void init() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().reset();
        final Logger globalLogger = Logger.getLogger("global");
        globalLogger.setLevel(Level.INFO);
        System.setErr(new PrintStream(new LogOutputStream(globalLogger)));
        System.setOut(new PrintStream(new LogOutputStream(globalLogger)));
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
    }

    private static class LogOutputStream extends OutputStream {

        private final Logger logger;
        private final StringBuilder builder;

        public LogOutputStream(final Logger logger) {
            this.logger = logger;
            this.builder = new StringBuilder();
        }

        @Override
        public void write(int b) throws IOException {
            if (b == '\n' || b == '\r') {
                logger.log(Level.INFO, builder.toString());
                builder.setLength(0);
            } else {
                builder.append((char)b);
            }
        }
    }


}
