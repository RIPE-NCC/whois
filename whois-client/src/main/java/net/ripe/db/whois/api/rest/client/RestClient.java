package net.ripe.db.whois.api.rest.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

@Component
public class RestClient {

    private Client client;

    @Value("${api.rest.baseurl}")
    private String restApiUrl;

    @Value("${whois.source}")
    private String sourceName;

    @Autowired
    private WhoisObjectMapper whoisObjectMapper;

    // TODO: [ES] use autowired constructor, drop the setters
    // NB: this is also used from dbweb, with multiple environments represented by multiple RestClient beans, managed by AppConfig
    public RestClient() {
        this.client = createClient();
    }

    public RestClient(final String restApiUrl, final String sourceName) {
        this();
        this.restApiUrl = restApiUrl;
        this.sourceName = sourceName;
    }

    public void setWhoisObjectMapper(final WhoisObjectMapper whoisObjectMapper) {
        this.whoisObjectMapper = whoisObjectMapper;
    }

    void setClient(final Client client) {
        this.client = client;
    }

    public RestClientTarget request() {
        return new RestClientTarget(client, restApiUrl, sourceName, whoisObjectMapper);
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
