package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@Component
public class RestClient {
    private final Client client;
    private String restApiUrl;
    private String sourceName;
    private WhoisObjectClientMapper whoisObjectClientMapper;

    public RestClient() {
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
                queryParams(queryParam("password", passwords))
        )).request().post(
                Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                WhoisResources.class
        );
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject createOverride(final RpslObject rpslObject, final String override) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                queryParams(queryParam("override", override))
        )).request().post(
                Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                WhoisResources.class
        );
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject update(final RpslObject rpslObject, final String... passwords) {
        WhoisResources entity = whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject));
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                queryParams(queryParam("password", passwords))
        )).request().put(
                Entity.entity(entity, MediaType.APPLICATION_XML),
                WhoisResources.class
        );
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public RpslObject updateOverride(final RpslObject rpslObject, final String override) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                queryParams(queryParam("override", override))
        )).request().put(
                Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                WhoisResources.class
        );
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public void delete(final RpslObject rpslObject, final String... passwords) {
        client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                queryParams(queryParam("password", passwords))
        )).request().delete(String.class);
    }

    public void deleteOverride(final RpslObject rpslObject, final String override) {
        client.target(String.format("%s/%s/%s/%s%s",
                restApiUrl,
                sourceName,
                rpslObject.getType().getName(),
                rpslObject.getKey().toString(),
                queryParams(queryParam("override", override))
        )).request().delete(String.class);
    }

    public RpslObject lookup(final ObjectType objectType, final String pkey, final String... passwords) {
        final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s%s",
                restApiUrl,
                sourceName,
                objectType.getName(),
                pkey,
                queryParams(queryParam("password", passwords)),
                (passwords.length == 0) ? "?unfiltered" : "&unfiltered"
        )).request().get(WhoisResources.class);
        return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
    }

    public AbuseContact lookupAbuseContact(final String resource) {
        AbuseResources abuseResources = client.target(String.format("%s/abuse-contact/%s",
                restApiUrl,
                resource
        )).request().get(AbuseResources.class);
        return abuseResources.getAbuseContact();
    }

    public Iterable<RpslObject> search(String searchKey,
                                       Set<String> sources,
                                       Set<AttributeType> inverseAttributes,
                                       Set<String> includeTags,
                                       Set<String> excludeTags,
                                       Set<ObjectType> types,
                                       Set<QueryFlag> flags) {

        final String uri = String.format("%s/search%s",
                restApiUrl,
                queryParams(
                        queryParam("query-string", RestClientUtils.encode(searchKey)),
                        queryParam("source", sources),
                        queryParam("inverse-attribute", Collections2.transform(inverseAttributes, new Function<AttributeType, String>() {
                            @Nullable
                            @Override
                            public String apply(@Nullable AttributeType input) {
                                return input.getName();
                            }
                        })),
                        queryParam("include-tag", includeTags),
                        queryParam("exclude-tag", excludeTags),
                        queryParam("type-filter", Collections2.transform(types, new Function<ObjectType, String>() {
                            @Nullable
                            @Override
                            public String apply(@Nullable ObjectType input) {
                                return (input == null ? null : input.getName());
                            }
                        })),
                        queryParam("flags", Collections2.transform(flags, new Function<QueryFlag, String>() {
                            @Nullable
                            @Override
                            public String apply(@Nullable QueryFlag input) {
                                return (input == null ? null : input.getName());
                            }
                        }))
                )
        );
        final WhoisResources whoisResources = client.target(uri).request().get(WhoisResources.class);

        return whoisObjectClientMapper.mapWhoisObjects(whoisResources.getWhoisObjects());
    }

    public String queryParams(final String... queryParam) {
        StringBuilder res = null;
        for (String s : queryParam) {
            if (!StringUtils.isBlank(s)) {
                if (res == null) {
                    res = new StringBuilder("?").append(s);
                } else {
                    res.append('&').append(s);
                }
            }
        }
        return res==null ? "" : res.toString();
    }

    public String queryParam(final String queryParam, final String... values) {
        return queryParam(queryParam, Arrays.asList(values));
    }

    public String queryParam(final String queryParam, final Collection<String> values) {
        if (!CollectionUtils.isEmpty(values)) {
            return String.format("%s=%s", queryParam, StringUtils.join(values, "&" + queryParam + "="));
        }
        return "";
    }
}
