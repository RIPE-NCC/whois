package net.ripe.db.whois.api.apiKey;

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
import com.google.common.collect.Lists;
import com.nimbusds.jose.util.JSONObjectUtils;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class ApiPublicKeyLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiPublicKeyLoader.class);

    private static final int CLIENT_CONNECT_TIMEOUT = 10_000;
    private static final int CLIENT_READ_TIMEOUT = 60_000;

    private final Client client;
    private final String restUrl;

    @Autowired
    public ApiPublicKeyLoader(
            @Value("${api.public.key.url:}")  final String restUrl) {
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

    @Cacheable(cacheNames = "JWTpublicKeyDetails")
    public List<RSAKey> loadPublicKey() throws ParseException {
        if(StringUtils.isEmpty(restUrl)) {
            LOGGER.warn("Skipping JWT verification as url is null");
            return Collections.emptyList();
        }

        LOGGER.debug("Loading public key from {}", restUrl);
        try {
            return  getListOfKeys(client.target(restUrl)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class));

        } catch (Exception e) {
            LOGGER.error("Failed to load RSA public key  apikey due to {}:{}", e.getClass().getName(), e.getMessage());
            throw e;
        }
    }

    protected List<RSAKey> getListOfKeys(final String publicKeys) throws ParseException {
        try {
            final Map<String, Object> content = JSONObjectUtils.parse(publicKeys);
            final List<RSAKey> rsaKeys = Lists.newArrayList();

            for (final Map<String, Object> key : JSONObjectUtils.getJSONObjectArray(content, "keys")) {
                rsaKeys.add(RSAKey.parse(key));
            }

            return rsaKeys;
        } catch ( Exception e ) {
            LOGGER.error("Failed to parse public key", e);
            throw e;
        }
    }
}
