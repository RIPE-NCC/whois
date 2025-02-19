package net.ripe.db.whois.common.apiKey;

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
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.common.aspects.Stopwatch;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component
public class ApiKeyAuthServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiKeyAuthServiceClient.class);

    private static final String VALIDATE_PATH = "/api/v1/api-keys/authenticate";

    private static final int CLIENT_CONNECT_TIMEOUT = 10_000;
    private static final int CLIENT_READ_TIMEOUT = 60_000;

    private final Client client;
    private final String restUrl;

    @Autowired
    public ApiKeyAuthServiceClient(
            @Value("${apiKey.key.registry:}")  final String restUrl) {
        this.restUrl = restUrl;

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
    }

    @Stopwatch(thresholdMs = 100L)
    @Cacheable(cacheNames="apiKeyOAuth")
    @Nullable
    public String validateApiKey(final String basicHeader,  final String apiKeyId) {
        try {
            return  client.target(restUrl)
                    .path(VALIDATE_PATH)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, basicHeader)
                    .get(String.class);

        } catch (NotFoundException | NotAuthorizedException e) {
            LOGGER.debug("Failed to validate api key {} due to {}:{}\n\tResponse: {}", apiKeyId, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            return null;
        } catch (Exception e) {
            LOGGER.error("Failed to validate api key {} due to {}:{}", apiKeyId, e.getClass().getName(), e.getMessage());
            return null;
        }
    }
}
