package net.ripe.db.whois.nrtm;

import net.ripe.db.whois.common.pipeline.MaintenanceHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

@Component
public class NrtmServerPipelineFactory extends BaseNrtmServerPipelineFactory {

    @Autowired
    public NrtmServerPipelineFactory(final NrtmChannelsRegistry nrtmChannelsRegistry,
                                     final NrtmExceptionHandler exceptionHandler,
                                     final MaintenanceHandler maintenanceHandler,
                                     final NrtmQueryHandlerFactory nrtmQueryHandlerFactory,
                                     final NrtmAclLimitHandler nrtmAclLimitHandler,
                                     final NrtmConnectionPerIpLimitHandler nrtmConnectionPerIpLimitHandler) {
        super(nrtmChannelsRegistry, exceptionHandler, maintenanceHandler, nrtmQueryHandlerFactory, nrtmAclLimitHandler,nrtmConnectionPerIpLimitHandler);
    }

    @PreDestroy
    private void destroyExecutionHandler() {
        executionHandler.releaseExternalResources();
    }
}
