package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

@Component
public class LegacyNrtmServerPipelineFactory extends BaseNrtmServerPipelineFactory {

    @Autowired
    public LegacyNrtmServerPipelineFactory(final NrtmExceptionHandler exceptionHandler, final AccessControlHandler aclHandler,
                                           final SerialDao serialDao, final NrtmLog nrtmLog, @Qualifier("dummifierLegacy") final Dummifier dummifier,
                                           @Qualifier("clientSynchronisationScheduler") final TaskScheduler clientSynchronisationScheduler,
                                           @Value("${application.version}") final String version,
                                           @Value("${whois.source}") final String source,
                                           @Value("${nrtm.update.interval:60}") final long updateInterval) {

        super(exceptionHandler, aclHandler, serialDao, nrtmLog, dummifier, clientSynchronisationScheduler, version, source, updateInterval);
    }
}
