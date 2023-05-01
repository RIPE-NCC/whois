package net.ripe.db.whois.api.nrtm4;

import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Component
public class WhoisNrtmServletDeployer implements ServletDeployer {

    private final NrtmClientService nrtmClientService;
    private final NrtmExceptionMapper nrtmExceptionMapper;

    private final NrtmHttpControl nrtmHttpControl;

    @Autowired
    public WhoisNrtmServletDeployer(final NrtmClientService nrtmClientService,
                                    final NrtmExceptionMapper nrtmExceptionMapper, final NrtmHttpControl nrtmHttpControl) {
        this.nrtmClientService = nrtmClientService;
        this.nrtmExceptionMapper = nrtmExceptionMapper;
        this.nrtmHttpControl = nrtmHttpControl;
    }

    @Override
    public void deploy(final WebAppContext context) {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(nrtmClientService);
        resourceConfig.register(nrtmExceptionMapper);
        resourceConfig.register(new NrtmCacheControl());

        context.addFilter(new FilterHolder(nrtmHttpControl), "/nrtmv4/*", EnumSet.allOf(DispatcherType.class));

        context.addServlet(new ServletHolder("Whois Nrtm version 4 REST API", new ServletContainer(resourceConfig)), "/nrtmv4/*");
    }
}
