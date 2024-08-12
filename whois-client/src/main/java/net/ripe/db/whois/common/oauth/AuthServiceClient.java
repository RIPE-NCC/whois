package net.ripe.db.whois.common.oauth;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.oauth.condition.Oauth2Condition;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import net.ripe.db.whois.common.sso.AuthServiceClientException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Objects;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

@Component
@Conditional(Oauth2Condition.class)
public class AuthServiceClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceClient.class);

    private static final String API_KEYS_PATH = "/api/api-keys";

    private final WebClient apiKeyGatewayWebClient;


    @Autowired
    public AuthServiceClient(final WebClient apiKeyGatewayWebClient) {
        this.apiKeyGatewayWebClient = apiKeyGatewayWebClient;

    }

    public List<ApiKey> validateJwtToken(final String jwtToken){
        if (StringUtils.isEmpty(jwtToken)) {
            LOGGER.debug("No ApiKey was supplied");
            throw new AuthServiceClientException(BAD_REQUEST.getStatusCode(), "No ApiKey supplied.");
        }

        List<ApiKey> apiKeys;

        try {
            ResponseEntity<List<ApiKey>> apiKeysResponse = apiKeyGatewayWebClient
                    .get()
                    .uri(API_KEYS_PATH)
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<List<ApiKey>>() {
                    })
                    .block();
            apiKeys = Objects.requireNonNull(apiKeysResponse).getBody();
        } catch (Exception exc) {
            LOGGER.error("Fetching API keys failed.", exc);
            apiKeys = null;
        }

        return apiKeys;

        /*HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List<ApiKey>> response = restTemplate.exchange(
                restUrl,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<List<ApiKey>>() {}
        );*/
    }
}
