package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.api.autocomplete.AutocompleteService;
import net.ripe.db.whois.api.autocomplete.ElasticAutocompleteService;
import net.ripe.db.whois.api.fulltextsearch.FullTextSearch;
import net.ripe.db.whois.api.httpserver.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.jaxb.internal.JaxbMessagingBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

@Component
public class WhoisServletDeployer implements ServletDeployer {

    private final WhoisRestService whoisRestService;
    private final WhoisSearchService whoisSearchService;
    private final WhoisVersionService whoisVersionService;
    private final SyncUpdatesService syncUpdatesService;
    private final WhoisMetadata whoisMetadata;
    private final GeolocationService geolocationService;
    private final AbuseContactService abuseContactService;
    private final AutocompleteService autocompleteService;
    private final ElasticAutocompleteService elasticAutocompleteService;
    private final ReferencesService referencesService;
    private final DefaultExceptionMapper defaultExceptionMapper;
    private final MaintenanceModeFilter maintenanceModeFilter;
    private final DomainObjectService domainObjectService;
    private final FullTextSearch fullTextSearch;
    private final BatchUpdatesService batchUpdatesService;
    private final HealthCheckService healthCheckService;

    @Autowired
    public WhoisServletDeployer(final WhoisRestService whoisRestService,
                                final WhoisSearchService whoisSearchService,
                                final WhoisVersionService whoisVersionService,
                                final SyncUpdatesService syncUpdatesService,
                                final WhoisMetadata whoisMetadata,
                                final GeolocationService geolocationService,
                                final AbuseContactService abuseContactService,
                                final AutocompleteService autocompleteService,
                                final ElasticAutocompleteService elasticAutocompleteService,
                                final ReferencesService referencesService,
                                final DefaultExceptionMapper defaultExceptionMapper,
                                final MaintenanceModeFilter maintenanceModeFilter,
                                final DomainObjectService domainObjectService,
                                final FullTextSearch fullTextSearch,
                                final BatchUpdatesService batchUpdatesService,
                                final HealthCheckService healthCheckService) {
        this.whoisRestService = whoisRestService;
        this.whoisSearchService = whoisSearchService;
        this.whoisVersionService = whoisVersionService;
        this.syncUpdatesService = syncUpdatesService;
        this.whoisMetadata = whoisMetadata;
        this.geolocationService = geolocationService;
        this.abuseContactService = abuseContactService;
        this.autocompleteService = autocompleteService;
        this.elasticAutocompleteService = elasticAutocompleteService;
        this.referencesService = referencesService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.maintenanceModeFilter = maintenanceModeFilter;
        this.domainObjectService = domainObjectService;
        this.fullTextSearch = fullTextSearch;
        this.batchUpdatesService = batchUpdatesService;
        this.healthCheckService = healthCheckService;
    }

    @Override
    public void deploy(WebAppContext context) {
        context.addFilter(new FilterHolder(maintenanceModeFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));

        final ResourceConfig resourceConfig = new ResourceConfig();
        EncodingFilter.enableFor(resourceConfig, GZipEncoder.class);
        EncodingFilter.enableFor(resourceConfig, DeflateEncoder.class);
        resourceConfig.register(MultiPartFeature.class);
        resourceConfig.register(whoisRestService);
        resourceConfig.register(whoisSearchService);
        resourceConfig.register(whoisVersionService);
        resourceConfig.register(syncUpdatesService);
        resourceConfig.register(whoisMetadata);
        resourceConfig.register(geolocationService);
        resourceConfig.register(abuseContactService);
        resourceConfig.register(autocompleteService);
        resourceConfig.register(elasticAutocompleteService);
        resourceConfig.register(referencesService);
        resourceConfig.register(defaultExceptionMapper);
        resourceConfig.register(domainObjectService);
        resourceConfig.register(fullTextSearch);
        resourceConfig.register(batchUpdatesService);
        resourceConfig.register(healthCheckService);
        resourceConfig.register(new CacheControlFilter());

        final JacksonJaxbJsonProvider jaxbJsonProvider = new JacksonJaxbJsonProvider();
        jaxbJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);
        jaxbJsonProvider.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        resourceConfig.register(jaxbJsonProvider);

        resourceConfig.register(new JaxbMessagingBinder());

        // only allow cross-origin requests from ripe.net
        final FilterHolder crossOriginFilterHolder = context.addFilter(org.eclipse.jetty.servlets.CrossOriginFilter.class, "/whois/*", EnumSet.allOf(DispatcherType.class));
        crossOriginFilterHolder.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "https?://*.ripe.net");

        context.addServlet(new ServletHolder("Whois REST API", new ServletContainer(resourceConfig)), "/whois/*");
    }
}
