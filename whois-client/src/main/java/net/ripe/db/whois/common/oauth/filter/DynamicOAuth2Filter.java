package net.ripe.db.whois.common.oauth.filter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.endpoint.DefaultClientCredentialsTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2ClientCredentialsGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


// We need to define ClientRegistration. This holds information such as client ID, client secret, authorization URI,
// token URI, and scopes. This is per request, because each user as his own grants and identification

// THis configuration is crucial because it defines how the application interacts with the OAuth2 provider, including
// where to send the authorisation request, how to obtain tokens...etc
//THis is used for retrieving an access token from the OAuth2 provider. Then we put this token as a bearer token
public class DynamicOAuth2Filter extends OncePerRequestFilter {

    private final InMemoryClientRegistrationRepository clientRegistrationRepository;

    private final OAuth2AuthorizedClientManager oauth2AuthorizedClientManager;

    private final String keyClockApi;

    //private final ObjectMapper objectMapper;
    //private final Client client;

    private static final DefaultClientCredentialsTokenResponseClient CLIENT_CREDENTIALS_TOKEN_RESPONSE_CLIENT =
            new DefaultClientCredentialsTokenResponseClient();

    public DynamicOAuth2Filter(final String keyClockApi,
                               final OAuth2AuthorizedClientManager oauth2AuthorizedClientManager, final InMemoryClientRegistrationRepository clientRegistrationRepository) {
        this.oauth2AuthorizedClientManager = oauth2AuthorizedClientManager;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.keyClockApi = keyClockApi;
        //this.client = client;
        //this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!(request.getUserPrincipal() instanceof UsernamePasswordAuthenticationToken)){
            filterChain.doFilter(request, response);
        }

