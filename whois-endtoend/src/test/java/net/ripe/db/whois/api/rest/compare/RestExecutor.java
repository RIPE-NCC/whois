package net.ripe.db.whois.api.rest.compare;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.query.endtoend.compare.ComparisonExecutor;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

public class RestExecutor implements ComparisonExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestExecutor.class);

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

        return Collections.<ResponseObject>singletonList(
                new StringResponseObject(parseResponse(props, response)));
    }

    private String parseResponse(final RestQueryProperties props, final String response) {

        final String parsedResponse = response.replaceAll("://.+?/", "://server/");

        if (configuration.getResponseFormat() == RestExecutorConfiguration.ResponseFormat.DEFAULT) {
            return parsedResponse;
        }

        return props.getMediaType() == MediaType.APPLICATION_JSON_TYPE ?
                compactJson(parsedResponse) : compactXmlNaive(parsedResponse);
    }

    public static String compactXmlNaive(final String response) {
        final BufferedReader br = new BufferedReader(new StringReader(response));
        final StringBuffer sb = new StringBuffer();
        String line;

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line.trim());
            }
            return sb.toString();
        } catch (IOException e) {
            LOGGER.debug("Could not process XML response.", e);
            return response;
        }
    }

    public static String compactJson(final String response) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBytes(), JsonNode.class).toString();
        } catch (IOException e) {
            LOGGER.debug("Could not process JSON response.", e);
            return response;
        }
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

        private static final WebTarget target(final String host, final int port, final String path) {
            return client.target(String.format("http://%s:%d/%s", host, port, path));
        }
    }

}
