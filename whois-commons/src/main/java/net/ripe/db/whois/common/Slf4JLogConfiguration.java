package net.ripe.db.whois.common;


import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public final class Slf4JLogConfiguration {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Slf4JLogConfiguration.class);

    private Slf4JLogConfiguration() {
        // do not instantiate
    }

    public static void init() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LogManager.getLogManager().reset();
        Logger.getLogger("global").setLevel(Level.WARNING);
        InternalLoggerFactory.setDefaultFactory(Slf4JLoggerFactory.INSTANCE);
        try {
            System.setErr(new PrintStream(new Console()));
            System.setOut(new PrintStream(new Console()));
        } catch (Exception e) {
            LOGGER.error("Couldn't redirect system output", e);
        }
    }

    private static class Console extends OutputStream {

        private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Console.class);

        private final StringBuilder builder = new StringBuilder();

        @Override
        public void write(int b) throws IOException {
            final char c = (char)b;
            if (c == '\n' || c == '\r') {
                LOGGER.info(builder.toString());
                builder.setLength(0);
            } else {
                builder.append(c);
            }
        }
    }


}
