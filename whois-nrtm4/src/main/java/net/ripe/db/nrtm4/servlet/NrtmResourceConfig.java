package net.ripe.db.nrtm4.servlet;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NrtmResourceConfig extends ResourceConfig {

    @Autowired
    public NrtmResourceConfig(
            final NrtmService nrtmClientService,
            final NrtmExceptionMapper nrtmExceptionMapper) {
        register(nrtmClientService);
        register(nrtmExceptionMapper);
        register(new NrtmCacheControl());
    }


}
