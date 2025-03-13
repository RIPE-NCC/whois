package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmtpServer implements ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpServer.class);

    private final boolean enabled;
    private final int port;

    @Autowired
    public SmtpServer(
        @Value("${smtp.enabled:true}") final boolean enabled,
        @Value("${smtp.port:0}") final int port) {
        this.enabled = enabled;
        this.port = port;
    }

    @Override
    public void start() {
        if (!enabled) {
            LOGGER.warn("STMP server not enabled");
            return;
        }
    }

    @Override
    public void stop(final boolean force) {

    }
}
