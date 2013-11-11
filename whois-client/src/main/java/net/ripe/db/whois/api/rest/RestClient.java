package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

@Component
public final class RestClient {
    private static final Client client;
    private String restApiUrl;
    private String sourceName;
    private WhoisObjectClientMapper whoisObjectClientMapper;

    static {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        client = ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    @Value("${api.rest.baseurl}")
    public void setRestApiUrl(final String restApiUrl) {
        this.restApiUrl = restApiUrl;
        this.whoisObjectClientMapper = new WhoisObjectClientMapper(restApiUrl);
    }

    @Value("${whois.source}")
    public void setSource(final String sourceName) {
        this.sourceName = sourceName;
    }

    public RpslObject create(final RpslObject rpslObject, final String... passwords) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                formatPasswords(passwords)))
                .request()
                .post(Entity.entity(whoisObjectClientMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject createOverride(final RpslObject rpslObject, final String override) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                StringUtils.isNotEmpty(override) ? String.format("?override=%s", override) : ""))
                .request()
                .post(Entity.entity(whoisObjectClientMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject update(final RpslObject rpslObject, final String... passwords) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                formatPasswords(passwords)))
                .request()
                .put(Entity.entity(whoisObjectClientMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject updateOverride(final RpslObject rpslObject, final String override) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                StringUtils.isNotEmpty(override) ? String.format("?override=%s", override) : ""))
                .request()
                .put(Entity.entity(whoisObjectClientMapper.map(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public void delete(final RpslObject rpslObject, final String... passwords) {
        client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                formatPasswords(passwords)))
                .request()
                .delete(String.class);
    }

    public void deleteOverride(final RpslObject rpslObject, final String override) {
        client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                StringUtils.isNotEmpty(override) ? String.format("?override=%s", override) : ""))
                .request()
                .delete(String.class);
    }

    public RpslObject lookup(final ObjectType objectType, final String pkey) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s?unfiltered",
                restApiUrl,
                sourceName,
                objectType.getName(),
                pkey)).request()
                .get(WhoisResources.class);
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public AbuseContact abuseContact(final String resource, final String source) {
        AbuseResources abuseResources = client.target(String.format("%s/abuse-contact/%s/%s", restApiUrl, source, resource)).request().get(AbuseResources.class);
        return abuseResources.getAbuseContact();
    }

    String formatPasswords(String... passwords) {
        if (passwords.length > 0) {
            return String.format("?password=%s", StringUtils.join(passwords, "&password="));
        }
        return "";
    }
}
