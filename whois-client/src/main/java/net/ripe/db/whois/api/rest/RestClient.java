package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectClientMapper;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Component
public class RestClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestClient.class);

    private Client client;
    private String restApiUrl;
    private String sourceName;
    private WhoisObjectClientMapper whoisObjectClientMapper;

    public RestClient() {
        this.client = createClient();
    }

    @Autowired
    public RestClient(@Value("${api.rest.baseurl}") String restApiUrl, @Value("${whois.source}") String sourceName) {
        setRestApiUrl(restApiUrl);
        setSource(sourceName);
    }

    public void setRestApiUrl(final String restApiUrl) {
        this.restApiUrl = restApiUrl;
        this.whoisObjectClientMapper = new WhoisObjectClientMapper(restApiUrl);
    }

    public void setSource(final String sourceName) {
        this.sourceName = sourceName;
    }

    void setClient(final Client client) {
        this.client = client;
    }

    public RestClientTarget request() {
        return new RestClientTarget(client, restApiUrl, sourceName, whoisObjectClientMapper);
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

}
