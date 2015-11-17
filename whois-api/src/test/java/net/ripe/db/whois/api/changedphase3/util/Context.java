package net.ripe.db.whois.api.changedphase3.util;

import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.update.mail.MailSenderStub;
import org.springframework.beans.factory.annotation.Autowired;

public class Context {
    private int restPort;
    private int syncUpdatePort;
    private WhoisObjectMapper whoisObjectMapper;
    private boolean debug = false;
    private MailUpdatesTestSupport mailUpdatesTestSupport;
    private MailSenderStub mailSenderStub;

    public Context(int restPort, int syncUpdatePort, WhoisObjectMapper whoisObjectMapper,
                   MailUpdatesTestSupport mailUpdatesTestSupport, MailSenderStub mailSenderStub) {
        this.restPort = restPort;
        this.syncUpdatePort = syncUpdatePort;
        this.whoisObjectMapper = whoisObjectMapper;
        this.mailUpdatesTestSupport = mailUpdatesTestSupport;
        this.mailSenderStub = mailSenderStub;
    }

    public int getRestPort() {
        return restPort;
    }

    public int getSyncUpdatePort() {
        return syncUpdatePort;
    }

    public WhoisObjectMapper getWhoisObjectMapper() {
        return whoisObjectMapper;
    }

    public MailUpdatesTestSupport getMailUpdatesTestSupport() {
        return mailUpdatesTestSupport;
    }

    public MailSenderStub getMailSenderStub() {
        return mailSenderStub;
    }

    public boolean isDebug() {
        return debug;
    }

}
