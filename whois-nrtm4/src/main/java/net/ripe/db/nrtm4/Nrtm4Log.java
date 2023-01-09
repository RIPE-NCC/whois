package net.ripe.db.nrtm4;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class Nrtm4Log {

    private final Logger logger;

    public Nrtm4Log() {
        this(LoggerFactory.getLogger(Nrtm4Log.class));
    }

    public Nrtm4Log(Logger logger) {
        this.logger = logger;
    }

    public void log(final InetAddress remoteAddress, final String queryString) {
        logger.info(
                "{} -- {}",
                remoteAddress.getHostAddress(),
                queryString
        );
    }
}
