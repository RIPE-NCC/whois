package net.ripe.db.whois.api.rest.compare;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.endtoend.compare.ComparisonExecutor;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class RestExecutor implements ComparisonExecutor {
    private final RestExecutorConfiguration configuration;

    public RestExecutor(final RestExecutorConfiguration configuration) throws UnknownHostException {
        this.configuration = configuration;
    }

    @Override
    public List<ResponseObject> getResponse(final String query) throws IOException {
        final RestQueryProperties props = new RestQueryProperties(query);
        String response;
        final Stopwatch stopWatch = Stopwatch.createStarted();
        try {
            response = RestCaller.target(configuration.getHost(), props.getPortFromConfiguration(configuration), query)
                    .request(props.getMediaType())
                    .get(String.class);
        } catch (ClientErrorException e) {
            response = e.getResponse().readEntity(String.class);
        } finally {
            stopWatch.stop();
        }
        return parseWhoisResponseIntoRpslObjects(query, props, response);
    }


    private List<ResponseObject> parseWhoisResponseIntoRpslObjects(final String query, RestQueryProperties props, String response) throws IOException {
//        if (props.getQueryType() == QueryType.LOOKUP) {
            ResponseObject obj = new StringResponseObject(response);
            return Collections.singletonList(obj);
//        }
    }


    static private class RestCaller {
        private static final Client client;

        static {
            final JacksonJaxbJsonProvider jsonProvider = new JacksonJaxbJsonProvider();
            jsonProvider.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false);
            jsonProvider.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            client = ClientBuilder.newBuilder()
                    .register(MultiPartFeature.class)
                    .register(jsonProvider)
                    .build();
        }

        public static final WebTarget target(final String host, final int port, final String path) {
            return client.target(String.format("http://%s:%d/%s", host, port, path));
        }
    }

}
