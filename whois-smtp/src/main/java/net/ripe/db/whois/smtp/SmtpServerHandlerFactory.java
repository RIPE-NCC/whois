package net.ripe.db.whois.smtp;

import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import net.ripe.db.whois.common.ApplicationVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmtpServerHandlerFactory {

    private final MailMessageDao mailMessageDao;
    final ApplicationVersion applicationVersion;

    @Autowired
    public SmtpServerHandlerFactory(
            final MailMessageDao mailMessageDao,
            final ApplicationVersion applicationVersion) {
        this.mailMessageDao = mailMessageDao;
        this.applicationVersion = applicationVersion;
    }

    public SmtpServerHandler getInstance() {
        return new SmtpServerHandler(
            mailMessageDao,
            applicationVersion);
    }



}
