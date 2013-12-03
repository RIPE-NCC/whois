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

import javax.annotation.Nullable;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

@Component
public class RestClient {
    private Client client;
    private String restApiUrl;
    private String sourceName;
    private WhoisObjectClientMapper whoisObjectClientMapper;

    public RestClient() {
        this.client = createClient();
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

    public void setClient(final Client client) {
        this.client = client;
    }

    public RpslObject create(final RpslObject rpslObject, final String... passwords) {
        try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    joinQueryParams(createQueryParams("password", passwords))
            )).request().post(
                    Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                    WhoisResources.class
            );
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject createOverride(final RpslObject rpslObject, final String override) {
        try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    joinQueryParams(createQueryParams("override", override))
            )).request().post(
                    Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                    WhoisResources.class
            );
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject update(final RpslObject rpslObject, final String... passwords) {
        try {
            WhoisResources entity = whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject));
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(createQueryParams("password", passwords))
            )).request().put(Entity.entity(entity, MediaType.APPLICATION_XML), WhoisResources.class);
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject updateOverride(final RpslObject rpslObject, final String override) {
        try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(createQueryParams("override", override))
            )).request().put(
                    Entity.entity(whoisObjectClientMapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML),
                    WhoisResources.class
            );
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public void delete(final RpslObject rpslObject, final String... passwords) {
        try {
            client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(createQueryParams("password", passwords))
            )).request().delete(String.class);
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public void deleteOverride(final RpslObject rpslObject, final String override) {
        try {
            client.target(String.format("%s/%s/%s/%s%s",
                    restApiUrl,
                    sourceName,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString(),
                    joinQueryParams(createQueryParams("override", override))
            )).request().delete(String.class);
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject lookup(final ObjectType objectType, final String pkey, final String... passwords) {
        try {
            final WhoisResources whoisResources = client.target(String.format("%s/%s/%s/%s%s%s",
                    restApiUrl,
                    sourceName,
                    objectType.getName(),
                    pkey,
                    joinQueryParams(createQueryParams("password", passwords)),
                    (passwords.length == 0) ? "?unfiltered" : "&unfiltered"
            )).request().get(WhoisResources.class);
            return whoisObjectClientMapper.map(whoisResources.getWhoisObjects().get(0));
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public AbuseContact lookupAbuseContact(final String resource) {
        try {
            AbuseResources abuseResources = client.target(String.format("%s/abuse-contact/%s",
                    restApiUrl,
                    resource
            )).request().get(AbuseResources.class);
            return abuseResources.getAbuseContact();
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public Iterable<RpslObject> search(String searchKey,
                                       Set<String> sources,
                                       Set<AttributeType> inverseAttributes,
                                       Set<String> includeTags,
                                       Set<String> excludeTags,
                                       Set<ObjectType> types,
                                       Set<QueryFlag> flags) {
        try {
            final String uri = String.format(
                    "%s/search%s",
                    restApiUrl,
                    joinQueryParams(
                            createQueryParams("query-string", RestClientUtils.encode(searchKey)),
                            createQueryParams("source", sources),
                            createQueryParams("inverse-attribute", Collections2.transform(inverseAttributes,
                                    new Function<AttributeType, String>() {
                                        @Nullable
                                        @Override
                                        public String apply(@Nullable AttributeType input) {
                                            return input.getName();
                                        }
                                    })),
                            createQueryParams("include-tag", includeTags),
                            createQueryParams("exclude-tag", excludeTags),
                            createQueryParams("type-filter", Collections2.transform(types,
                                    new Function<ObjectType, String>() {
                                        @Nullable
                                        @Override
                                        public String apply(@Nullable ObjectType input) {
                                            return (input == null ? null : input.getName());
                                        }
                                    })),
                            createQueryParams("flags", Collections2.transform(flags,
                                    new Function<QueryFlag, String>() {
                                        @Nullable
                                        @Override
                                        public String apply(@Nullable QueryFlag input) {
                                            return (input == null ? null : input.getName());
                                        }
                                    }))
                    ));

            final WhoisResources whoisResources = client.target(uri).request().get(WhoisResources.class);
            return whoisObjectClientMapper.mapWhoisObjects(whoisResources.getWhoisObjects());
        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    private static Client createClient() {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return ClientBuilder.newBuilder()
                .register(MultiPartFeature.class)
                .register(jsonProvider)
                .build();
    }

    private static String joinQueryParams(final String... queryParams) {
        final StringBuilder result = new StringBuilder();
        for (String queryParam : queryParams) {
            if (!StringUtils.isBlank(queryParam)) {
                if (result.length() == 0) {
                    result.append('?').append(queryParam);
                } else {
                    result.append('&').append(queryParam);
                }
            }
        }
        return result.toString();
    }

    private static String createQueryParams(final String key, final String... values) {
        return createQueryParams(key, Arrays.asList(values));
    }

    private static String createQueryParams(final String key, final Collection<String> values) {
        final StringBuilder result = new StringBuilder();
        for (String value : values) {
            if (result.length() > 0) {
                result.append('&');
            }
            result.append(key).append('=').append(value);
        }
        return result.toString();
    }

    private static RuntimeException createException(final ClientErrorException e) {
        // TODO: is there always a WhoisResources object when there is an error?
        try {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            return new RestClientException(whoisResources.getErrorMessages());
        } catch (ProcessingException | IllegalStateException e1) {
            // TODO: handle in some way?
            return e;
        }
    }
}
