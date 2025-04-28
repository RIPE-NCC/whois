package net.ripe.db.whois.smtp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class SmtpRawLog {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmtpRawLog.class);

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy");

    public void log(final byte[] message) {
        LOGGER.info("From MAILER-DAEMON {}\n{}\n",
            DATE_TIME_FORMATTER.format(LocalDateTime.now()),
            new String(message));
    }

}
