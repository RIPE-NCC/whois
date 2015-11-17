package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.update.mail.MailSenderStub;

public class Context {
    private final int restPort;
    private final int syncUpdatePort;
    private final WhoisObjectMapper whoisObjectMapper;
    private final boolean debug = false;
    private final MailUpdatesTestSupport mailUpdatesTestSupport;
    private final MailSenderStub mailSenderStub;
    private final NrtmServer nrtmServer;

    public Context(final int restPort, final int syncUpdatePort, final WhoisObjectMapper whoisObjectMapper,
                   final MailUpdatesTestSupport mailUpdatesTestSupport, final MailSenderStub mailSenderStub,
                   final NrtmServer nrtmServer) {
        this.restPort = restPort;
        this.syncUpdatePort = syncUpdatePort;
        this.whoisObjectMapper = whoisObjectMapper;
        this.mailUpdatesTestSupport = mailUpdatesTestSupport;
        this.mailSenderStub = mailSenderStub;
        this.nrtmServer = nrtmServer;
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

    public NrtmServer getNrtmServer() {
        return nrtmServer;
    }

    public boolean isDebug() {
        return debug;
    }

}