        final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken)request.getUserPrincipal();
        String username = extractAuthorityValues(usernamePasswordAuthenticationToken.getAuthorities(), AuthorityPrefix.USER)
                .findFirst()
                .orElseThrow();

        ClientRegistration originalOAuth2ClientRegistration = clientRegistrationRepository.iterator().next();

        OAuth2AuthorizeRequest oauth2AuthorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(originalOAuth2ClientRegistration.getRegistrationId())
                .principal("api-key-gateway")
                .build();
        OAuth2AuthorizedClient oauth2AuthorizedClient = oauth2AuthorizedClientManager.authorize(oauth2AuthorizeRequest);

        WebClient webClient = WebClient.create();
        ResponseEntity<List<KeycloakUserRepresentation>> keycloakUsersResponse = webClient
                .get()
                .uri(buildSafeUri(keyClockApi, username))
                .headers(headers -> headers.setBearerAuth(Objects.requireNonNull(oauth2AuthorizedClient).getAccessToken().getTokenValue()))
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<KeycloakUserRepresentation>>() {})
                .block();

        /*List<KeycloakUserRepresentation> keycloakUsersResponse = client.target(keyClockApi)
                .queryParam("username", username)
                .queryParam("exact", true)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("", Objects.requireNonNull(oauth2AuthorizedClient).getAccessToken().getTokenValue())
                .get()
                .readEntity(new GenericType<>() {});*/

        KeycloakUserRepresentation keycloakUserRepresentation = Objects.requireNonNull(Objects.requireNonNull(keycloakUsersResponse).getBody()).getFirst();
        if (!keycloakUserRepresentation.enabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String userId = keycloakUserRepresentation.id();

        String application = extractAuthorityValues(usernamePasswordAuthenticationToken.getAuthorities(), AuthorityPrefix.APP)
                .findFirst()
                .orElseThrow();

        Set<String> scope = extractAuthorityValues(usernamePasswordAuthenticationToken.getAuthorities(), AuthorityPrefix.SCOPE)
                .collect(Collectors.toSet());

        // this token to be exchanged has to have the same scope as the one requested below
        ClientRegistration customOAuth2ClientRegistration = ClientRegistration
                .withClientRegistration(originalOAuth2ClientRegistration)
                .scope(scope)
                .build();

        OAuth2ClientCredentialsGrantRequest clientCredentialsGrantRequest = new OAuth2ClientCredentialsGrantRequest(customOAuth2ClientRegistration);

        OAuth2AccessTokenResponse clientCredentialsGrantResponse =
                CLIENT_CREDENTIALS_TOKEN_RESPONSE_CLIENT.getTokenResponse(clientCredentialsGrantRequest);

        /*JsonNode requestBody = buildRequestBody(
                clientCredentialsGrantResponse, originalOAuth2ClientRegistration, userId, application, scope
        );*/

        JsonNode tokenExchangeResponse = webClient
                .post()
                .uri(customOAuth2ClientRegistration.getProviderDetails().getTokenUri())
                .body(BodyInserters
                        .fromFormData("grant_type", AuthorizationGrantType.TOKEN_EXCHANGE.getValue())
                        .with("requested_token_type", "urn:ietf:params:oauth:token-type:access_token")
                        .with("subject_token", clientCredentialsGrantResponse.getAccessToken().getTokenValue())
                        .with("client_id", originalOAuth2ClientRegistration.getClientId())
                        .with("client_secret", originalOAuth2ClientRegistration.getClientSecret())
                        .with("requested_subject", userId)
                        .with("audience", application)
                        .with("scope", String.join(" ", scope)))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        /*client.target(customOAuth2ClientRegistration.getProviderDetails().getTokenUri())
        .request()
        .post(Entity.json(requestBody));

        JsonNode jsonNode = objectMapper.readTree(tokenExchangeResponse.readEntity(String.class));*/


        CustomHttpServletRequestHeadersWrapper modifiedServerRequest = new CustomHttpServletRequestHeadersWrapper(request)
                .addHeader("Authorisation", Objects.requireNonNull(tokenExchangeResponse).get("access_token").asText());

        filterChain.doFilter(modifiedServerRequest, response);
    }

    //Create safe uri to avoid CVE-2024-22243 vulnerability that avoids introducing vulnerabilities
    private URI buildSafeUri(String keyClockApi, String username) {
        // Encode the username to ensure it's safely included in the URI
        String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8);

        // Build the URI with encoded parameters
        URI uri = UriComponentsBuilder.fromHttpUrl(keyClockApi)
                .queryParam("username", encodedUsername)
                .queryParam("exact", true)
                .build()
                .encode() // Ensures the entire URI is safely encoded
                .toUri();

        return uri;
    }

    /*public JsonNode buildRequestBody(OAuth2AccessTokenResponse clientCredentialsGrantResponse,
                                     ClientRegistration originalOAuth2ClientRegistration,
                                     String userId, String application, Set<String> scope) {
        // Create an ObjectNode to represent the JSON object
        ObjectNode requestBody = objectMapper.createObjectNode();

        requestBody.put("grant_type", AuthorizationGrantType.TOKEN_EXCHANGE.getValue());
        requestBody.put("requested_token_type", "urn:ietf:params:oauth:token-type:access_token");
        requestBody.put("subject_token", clientCredentialsGrantResponse.getAccessToken().getTokenValue());
        requestBody.put("client_id", originalOAuth2ClientRegistration.getClientId());
        requestBody.put("client_secret", originalOAuth2ClientRegistration.getClientSecret());
        requestBody.put("requested_subject", userId);
        requestBody.put("audience", application);
        requestBody.put("scope", String.join(" ", scope));

        return requestBody;
    }*/


    private static Stream<String> extractAuthorityValues(Collection<GrantedAuthority> authorities, AuthorityPrefix authorityPrefix) {
        return authorities
                .stream()
                .filter(grantedAuthority -> grantedAuthority.getAuthority().startsWith(authorityPrefix.getValue()))
                .map(grantedAuthority -> grantedAuthority.getAuthority().split(authorityPrefix.getValue())[1]);
    }

    private static final class CustomHttpServletRequestHeadersWrapper extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders;

        private CustomHttpServletRequestHeadersWrapper(final HttpServletRequest request) {
            super(request);
            this.customHeaders = new HashMap<>();
        }

        public CustomHttpServletRequestHeadersWrapper addHeader(String name, String value) {
            this.customHeaders.put(name, value);
            return this;
        }

        @Override
        public String getHeader(String name) {
            // Check the custom headers first
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            // Otherwise fall back to the original wrapped request
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (customHeaders.containsKey(name)) {
                return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            // Create a set with all the header names
            Map<String, String> allHeaders = new HashMap<>(customHeaders);
            Enumeration<String> originalHeaderNames = super.getHeaderNames();
            while (originalHeaderNames.hasMoreElements()) {
                String headerName = originalHeaderNames.nextElement();
                allHeaders.putIfAbsent(headerName, super.getHeader(headerName));
            }
            return Collections.enumeration(allHeaders.keySet());
        }
    }
}
