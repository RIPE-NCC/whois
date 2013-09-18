package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.SerializationConfig;
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
public class WhoisServletDeployer implements ServletDeployer {

    private final WhoisRestService whoisRestService;
    private final SyncUpdatesService syncUpdatesService;
    private final WhoisMetadata whoisMetadata;
    private final GeolocationService geolocationService;
    private final DefaultExceptionMapper defaultExceptionMapper;
    private final MaintenanceModeFilter maintenanceModeFilter;

    @Autowired
    public WhoisServletDeployer(final WhoisRestService whoisRestService,
                                final SyncUpdatesService syncUpdatesService,
                                final WhoisMetadata whoisMetadata,
                                final GeolocationService geolocationService,
                                final DefaultExceptionMapper defaultExceptionMapper,
                                final MaintenanceModeFilter maintenanceModeFilter) {
        this.whoisRestService = whoisRestService;
        this.syncUpdatesService = syncUpdatesService;
        this.whoisMetadata = whoisMetadata;
        this.geolocationService = geolocationService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.maintenanceModeFilter = maintenanceModeFilter;
    }

    @Override
    public Audience getAudience() {
        return Audience.PUBLIC;
    }

    @Override
    public void deploy(WebAppContext context) {
        context.addFilter(new FilterHolder(maintenanceModeFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(whoisRestService);
        resourceConfig.register(syncUpdatesService);
        resourceConfig.register(whoisMetadata);
        resourceConfig.register(geolocationService);
        resourceConfig.register(defaultExceptionMapper);
        final JacksonJaxbJsonProvider jaxbJsonProvider = new JacksonJaxbJsonProvider();
        jaxbJsonProvider.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        jaxbJsonProvider.configure(SerializationConfig.Feature.WRAP_ROOT_VALUE, false);
        resourceConfig.register(jaxbJsonProvider);
        context.addServlet(new ServletHolder("Whois REST API", new ServletContainer(resourceConfig)), "/whois/*");
    }
}
