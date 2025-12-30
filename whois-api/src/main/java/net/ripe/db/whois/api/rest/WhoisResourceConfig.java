package net.ripe.db.whois.api.rest;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import jakarta.ws.rs.ext.MessageBodyWriter;
import net.ripe.db.whois.api.autocomplete.AutocompleteService;
import net.ripe.db.whois.api.fulltextsearch.FullTextSearchService;
import net.ripe.db.whois.api.healthcheck.HealthCheckService;
import net.ripe.db.whois.api.httpserver.ClientCertificateService;
import net.ripe.db.whois.api.httpserver.DefaultExceptionMapper;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import org.glassfish.jersey.jaxb.internal.JaxbMessagingBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WhoisResourceConfig extends ResourceConfig {

    @Autowired
    public WhoisResourceConfig(
                final WhoisRestService whoisRestService,
                final WhoisSearchService whoisSearchService,
                final WhoisVersionService whoisVersionService,
                final SyncUpdatesService syncUpdatesService,
                final WhoisMetadata whoisMetadata,
                final GeolocationService geolocationService,
                final AbuseContactService abuseContactService,
                final AutocompleteService autocompleteService,
                final ReferencesService referencesService,
                final DefaultExceptionMapper defaultExceptionMapper,
                final DomainObjectService domainObjectService,
                final FullTextSearchService fullTextSearch,
                final BatchUpdatesService batchUpdatesService,
                final HealthCheckService healthCheckService,
                final ClientCertificateService clientCertificateService) {
        EncodingFilter.enableFor(this, GZipEncoder.class);
        EncodingFilter.enableFor(this, DeflateEncoder.class);
        register(MultiPartFeature.class);
        register(whoisRestService);
        register(whoisSearchService);
        register(whoisVersionService);
        register(syncUpdatesService);
        register(whoisMetadata);
        register(geolocationService);
        register(abuseContactService);
        register(autocompleteService);
        register(referencesService);
        register(defaultExceptionMapper);
        register(domainObjectService);
        register(fullTextSearch);
        register(batchUpdatesService);
        register(healthCheckService);
        register(clientCertificateService);
        register(new CacheControlFilter());
        register(new HttpBasicAuthResponseFilter());

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
        register(jaxbJsonProvider);

        final MessageBodyWriter<WhoisResources> customMessageBodyWriter = new WhoisResourcesPlainTextWriter();
        register(customMessageBodyWriter);

        register(new JaxbMessagingBinder());
    }

}
