package net.ripe.db.whois.api.nrtm4;

import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WhoisNrtmServletDeployer implements ServletDeployer {

    private final NrtmClientService nrtmClientService;
    private final NrtmExceptionMapper nrtmExceptionMapper;

    @Autowired
    public WhoisNrtmServletDeployer(final NrtmClientService nrtmClientService, final NrtmExceptionMapper nrtmExceptionMapper) {
        this.nrtmClientService = nrtmClientService;
        this.nrtmExceptionMapper = nrtmExceptionMapper;
    }

    @Override
    public void deploy(final WebAppContext context) {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(nrtmClientService);
        resourceConfig.register(nrtmExceptionMapper);
        resourceConfig.register(new NrtmCacheControl());

        context.addServlet(new ServletHolder("Whois Nrtm version 4 REST API", new ServletContainer(resourceConfig)), "/nrtmv4/*");
    }
}
