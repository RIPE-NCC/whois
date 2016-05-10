package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.dao.SerialDao;
import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import net.ripe.db.whois.common.rpsl.Dummifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class NrtmServerPipelineFactory extends BaseNrtmServerPipelineFactory {

    @Autowired
    public NrtmServerPipelineFactory(final NrtmChannelsRegistry nrtmChannelsRegistry,
                                     final NrtmExceptionHandler exceptionHandler,
                                     final AccessControlHandler aclHandler,
                                     @Qualifier("jdbcSlaveSerialDao") final SerialDao serialDao,
                                     final MaintenanceHandler maintenanceHandler,
                                     final NrtmLog nrtmLog,
                                     @Qualifier("dummifierNrtm") final Dummifier dummifier,
                                     @Qualifier("clientSynchronisationScheduler") final TaskScheduler clientSynchronisationScheduler,
                                     @Value("${application.version}") final String version,
                                     @Value("${whois.source}") final String source,
                                     @Value("${nrtm.update.interval:60}") final long updateInterval) {

        super(nrtmChannelsRegistry, exceptionHandler, aclHandler, serialDao, nrtmLog, dummifier, clientSynchronisationScheduler, maintenanceHandler, version, source, updateInterval);
    }

    @PreDestroy
    private void destroyExecutionHandler() {
        executionHandler.releaseExternalResources();
    }
}
