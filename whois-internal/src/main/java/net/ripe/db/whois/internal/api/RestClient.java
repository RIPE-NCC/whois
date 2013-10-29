package net.ripe.db.whois.internal.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.WhoisObjectMapper;
import net.ripe.db.whois.api.whois.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

@Component
public final class RestClient {
    private static final Client client;
    private final String restApiUrl;
    private final String sourceName;
    private final WhoisObjectMapper whoisObjectMapper;

    static {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    @Autowired
    public RestClient(
            @Value("${api.rest.baseurl}") final String restApiUrl,
            @Value("${whois.source}") final String sourceName) {
        this.whoisObjectMapper = new WhoisObjectMapper(null, restApiUrl);
        this.restApiUrl = restApiUrl;
        this.sourceName = sourceName;
    }

    public RpslObject create(final RpslObject rpslObject, final String override) {
        final WhoisResources whoisResources = target(rpslObject.getType().getName(), override)
                .request()
                .put(Entity.entity(whoisObjectMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject read(final ObjectType objectType, final String pkey) {
        final WhoisResources whoisResources = target(objectType.getName(), pkey)
                .request()
                .get(WhoisResources.class);
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject update(final RpslObject rpslObject, final String override) {
        final WhoisResources whoisResources = target(rpslObject.getType().getName(), rpslObject.getKey().toString(), override)
                .request()
                .put(Entity.entity(whoisObjectMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public void delete(final RpslObject rpslObject, final String override) {
        target(rpslObject.getType().getName(), rpslObject.getKey().toString(), override)
                    .request()
                    .delete(String.class);
    }

    private WebTarget target(final String objectType, final String objectKey) {
        return target(objectType, objectKey, null);
    }

    private WebTarget target(final String objectType, final String objectKey, final String override) {
       return client.target(String.format("%s/%s/%s/%s%s",
               restApiUrl,
               sourceName,
               objectType,
               objectKey,
               StringUtils.isNotEmpty(override) ? String.format("?%s", override) : ""));
    }
}
