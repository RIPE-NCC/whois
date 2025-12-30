package net.ripe.db.whois.api.rest;

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
public class WhoisServletDeployer implements ServletDeployer {

    private final WhoisResourceConfig resourceConfig;
    private final MaintenanceModeFilter maintenanceModeFilter;
    private final HttpsBasicAuthFiler httpsBasicAuthFiler;
    private final HttpsAuthHeaderFiler httpsAuthHeaderFiler;
    private final HttpsAPIKeyAuthFilter httpsAPIKeyAuthFilter;
    private final SyncUpdatesHttpSchemeFilter syncUpdatesHttpSchemeFilter;
    private final WhoisCrossOriginFilter whoisCrossOriginFilter;

    @Autowired
    public WhoisServletDeployer(
                    final WhoisResourceConfig resourceConfig,
                    final MaintenanceModeFilter maintenanceModeFilter,
                    final WhoisCrossOriginFilter whoisCrossOriginFilter,
                    final HttpsBasicAuthFiler httpsBasicAuthFiler,
                    final HttpsAuthHeaderFiler httpsAuthHeaderFiler,
                    final HttpsAPIKeyAuthFilter httpsAPIKeyAuthFilter,
                    final SyncUpdatesHttpSchemeFilter syncUpdatesHttpSchemeFilter) {
        this.resourceConfig = resourceConfig;
        this.maintenanceModeFilter = maintenanceModeFilter;
        this.httpsBasicAuthFiler = httpsBasicAuthFiler;
        this.httpsAuthHeaderFiler = httpsAuthHeaderFiler;
        this.httpsAPIKeyAuthFilter = httpsAPIKeyAuthFilter;
        this.syncUpdatesHttpSchemeFilter = syncUpdatesHttpSchemeFilter;
        this.whoisCrossOriginFilter = whoisCrossOriginFilter;
    }

    @Override
    public void deploy(ServletContextHandler context) {
        context.addFilter(new FilterHolder(maintenanceModeFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(httpsAuthHeaderFiler), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(httpsAPIKeyAuthFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(httpsBasicAuthFiler), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(syncUpdatesHttpSchemeFilter), "/whois/syncupdates/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(whoisCrossOriginFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));

        context.addServlet(new ServletHolder("Whois REST API", new ServletContainer(resourceConfig)), "/whois/*");
    }
}
