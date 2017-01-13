package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Component
public class ZonemasterRestClient {

    @Autowired
    public ZonemasterRestClient(@Value("${whois.zonemaster.baseUrl}") final String baseUrl) {
        target = createClient().target(baseUrl);
    }

    Response sendRequest(final ZonemasterRequest request) {
        return target.request(MediaType.APPLICATION_JSON_TYPE).post(Entity.entity(request.json(), MediaType.APPLICATION_JSON));
    }

    private static Client createClient() {
        final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
        jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
        jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        return ClientBuilder.newBuilder()
                .register(jsonProvider)
                .build();
    }

    private final WebTarget target;

}
