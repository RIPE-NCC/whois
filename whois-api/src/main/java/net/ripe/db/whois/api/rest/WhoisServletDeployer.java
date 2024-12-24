package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import jakarta.servlet.DispatcherType;
import jakarta.ws.rs.ext.MessageBodyWriter;
import net.ripe.db.whois.api.autocomplete.AutocompleteService;
import net.ripe.db.whois.api.fulltextsearch.FullTextSearchService;
import net.ripe.db.whois.api.healthcheck.HealthCheckService;
import net.ripe.db.whois.api.httpserver.ClientCertificateService;
import net.ripe.db.whois.api.httpserver.DefaultExceptionMapper;
import net.ripe.db.whois.api.httpserver.ServletDeployer;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
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
    private final ReferencesService referencesService;
    private final DefaultExceptionMapper defaultExceptionMapper;
    private final MaintenanceModeFilter maintenanceModeFilter;
    private final DomainObjectService domainObjectService;
    private final FullTextSearchService fullTextSearch;
    private final BatchUpdatesService batchUpdatesService;
    private final HealthCheckService healthCheckService;
    private final ClientCertificateService clientCertificateService;
    private final HttpsBasicAuthCustomizer httpsBasicAuthCustomizer;
    private final HttpsAPIKeyAuthCustomizer httpsAPIKeyAuthCustomizer;
    private final SyncUpdatesHttpSchemeFilter syncUpdatesHttpSchemeFilter;

    @Autowired
    public WhoisServletDeployer(final WhoisRestService whoisRestService,
                                final WhoisSearchService whoisSearchService,
                                final WhoisVersionService whoisVersionService,
                                final SyncUpdatesService syncUpdatesService,
                                final WhoisMetadata whoisMetadata,
                                final GeolocationService geolocationService,
                                final AbuseContactService abuseContactService,
                                final AutocompleteService autocompleteService,
                                final ReferencesService referencesService,
                                final DefaultExceptionMapper defaultExceptionMapper,
                                final MaintenanceModeFilter maintenanceModeFilter,
                                final DomainObjectService domainObjectService,
                                final FullTextSearchService fullTextSearch,
                                final BatchUpdatesService batchUpdatesService,
                                final HealthCheckService healthCheckService,
                                final HttpsBasicAuthCustomizer httpsBasicAuthCustomizer,
                                final HttpsAPIKeyAuthCustomizer httpsAPIKeyAuthCustomizer,
                                final ClientCertificateService clientCertificateService,
                                final SyncUpdatesHttpSchemeFilter syncUpdatesHttpSchemeFilter) {
        this.whoisRestService = whoisRestService;
        this.whoisSearchService = whoisSearchService;
        this.whoisVersionService = whoisVersionService;
        this.syncUpdatesService = syncUpdatesService;
        this.whoisMetadata = whoisMetadata;
        this.geolocationService = geolocationService;
        this.abuseContactService = abuseContactService;
        this.autocompleteService = autocompleteService;
        this.referencesService = referencesService;
        this.defaultExceptionMapper = defaultExceptionMapper;
        this.maintenanceModeFilter = maintenanceModeFilter;
        this.domainObjectService = domainObjectService;
        this.fullTextSearch = fullTextSearch;
        this.batchUpdatesService = batchUpdatesService;
        this.healthCheckService = healthCheckService;
        this.clientCertificateService = clientCertificateService;
        this.httpsBasicAuthCustomizer = httpsBasicAuthCustomizer;
        this.httpsAPIKeyAuthCustomizer = httpsAPIKeyAuthCustomizer;
        this.syncUpdatesHttpSchemeFilter = syncUpdatesHttpSchemeFilter;
    }

    @Override
    public void deploy(WebAppContext context) {
        context.addFilter(new FilterHolder(maintenanceModeFilter), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(httpsAPIKeyAuthCustomizer), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(httpsBasicAuthCustomizer), "/whois/*", EnumSet.allOf(DispatcherType.class));
        context.addFilter(new FilterHolder(syncUpdatesHttpSchemeFilter), "/whois/syncupdates/*", EnumSet.allOf(DispatcherType.class));

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
        resourceConfig.register(referencesService);
        resourceConfig.register(defaultExceptionMapper);
        resourceConfig.register(domainObjectService);
        resourceConfig.register(fullTextSearch);
        resourceConfig.register(batchUpdatesService);
        resourceConfig.register(healthCheckService);
        resourceConfig.register(clientCertificateService);
        resourceConfig.register(new CacheControlFilter());
        resourceConfig.register(new HttpBasicAuthResponseFilter());

        final ObjectMapper objectMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();
        objectMapper.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(
                        new JacksonAnnotationIntrospector(),
                        new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())));

        final JacksonJsonProvider jaxbJsonProvider = new JacksonJsonProvider();
        jaxbJsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);
        jaxbJsonProvider.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        jaxbJsonProvider.setMapper(objectMapper);
        resourceConfig.register(jaxbJsonProvider);

        final MessageBodyWriter<WhoisResources> customMessageBodyWriter = new WhoisResourcesPlainTextWriter();
        resourceConfig.register(customMessageBodyWriter);

        resourceConfig.register(new JaxbMessagingBinder());

        // only allow cross-origin requests from ripe.net
        final FilterHolder crossOriginFilterHolder = context.addFilter(org.eclipse.jetty.servlets.CrossOriginFilter.class, "/whois/*", EnumSet.allOf(DispatcherType.class));
        crossOriginFilterHolder.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "https?://*.ripe.net");

        context.addServlet(new ServletHolder("Whois REST API", new ServletContainer(resourceConfig)), "/whois/*");
    }
}
