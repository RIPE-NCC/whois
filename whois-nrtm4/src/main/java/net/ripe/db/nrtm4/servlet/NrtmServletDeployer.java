package net.ripe.db.nrtm4.servlet;

import jakarta.servlet.DispatcherType;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.ee10.servlet.FilterHolder;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component
public class NrtmServletDeployer implements ServletDeployer {

    private final NrtmHttpSchemeFilter nrtmHttpSchemeFilter;
    private final NrtmResourceConfig resourceConfig;

    @Autowired
    public NrtmServletDeployer(
            final NrtmResourceConfig resourceConfig,
            final NrtmHttpSchemeFilter nrtmHttpSchemeFilter) {
        this.resourceConfig = resourceConfig;
        this.nrtmHttpSchemeFilter = nrtmHttpSchemeFilter;
    }

    @Override
    public void deploy(final ServletContextHandler context) {
        context.addFilter(new FilterHolder(nrtmHttpSchemeFilter), "/nrtmv4/*", EnumSet.allOf(DispatcherType.class));

        context.addServlet(new ServletHolder("Whois Nrtm version 4 REST API", new ServletContainer(resourceConfig)), "/nrtmv4/*");
    }
}
