package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class NrtmQueryHandlerFactory {

    private final SerialDao serialDao;
    private final Dummifier dummifier;
    private final TaskScheduler clientSynchronisationScheduler;
    private final NrtmLog nrtmLog;
    private final String version;
    private final String source;
    private final String nonAuthSource;
    private final long updateInterval;
    private final boolean keepaliveEndOfStream;

    @Autowired
    public NrtmQueryHandlerFactory(
            @Qualifier("jdbcSlaveSerialDao") final SerialDao serialDao,
            final NrtmLog nrtmLog,
            @Qualifier("dummifierNrtm") final Dummifier dummifier,
            @Qualifier("clientSynchronisationScheduler") final TaskScheduler clientSynchronisationScheduler,
            @Value("${application.version}") final String version,
            @Value("${whois.source}") final String source,
            @Value("${whois.nonauth.source}") final String nonAuthSource,
            @Value("${nrtm.update.interval:60}") final long updateInterval,
            @Value("${nrtm.keepalive.end.of.stream:false}") final boolean keepaliveEndOfStream) {
        this.serialDao = serialDao;
        this.dummifier = dummifier;
        this.clientSynchronisationScheduler = clientSynchronisationScheduler;
        this.nrtmLog = nrtmLog;
        this.version = version;
        this.source = source;
        this.nonAuthSource = nonAuthSource;
        this.updateInterval = updateInterval;
        this.keepaliveEndOfStream = keepaliveEndOfStream;
    }

    public NrtmQueryHandler getInstance() {
        return new NrtmQueryHandler(
            serialDao,
            dummifier,
            clientSynchronisationScheduler,
            nrtmLog,
            version,
            source,
            nonAuthSource,
            updateInterval,
            keepaliveEndOfStream);
    }



}
