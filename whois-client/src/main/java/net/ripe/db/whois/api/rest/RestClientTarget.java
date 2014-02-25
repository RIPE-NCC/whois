package net.ripe.db.whois.api.rest;


import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.AbuseContact;
import net.ripe.db.whois.api.rest.domain.AbuseResources;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.StringUtils;
import sun.net.www.protocol.http.HttpURLConnection;

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
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RestClientTarget {
    private Client client;
    private String baseUrl;
    private String source;
    private WhoisObjectClientMapper mapper;
    private NotifierCallback notifierCallback;
    private MultivaluedMap<String, String> params = new MultivaluedHashMap<>();
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

    public RestClientTarget addParams(final String key, final String... values) {
        for (String value : values) {
            addParam(key, value);
        }
        return this;
    }

    public RestClientTarget addParams(final MultivaluedMap map) {
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

    // client calls

    public RpslObject create(final RpslObject rpslObject) {
        try {
            WebTarget webTarget = client.target(baseUrl)
                    .path(source)
                    .path(rpslObject.getType().getName());
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request
                    .post(Entity.entity(mapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);

            if (notifierCallback != null) {
                notifierCallback.notify(whoisResources.getErrorMessages());
            }

            return mapper.map(whoisResources.getWhoisObjects().get(0));

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject update(final RpslObject rpslObject) {
        try {
            WebTarget webTarget = client.target(baseUrl)
                    .path(source)
                    .path(rpslObject.getType().getName())
                    .path(rpslObject.getKey().toString());
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request
                    .put(Entity.entity(mapper.mapRpslObjects(Lists.newArrayList(rpslObject)), MediaType.APPLICATION_XML), WhoisResources.class);

            if (notifierCallback != null) {
                notifierCallback.notify(whoisResources.getErrorMessages());
            }

            return mapper.map(whoisResources.getWhoisObjects().get(0));

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject delete(final RpslObject rpslObject) {
        try {
            WebTarget webTarget = client.target(baseUrl)
                    .path(source)
                    .path(rpslObject.getType().getName())
                    .path(rpslObject.getKey().toString());
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request.delete(WhoisResources.class);

            if (notifierCallback != null) {
                notifierCallback.notify(whoisResources.getErrorMessages());
            }

            return mapper.map(whoisResources.getWhoisObjects().get(0));

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public RpslObject lookup(final ObjectType objectType, final String pkey) {
        return mapper.map(lookupRaw(objectType, pkey));
    }

    public WhoisObject lookupRaw(final ObjectType objectType, final String pkey) {
        try {
            WebTarget webTarget = client.target(baseUrl)
                    .path(source)
                    .path(objectType.getName())
                    .path(pkey);
            webTarget = webTarget.queryParam("unfiltered", "");
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request.get(WhoisResources.class);

            if (notifierCallback != null) {
                notifierCallback.notify(whoisResources.getErrorMessages());
            }

            return whoisResources.getWhoisObjects().get(0);

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    public AbuseContact lookupAbuseContact(final String resource) {
        try {
            final Invocation.Builder request = client.target(baseUrl)
                    .path("abuse-contact")
                    .path(resource)
                    .request();

            setCookies(request);
            setHeaders(request);

            AbuseResources abuseResources = request.get(AbuseResources.class);

            return abuseResources.getAbuseContact();

        } catch (ClientErrorException e) {
            throw createExceptionFromMessage(e);
        }
    }

    /**
     * beware, this imlementation is not streaming; result can be gigantic
     */
    public Collection<RpslObject> search() {
        try {
            WebTarget webTarget = client.target(baseUrl)
                    .path("search");
            webTarget = setParams(webTarget);

            final Invocation.Builder request = webTarget.request();

            setCookies(request);
            setHeaders(request);

            final WhoisResources whoisResources = request.get(WhoisResources.class);

            if (notifierCallback != null) {
                notifierCallback.notify(whoisResources.getErrorMessages());
            }

            return mapper.mapWhoisObjects(whoisResources.getWhoisObjects());

        } catch (ClientErrorException e) {
            throw createException(e);
        }
    }

    /**
     * Returned objects is a Closeable; caller *MUST* close it once finished processing it.
     * Recommended to use call this method in a try-with-resource.
     */
    public StreamingRestClient streamingSearch() {
        URLConnection urlConnection = null;
        try {
            final WebTarget webTarget = client.target(baseUrl).path("search");
            urlConnection = setParams(webTarget).getUri().toURL().openConnection();

            setHeaders(urlConnection);
            setCookies(urlConnection);

            return new StreamingRestClient(urlConnection.getInputStream());
        } catch (IOException e) {
            try (InputStream errorStream = ((HttpURLConnection) urlConnection).getErrorStream()) {
                final WhoisResources whoisResources = StreamingRestClient.unMarshalError(errorStream);
                throw new RestClientException(whoisResources.getErrorMessages());
            } catch (IOException e1) {
                throw new RestClientException(e1.getMessage());
            }
        }
    }

    // helper methods
    private WebTarget setParams(final WebTarget webTarget) {
        WebTarget updatedWebTarget = webTarget;
        for (Map.Entry<String, List<String>> param : params.entrySet()) {
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
        // add headers 1 by 1 instead of replacing all (and wiping out cookies)
        final Set<String> headerKeys = headers.keySet();
        for (String header : headerKeys) {
            request.header(header, headers.get(header));
        }
        return request;
    }

    private void setHeaders(URLConnection urlConnection) {
        for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
            urlConnection.setRequestProperty(entry.getKey(), StringUtils.join(entry.getValue(), ','));
        }
    }

    private void setCookies(URLConnection urlConnection) {
        StringBuilder cookieHeader = new StringBuilder();
        for (Cookie cookie : cookies) {
            if (cookieHeader.length() > 0) {
                cookieHeader.append("; ");
            }
            cookieHeader.append(cookie.getName()).append('=').append(cookie.getValue());
        }

        urlConnection.setRequestProperty("Cookie", cookieHeader.toString());
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

        return new RestClientException(message);
    }
}
