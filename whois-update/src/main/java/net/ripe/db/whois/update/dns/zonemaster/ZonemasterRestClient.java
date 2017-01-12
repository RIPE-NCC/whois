package net.ripe.db.whois.update.dns.zonemaster;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


public class ZonemasterRestClient {

    ZonemasterRestClient() {
        target = createClient().target("http://zonemaster-test.ripe.net:5000");
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

    public static void main(final String[] args) {
        ZonemasterRequest req1 = new VersionInfoRequest();
        System.out.println(">>> VersionInfoResponse: " + req1.execute().readEntity(VersionInfoResponse.class));
        ZonemasterRequest req2 = new GetTestResultsRequest("974995af7d7b59c9");
        System.out.println(">>> GetTestResultsResponse: " + req2.execute().readEntity(GetTestResultsResponse.class));
    }

    private final WebTarget target;

}
