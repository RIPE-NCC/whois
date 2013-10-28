package net.ripe.db.whois.api.whois;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.api.httpserver.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
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
    private final AbuseContactService abuseContactService;
    private final DefaultExceptionMapper defaultExceptionMapper;
    private final MaintenanceModeFilter maintenanceModeFilter;

    @Autowired
    public WhoisServletDeployer(final WhoisRestService whoisRestService,
                                final SyncUpdatesService syncUpdatesService,
                                final WhoisMetadata whoisMetadata,
                                final GeolocationService geolocationService,
                                final AbuseContactService abuseContactService,
                                final DefaultExceptionMapper defaultExceptionMapper,
                                final MaintenanceModeFilter maintenanceModeFilter) {
        this.whoisRestService = whoisRestService;
        this.syncUpdatesService = syncUpdatesService;
        this.whoisMetadata = whoisMetadata;
        this.geolocationService = geolocationService;
        this.abuseContactService = abuseContactService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.maintenanceModeFilter = maintenanceModeFilter;
    }

    @Override
    public void deploy(WebAppContext context) {
        context.addFilter(new FilterHolder(maintenanceModeFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));
        final ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(MultiPartFeature.class);
        resourceConfig.register(whoisRestService);
        resourceConfig.register(syncUpdatesService);
        resourceConfig.register(whoisMetadata);
        resourceConfig.register(geolocationService);
        resourceConfig.register(abuseContactService);
        resourceConfig.register(defaultExceptionMapper);
        final JacksonJaxbJsonProvider jaxbJsonProvider = new JacksonJaxbJsonProvider();
        jaxbJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);
        jaxbJsonProvider.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        resourceConfig.register(jaxbJsonProvider);
        context.addServlet(new ServletHolder("Whois REST API", new ServletContainer(resourceConfig)), "/whois/*");
    }
}
