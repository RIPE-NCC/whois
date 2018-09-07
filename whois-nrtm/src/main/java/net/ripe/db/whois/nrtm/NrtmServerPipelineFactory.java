package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

@Component
public class NrtmServerPipelineFactory extends BaseNrtmServerPipelineFactory {

    @Autowired
    public NrtmServerPipelineFactory(final NrtmChannelsRegistry nrtmChannelsRegistry,
                                     final NrtmExceptionHandler exceptionHandler,
                                     final AccessControlHandler aclHandler,
                                     final MaintenanceHandler maintenanceHandler,
                                     final NrtmQueryHandlerFactory nrtmQueryHandlerFactory) {
        super(nrtmChannelsRegistry, exceptionHandler, aclHandler, maintenanceHandler, nrtmQueryHandlerFactory);
    }

    @PreDestroy
    private void destroyExecutionHandler() {
        executionHandler.releaseExternalResources();
    }
}
