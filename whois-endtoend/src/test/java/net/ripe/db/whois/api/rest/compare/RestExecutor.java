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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
            System.out.println("url = " + String.format("http://%s:%d/%s", configuration.getHost(), props.getPortFromConfiguration(configuration), query));
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

    private String parseResponse(RestQueryProperties props, String response) {

        String parsedResponse = response.replaceAll("://.+?/", "://server/");

        if (configuration.getResponseFormat() == RestExecutorConfiguration.ResponseFormat.DEFAULT) {
            return parsedResponse;
        }

        if (props.getMediaType() == MediaType.APPLICATION_JSON_TYPE){
            return compactJson(parsedResponse);
        } else {
            return compactXmlNaive(parsedResponse);
        }
    }

    public static String compactXml(String response){
        //TODO [TP] : Remove this method if the compactXmlNaive does the job
        // Attention: this approach changes the elements from <element><element/> to <element />
        // It also does not work consistently.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        TransformerFactory tf = TransformerFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(response)));

            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString() ;
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            LOGGER.debug("Could not process XML response.", e);
            return response;
        }

    }

    public static String compactXmlNaive(String response) {
        BufferedReader br = new BufferedReader(new StringReader(response));
        String line;
        StringBuffer sb = new StringBuffer();
        try {
            while((line=br.readLine())!= null){
                sb.append(line.trim());
            }
        return sb.toString();
        } catch (IOException e) {
            LOGGER.debug("Could not process XML response.", e);
            return response;
        }
    }


    public static String compactJson(String response){
        try {
            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
            JsonNode json = mapper.readValue(response.getBytes(), JsonNode.class);
            return json.toString();
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
