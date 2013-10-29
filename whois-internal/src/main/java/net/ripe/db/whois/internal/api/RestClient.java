package net.ripe.db.whois.internal.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.WhoisObjectMapper;
import net.ripe.db.whois.api.whois.domain.WhoisResources;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

@Component
public class RestClient {
    private static final Client client;

    static {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    private String restApiUrl;
    private final WhoisObjectMapper whoisObjectMapper;

    public RestClient() {
        this.whoisObjectMapper = new WhoisObjectMapper(null, restApiUrl);
    }

    @Value("${api.rest.baseurl}")
    public void setRestApiUrl(String restApiUrl) {
        this.restApiUrl = restApiUrl;
    }

    public final RpslObject lookup(ObjectType objectType, String pkey) {
        final WhoisResources whoisResources = client.target(String.format("%s/ripe/%s/%s", restApiUrl, objectType.getName(), pkey)).request().get(WhoisResources.class);
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public final RpslObject create(RpslObject rpslObject, String override) {
        final WhoisResources whoisResources = client.target(String.format("%s/ripe/%s?override=%s", restApiUrl, rpslObject.getType().getName(), override)).request()
                .put(Entity.entity(whoisObjectMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public final RpslObject update(RpslObject rpslObject, String override) {
        final WhoisResources whoisResources = client.target(String.format("%s/ripe/%s/%s?override=%s", restApiUrl, rpslObject.getType().getName(), rpslObject.getKey(), override)).request()
                .put(Entity.entity(whoisObjectMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectMapper.map(whoisResources.getWhoisObjects().get(0));
    }
}
