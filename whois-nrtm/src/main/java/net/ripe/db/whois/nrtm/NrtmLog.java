package net.ripe.db.whois.nrtm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class NrtmLog {
    private final Logger logger;

    public NrtmLog() {
        this(LoggerFactory.getLogger(NrtmLog.class));
    }

    public NrtmLog(Logger logger) {
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

