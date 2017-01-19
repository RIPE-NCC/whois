package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import net.ripe.db.whois.update.dns.zonemaster.domain.ZonemasterRequest;
import org.glassfish.jersey.client.ClientProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Component
public class ZonemasterRestClient {

    private Client client;
    private String baseUrl;

    @Autowired
    public ZonemasterRestClient(@Value("${whois.zonemaster.baseUrl}") final String baseUrl) {
        this.baseUrl = baseUrl;
        this.client = createClient();
    }

    public Response sendRequest(final ZonemasterRequest request) {
        return client
            .target(baseUrl)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(request, MediaType.APPLICATION_JSON));
    }

    private static Client createClient() {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        jsonProvider.configure(SerializationFeature.INDENT_OUTPUT, true);
        jsonProvider.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);
        jsonProvider.locateMapper(ZonemasterRequest.class, MediaType.APPLICATION_JSON_TYPE).setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return ClientBuilder.newBuilder()
                .register(jsonProvider)
                .property(ClientProperties.CONNECT_TIMEOUT, 10 * 1_000)
                .property(ClientProperties.READ_TIMEOUT,    10 * 1_000)
                .build();
    }
}
