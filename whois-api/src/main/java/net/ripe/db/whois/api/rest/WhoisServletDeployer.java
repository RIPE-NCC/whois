package net.ripe.db.whois.api.rest;

import jakarta.servlet.DispatcherType;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import net.ripe.db.whois.api.httpserver.SyncUpdateCORSFilter;
import net.ripe.db.whois.api.security.SecurityConfig;
import org.eclipse.jetty.ee11.servlet.FilterHolder;
import org.eclipse.jetty.ee11.servlet.ServletContextHandler;
import org.eclipse.jetty.ee11.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.EnumSet;

@Component
public class WhoisServletDeployer implements ServletDeployer {

    private final WhoisResourceConfig resourceConfig;
    private final MaintenanceModeFilter maintenanceModeFilter;
    private final HttpsBasicAuthFiler httpsBasicAuthFiler;
    private final HttpsAuthHeaderFiler httpsAuthHeaderFiler;
    private final SyncUpdatesHttpSchemeFilter syncUpdatesHttpSchemeFilter;
    private final WhoisCrossOriginFilter whoisCrossOriginFilter;
    private final SyncUpdateCORSFilter syncUpdateCORSFilter;
    private final ApplicationContext applicationContext;

    @Autowired
    public WhoisServletDeployer(
                    final WhoisResourceConfig resourceConfig,
                    final MaintenanceModeFilter maintenanceModeFilter,
                    final WhoisCrossOriginFilter whoisCrossOriginFilter,
                    final HttpsBasicAuthFiler httpsBasicAuthFiler,
                    final HttpsAuthHeaderFiler httpsAuthHeaderFiler,
                    final SyncUpdateCORSFilter syncUpdateCORSFilter,
                    final SyncUpdatesHttpSchemeFilter syncUpdatesHttpSchemeFilter,
                    final ApplicationContext applicationContext) {
        this.resourceConfig = resourceConfig;
        this.maintenanceModeFilter = maintenanceModeFilter;
        this.httpsBasicAuthFiler = httpsBasicAuthFiler;
        this.httpsAuthHeaderFiler = httpsAuthHeaderFiler;
        this.syncUpdatesHttpSchemeFilter = syncUpdatesHttpSchemeFilter;
        this.whoisCrossOriginFilter = whoisCrossOriginFilter;
        this.syncUpdateCORSFilter = syncUpdateCORSFilter;
        this.applicationContext = applicationContext;
    }

    @Override
    public void deploy(ServletContextHandler context) {
        context.addFilter(new FilterHolder(maintenanceModeFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(httpsAuthHeaderFiler), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(httpsBasicAuthFiler), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(syncUpdatesHttpSchemeFilter), "/whois/syncupdates/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(syncUpdateCORSFilter), "/whois/syncupdates/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(whoisCrossOriginFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));

        addSpringSecurityContext(context);

        context.addServlet(new ServletHolder("Whois REST API", new ServletContainer(resourceConfig)), "/whois/*");
    }

    private void addSpringSecurityContext(final ServletContextHandler context) {
        final AnnotationConfigWebApplicationContext securityCtx = new AnnotationConfigWebApplicationContext();
        securityCtx.setParent(applicationContext);
        securityCtx.getEnvironment().setActiveProfiles(applicationContext.getEnvironment().getActiveProfiles());

        securityCtx.register(SecurityConfig.class);

        securityCtx.refresh();

        context.addEventListener( new ContextLoaderListener(securityCtx));

        context.addFilter(
                new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain")),
                "/whois/*",
                EnumSet.of(DispatcherType.REQUEST)
        );
    }
}
