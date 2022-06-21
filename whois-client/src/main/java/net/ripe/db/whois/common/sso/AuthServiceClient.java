package net.ripe.db.whois.common.sso;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.ripe.db.whois.common.sso.domain.MemberContactsResponse;
import net.ripe.db.whois.common.sso.domain.ValidateTokenResponse;
import org.apache.commons.lang.StringUtils;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

@Component
public class AuthServiceClient {

    public static final String TOKEN_KEY = "crowd.token_key";
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServiceClient.class);

    private static final String VALIDATE_PATH = "/authorisation-service/v2/authresource/validate";
    private static final String ORGANISATION_MEMBERS_PATH = "/authorisation-service/v2/authresource/members";
    private static final String USER_SEARCH_PATH = "/authorisation-service/v2/authresource/accounts/";

    private static final String CONTACT_PATH = "/contacts";
    private static final String EMAIL_PATH = "/email";
    private static final String VALIDATE_TOKEN_PERMISSION = "portal";

    private static final int CLIENT_CONNECT_TIMEOUT = 10_000;
    private static final int CLIENT_READ_TIMEOUT = 60_000;

    private final Client client;

    private final String restUrl;
    private final String apiKey;

    @Autowired
    public AuthServiceClient(
            @Value("${authorisation.service.api.url:}") final String restUrl,
            @Value("${authorisation.service.api.key:}") final String apiKey) {
        this.restUrl = restUrl;
        this.apiKey = apiKey;

        final JacksonJsonProvider jsonProvider = (new JacksonJaxbJsonProvider())
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        this.client = (ClientBuilder.newBuilder()
                .register(jsonProvider))
                .property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT)
                .property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT)
                .build();
    }

    @Nullable
    public ValidateTokenResponse validateToken(final String crowdToken) {
        if (StringUtils.isEmpty(crowdToken)) {
            LOGGER.info("No crowdToken was supplied");
            throw new NotAuthorizedException("Invalid token.");
        }

        try {
            return client.target(restUrl)
                    .path(VALIDATE_PATH)
                    .path(crowdToken)
                    .queryParam("permission", VALIDATE_TOKEN_PERMISSION)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-API_KEY", apiKey)
                    .get(ValidateTokenResponse.class);
        } catch (NotFoundException e) {
            LOGGER.debug("Failed to validate token {} due to {}:{}\n\tResponse: {}", crowdToken, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            throw new CrowdClientException(UNAUTHORIZED.getStatusCode(), "Invalid token.");
        } catch (WebApplicationException e) {
            LOGGER.debug("Failed to validate token {} due to {}:{}\n\tResponse: {}", crowdToken, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            throw new CrowdClientException(INTERNAL_SERVER_ERROR.getStatusCode(), "Internal server error");
        } catch (ProcessingException e) {
            LOGGER.debug("Failed to validate token {} due to {}:{}", crowdToken, e.getClass().getName(), e.getMessage());
            throw new CrowdClientException(INTERNAL_SERVER_ERROR.getStatusCode(), "Internal server error");
        }
    }

    public UserSession getUserSession(final String token) {
        ValidateTokenResponse response = validateToken(token);
        return new UserSession(response.response.content.email, response.response.content.getName(), response.response.content.active, null);
    }

    public String getUuid(final String username) {
        if (StringUtils.isEmpty(username)) {
            throw new NotAuthorizedException("Invalid username.");
        }



        try {
            final ValidateTokenResponse response = client.target(restUrl)
                    .path(USER_SEARCH_PATH)
                    .path(EMAIL_PATH)
                    .path(username)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-API_KEY", apiKey)
                    .get(ValidateTokenResponse.class);
            return response.response.content.id;

        } catch (NotFoundException e) {
            LOGGER.debug("Failed to get info {} due to {}:{}\n\tResponse: {}", username, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            throw new CrowdClientException(UNAUTHORIZED.getStatusCode(), "Invalid username.");
        } catch (WebApplicationException e) {
            LOGGER.debug("Failed to get details for email {} due to {}:{}\n\tResponse: {}", username, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            throw new CrowdClientException(INTERNAL_SERVER_ERROR.getStatusCode(), "Internal server error");
        } catch (ProcessingException e) {
            LOGGER.debug("Failed to get details for email {} due to {}:{}", username, e.getClass().getName(), e.getMessage());
            throw new CrowdClientException(INTERNAL_SERVER_ERROR.getStatusCode(), "Internal server error");
        }
    }

    public ValidateTokenResponse getUserDetails(final String uuid) {
        if (StringUtils.isEmpty(uuid)) {
            LOGGER.info("No uuid was supplied");
            throw new NotAuthorizedException("Invalid uuid.");
        }

        try {
            return client.target(restUrl)
                    .path(USER_SEARCH_PATH)
                    .path(uuid)
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .header("X-API_KEY", apiKey)
                    .get(ValidateTokenResponse.class);
        } catch (BadRequestException e) {
            LOGGER.debug("Failed to get details for uuid {} (token is invalid)", uuid);
            throw new NotAuthorizedException("Invalid token.");
        } catch (WebApplicationException e) {
            LOGGER.debug("Failed to get details for uuid {} due to {}:{}\n\tResponse: {}", uuid, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            throw new CrowdClientException(INTERNAL_SERVER_ERROR.getStatusCode(), "Internal server error");
        } catch (ProcessingException e) {
            LOGGER.debug("Failed to get details for uuid {} due to {}:{}", uuid, e.getClass().getName(), e.getMessage());
            throw new CrowdClientException(INTERNAL_SERVER_ERROR.getStatusCode(), "Internal server error");
        }
    }

    public String getUsername(final String uuid) {
        return getUserDetails(uuid).response.content.email;
    }

    public String getDisplayName(final String uuid) {
        return getUserDetails(uuid).response.content.getName();
    }

    public List<String> getOrgsContactsEmails(final Long membershipId) {
        if (membershipId != null) {
            try {
                final MemberContactsResponse response = client.target(restUrl)
                        .path(ORGANISATION_MEMBERS_PATH)
                        .path(String.valueOf(membershipId))
                        .path(CONTACT_PATH)
                        .queryParam("api-key", apiKey) // TODO remove this when this endpoint supports apikey as a header
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .header("X-API_KEY", apiKey)
                        .get(MemberContactsResponse.class);

                return response.response.results.stream()
                        .map(MemberContactsResponse.ContactDetails::getEmail)
                        .collect(Collectors.toList());

            } catch (ForbiddenException e) {
                LOGGER.info("Failed to retrieve additional contact email addresses for an membershipId {} (membershipId is invalid)", membershipId);
            } catch (WebApplicationException e) {
                LOGGER.info("Failed to retrieve additional contact email addresses for an membershipId {} due to {}:{}\n\tResponse: {}", membershipId, e.getClass().getName(), e.getMessage(), e.getResponse().readEntity(String.class));
            } catch (ProcessingException e) {
                LOGGER.info("Failed to retrieve additional contact email addresses for an membershipId {} due to {}:{}", membershipId, e.getClass().getName(), e.getMessage());
            }  catch (Exception e) {
                LOGGER.warn(e.getMessage(), e);
            }
        }

        return Collections.emptyList();
    }
}
