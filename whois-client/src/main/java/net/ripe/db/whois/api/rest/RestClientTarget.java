package net.ripe.db.whois.api.rest;


import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.springframework.util.MultiValueMap;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RestClientTarget {

    private Client client;
    private String baseUrl;
    private String source;
    private WhoisObjectClientMapper mapper;
    private NotifierCallback notifierCallback;
    private MultivaluedMap<String, String> params = new MultivaluedStringMap();
    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private List<Cookie> cookies = Lists.newArrayList();

    RestClientTarget(final Client client, final String baseUrl, final String source, final WhoisObjectClientMapper mapper) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.source = source;
        this.mapper = mapper;
    }

    // builder methods

    public RestClientTarget addCookie(final Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    public RestClientTarget addParam(final String key, final String value) {
        params.add(key, value);
        return this;
    }

    public RestClientTarget addParams(final String key, final Iterable<String> values) {
        for (String value : values) {
            addParam(key, value);
        }
        return this;
    }

    public RestClientTarget addParams(final String key, final String ... values) {
        for (String value : values) {
            addParam(key, value);
        }
        return this;
    }

    public RestClientTarget addParams(final MultivaluedMap map) {
        params.putAll(map);
        return this;
    }

    public RestClientTarget addParams(final MultiValueMap map) {
        params.putAll(map);
        return this;
    }

    public RestClientTarget setNotifier(final NotifierCallback notifierCallback) {
        this.notifierCallback = notifierCallback;
        return this;
    }

    public RestClientTarget addHeader(final String key, final String value) {
        this.headers.add(key, value);
        return this;
    }

    public RestClientTarget addHeaders(final String key, final Iterable<String> values) {
        for (String value : values) {
            addHeader(key, value);
        }
        return this;
    }

    public RestClientTarget addHeaders(final MultivaluedMap map) {
        headers.putAll(map);
        return this;
    }

    public RestClientTarget addHeaders(final MultiValueMap map) {
        headers.putAll(map);
        return this;
    }

    // client calls

    public RpslObject create(final RpslObject rpslObject) {
        try {
            WebTarget webTarget = client.target(String.format("%s/%s/%s",
                    baseUrl,
                    source,
                    rpslObject.getType().getName()));
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request
                    .post(Entity.entity(mapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);

            if (notifierCallback != null){
                notifierCallback.notify(whoisResources.getErrorMessages());
            }

            return mapper.map(whoisResources.getWhoisObjects().get(0));

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject update(final RpslObject rpslObject) {
        try {
            WebTarget webTarget = client.target(String.format("%s/%s/%s/%s",
                    baseUrl,
                    source,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString()));
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request
                    .put(Entity.entity(mapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);

            return mapper.map(whoisResources.getWhoisObjects().get(0));

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject delete(final RpslObject rpslObject) {
        try {
            WebTarget webTarget = client.target(String.format("%s/%s/%s/%s",
                    baseUrl,
                    source,
                    rpslObject.getType().getName(),
                    rpslObject.getKey().toString()));
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request.delete(WhoisResources.class);

            return mapper.map(whoisResources.getWhoisObjects().get(0));

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject lookup(final ObjectType objectType, final String pkey) {
        try {
            WebTarget webTarget = client.target(String.format("%s/%s/%s/%s",
                    baseUrl,
                    source,
                    objectType.getName(),
                    pkey));
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request.get(WhoisResources.class);

            return mapper.map(whoisResources.getWhoisObjects().get(0));

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public AbuseContact lookupAbuseContact(final String resource) {
        try {
            AbuseResources abuseResources = client.target(String.format("%s/abuse-contact/%s",
                    baseUrl, resource))
                    .request()
                    .get(AbuseResources.class);

            return abuseResources.getAbuseContact();

        } catch (ClientErrorException e) {
            throw createExceptionFromMessage(e);
        }
    }

    // TODO: [AH] make this streaming; result can be gigantic
    public Collection<RpslObject> search() {
        try {
            WebTarget webTarget = client.target(String.format("%s/search", baseUrl));
            webTarget = setParams(webTarget);

            final WhoisResources whoisResources = webTarget
                    .request()
                    .get(WhoisResources.class);

            return mapper.mapWhoisObjects(whoisResources.getWhoisObjects());

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    // helper methods

    private WebTarget setParams(final WebTarget webTarget) {
        WebTarget updatedWebTarget = webTarget;
        for (Map.Entry<String,List<String>> param : params.entrySet()) {
            updatedWebTarget = updatedWebTarget.queryParam(param.getKey(), param.getValue().toArray());
        }
        return updatedWebTarget;
    }

    private Invocation.Builder setCookies(final Invocation.Builder request) {
        for (Cookie cookie : cookies) {
            request.cookie(cookie);
        }
        return request;
    }

    private Invocation.Builder setHeaders(final Invocation.Builder request) {
        request.headers(headers);
        return request;
    }

    private static RuntimeException createException(final ClientErrorException e) {
        try {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            return new RestClientException(whoisResources.getErrorMessages());
        } catch (ProcessingException | IllegalStateException e1) {
            return createExceptionFromMessage(e);
        }
    }

    private static RuntimeException createExceptionFromMessage(final ClientErrorException e) {
        String message;
        try {
            message = e.getResponse().readEntity(String.class);
        } catch (IllegalStateException e1) {
            // stream has already been closed
            message = e.getMessage();
        }

        final ErrorMessage errorMessage = new ErrorMessage(new Message(Messages.Type.ERROR, message));
        return new RestClientException(message);
    }
}
