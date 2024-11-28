package net.ripe.db.nrtm4.client.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;
import com.google.common.net.HttpHeaders;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ripe.db.nrtm4.client.condition.Nrtm4ClientCondition;
import org.apache.commons.compress.utils.Lists;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpScheme;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Conditional(Nrtm4ClientCondition.class)
public class NrtmRestClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(NrtmRestClient.class);

    private final String baseUrl;

    private static final int CLIENT_CONNECT_TIMEOUT = 10_000;

    private static final int CLIENT_READ_TIMEOUT = 60_000;

    private final Client client;

    public NrtmRestClient(@Value("${nrtm.baseUrl}") final String baseUrl) {
        final ObjectMapper objectMapper = JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();
        objectMapper.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(
                        new JacksonAnnotationIntrospector(),
                        new JakartaXmlBindAnnotationIntrospector(TypeFactory.defaultInstance())));
        objectMapper.registerModule(new JavaTimeModule());
        final JacksonJsonProvider jsonProvider = (new JacksonJsonProvider())
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        jsonProvider.setMapper(objectMapper);

        this.client = (ClientBuilder.newBuilder()
                .register(jsonProvider))
                .property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT)
                .property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT)
                .build();
        this.baseUrl = baseUrl;
    }

    public List<String> getNrtmAvailableSources(){
        try {
            final String response = client.target(baseUrl)
                    .request(MediaType.TEXT_HTML_TYPE)
                    .get(String.class);

            return extractSources(response);
        } catch (final Exception e) {
            LOGGER.error("Unable to get the available sources", e);
            return Lists.newArrayList();
        }
    }

    public String getNotificationFileSignature(final String source){
        return client.target(String.format("%s/%s", baseUrl, source))
                .path("update-notification-file.jose")
                .request()
                .header(HttpHeaders.CONTENT_TYPE, "application/jose+json")
                .get(String.class);
    }

    @Nullable
    public byte[] getSnapshotFile(final String url){
        try {
            final Response response = client.target(baseUrl)
                    .path(url)
                    .request(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeader.X_FORWARDED_PROTO.asString(), HttpScheme.HTTPS.asString())
                    .get(Response.class);

            return response.readEntity(byte[].class);
        } catch (Exception ex){
            LOGGER.error("Unable to get the records from the snapshot", ex);
            return null;
        }
    }

    @Nullable
    public byte[] getDeltaFiles(final String url){
        try {
            final Response response = client.target(baseUrl)
                    .path(url)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Response.class);

            return response.readEntity(byte[].class);
        } catch (Exception ex){
            LOGGER.error("Unable to get the records from the snapshot", ex);
            return null;
        }
    }

    private static List<String> extractSources(final String html) {
        final List<String> sources = com.google.common.collect.Lists.newArrayList();

        final String regex = "<a[^>]*>(.*?)</a>";
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            final String source = matcher.group(1).trim();
            sources.add(source);
        }

        return sources;
    }
}
