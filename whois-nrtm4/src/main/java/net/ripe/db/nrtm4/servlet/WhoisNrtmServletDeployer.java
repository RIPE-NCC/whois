package net.ripe.db.nrtm4.servlet;

import jakarta.servlet.DispatcherType;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class WhoisNrtmServletDeployer implements ServletDeployer {

    private final NrtmService nrtmClientService;
    private final NrtmExceptionMapper nrtmExceptionMapper;

    private final NrtmHttpSchemeFilter nrtmHttpSchemeFilter;

    @Autowired
    public WhoisNrtmServletDeployer(final NrtmService nrtmClientService,
                                    final NrtmExceptionMapper nrtmExceptionMapper, final NrtmHttpSchemeFilter nrtmHttpSchemeFilter) {
        this.nrtmClientService = nrtmClientService;
        this.nrtmExceptionMapper = nrtmExceptionMapper;
        this.nrtmHttpSchemeFilter = nrtmHttpSchemeFilter;
    }

    @Override
    public void deploy(final WebAppContext context) {
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(nrtmClientService);
        resourceConfig.register(nrtmExceptionMapper);
        resourceConfig.register(new NrtmCacheControl());

        context.addFilter(new FilterHolder(nrtmHttpSchemeFilter), "/nrtmv4/*", EnumSet.allOf(DispatcherType.class));

        context.addServlet(new ServletHolder("Whois Nrtm version 4 REST API", new ServletContainer(resourceConfig)), "/nrtmv4/*");
    }
}
