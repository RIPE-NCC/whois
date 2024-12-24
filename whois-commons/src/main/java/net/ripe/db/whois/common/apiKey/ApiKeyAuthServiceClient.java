package net.ripe.db.whois.common.apiKey;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import net.ripe.db.whois.common.sso.UserSession;
import net.ripe.db.whois.common.sso.domain.HistoricalUserResponse;
import net.ripe.db.whois.common.sso.domain.MemberContactsResponse;
import net.ripe.db.whois.common.sso.domain.ValidateTokenResponse;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static jakarta.ws.rs.core.Response.Status.UNAUTHORIZED;

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

        final JacksonJsonProvider jsonProvider = (new JacksonJsonProvider())
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        this.client = (ClientBuilder.newBuilder()
                .register(jsonProvider))
                .property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT)
                .property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT)
                .build();
    }

    public String validateApiKey(final String basicHeader) {
        final String accessKey = ApiKeyUtils.getAccessKey(basicHeader);
        try {

            final String response =  client.target(restUrl)
                    .path(VALIDATE_PATH)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header(HttpHeaders.AUTHORIZATION, basicHeader)
                    .get(String.class);

            return getOAuthSession(response, accessKey);
        } catch (NotFoundException | NotAuthorizedException e) {
            LOGGER.debug("Failed to validate apikey {} due to {}:{}\n\tResponse: {}", accessKey, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            return getFailedOAuth(accessKey);
        } catch (Exception e) {
            LOGGER.debug("Failed to validate token {} due to {}:{}", accessKey, e.getClass().getName(), e.getMessage());
            return getFailedOAuth(accessKey);
        }
    }

    //TODO: ADD AccessKey
    private String getFailedOAuth(String accessKey) {
        return ApiKeyUtils.getOAuthSession(new OAuthSession(accessKey));
    }

    //TODO Add access Key
    private String getOAuthSession(final String response, final String accessKey) {
        final String payload =  new String(Base64.getUrlDecoder().decode(response.split("\\.")[1]));

        //TODO: remove when accessKey is available from api registry call
        return ApiKeyUtils.getOAuthSession(OAuthSession.from(ApiKeyUtils.getOAuthSession(payload), accessKey));
    }
}
