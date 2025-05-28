package net.ripe.db.whois.changedphase3.util;

import net.ripe.db.whois.api.MailUpdatesTestSupport;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.dao.jdbc.DatabaseHelper;
import net.ripe.db.whois.nrtm.NrtmServer;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.scheduler.task.export.DatabaseTextExport;
import net.ripe.db.whois.update.mail.MailSenderStub;

public class Context {
    private final int restPort;
    private final int syncUpdatePort;
    private final WhoisObjectMapper whoisObjectMapper;
    private final boolean debug = false;
    private final MailUpdatesTestSupport mailUpdatesTestSupport;
    private final MailSenderStub mailSenderStub;
    private final NrtmServer nrtmServer;
    private final QueryServer queryServer;
    private final DatabaseHelper databaseHelper;
    private final DatabaseTextExport databaseTextExport;

    public Context(final int restPort, final int syncUpdatePort, final WhoisObjectMapper whoisObjectMapper,
                   final MailUpdatesTestSupport mailUpdatesTestSupport, final MailSenderStub mailSenderStub,
                   final QueryServer queryServer, final NrtmServer nrtmServer, final DatabaseHelper databaseHelper, final DatabaseTextExport databaseTextExport) {
        this.restPort = restPort;
        this.syncUpdatePort = syncUpdatePort;
        this.whoisObjectMapper = whoisObjectMapper;
        this.mailUpdatesTestSupport = mailUpdatesTestSupport;
        this.mailSenderStub = mailSenderStub;
        this.nrtmServer = nrtmServer;
        this.queryServer = queryServer;
        this.databaseHelper = databaseHelper;
        this.databaseTextExport = databaseTextExport;
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

    public QueryServer getQueryServer() {
        return queryServer;
    }

    public boolean isDebug() {
        return debug;
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public DatabaseTextExport getDatabaseTextExport() {
        return databaseTextExport;
    }
}
