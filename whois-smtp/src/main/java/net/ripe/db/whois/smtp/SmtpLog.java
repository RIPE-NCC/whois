package net.ripe.db.whois.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Component
public class SmtpLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpLog.class);

    public void log(final InetAddress remoteAddress, final String domainName) {
        LOGGER.info(
                "{} {}",
                remoteAddress.getHostAddress(),
                domainName != null ? domainName : "NONE"
        );
    }


}
