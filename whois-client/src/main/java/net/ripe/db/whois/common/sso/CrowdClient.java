package net.ripe.db.whois.common.sso;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.NoSuchElementException;

@Component
public class CrowdClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrowdClient.class);

    private static final String CROWD_SESSION_PATH = "rest/usermanagement/1/session";
    private static final String CROWD_USER_ATTRIBUTE_PATH = "rest/usermanagement/1/user/attribute";
    private static final String CROWD_UUID_SEARCH_PATH = "rest/sso/1/uuid-search";

    private static final int CLIENT_CONNECT_TIMEOUT = 10_000;
    private static final int CLIENT_READ_TIMEOUT = 10_000;

    private String restUrl;
    private Client client;

    @Autowired
    public CrowdClient(@Value("${crowd.rest.url}") final String translatorUrl,
                       @Value("${crowd.rest.user}") final String crowdAuthUser,
                       @Value("${crowd.rest.password}") final String crowdAuthPassword) {
        this.restUrl = translatorUrl;

        final JacksonJsonProvider jsonProvider = new JacksonJaxbJsonProvider()
                .configure(DeserializationFeature.UNWRAP_ROOT_VALUE, false)
                .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        client = ClientBuilder.newBuilder()
                .register(HttpAuthenticationFeature.basic(crowdAuthUser, crowdAuthPassword))
                .register(JacksonFeature.class)         // use Jackson
                .register(jsonProvider)
                .build();
        client.property(ClientProperties.CONNECT_TIMEOUT, CLIENT_CONNECT_TIMEOUT);
        client.property(ClientProperties.READ_TIMEOUT, CLIENT_READ_TIMEOUT);
    }

    public String login(final String username, final String password) throws CrowdClientException {
        LOGGER.info("login");

        final CrowdAuthenticationContext crowdAuth = new CrowdAuthenticationContext(username, password);

        try {
            final CrowdSession session = client.target(restUrl)
                    .path(CROWD_SESSION_PATH)
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.entity(crowdAuth, MediaType.APPLICATION_XML), CrowdSession.class);
            return session.getToken();
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    public void logout(final String username) throws CrowdClientException {
        LOGGER.info("logout");

        try {
            client.target(restUrl)
                    .path(CROWD_SESSION_PATH)
                    .queryParam("username", username)
                    .request()
                    .delete();
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    public void invalidateToken(final String token) throws CrowdClientException {
        LOGGER.info("invalidateToken");


        try {
            client.target(restUrl)
                    .path(CROWD_SESSION_PATH)
                    .path(token)
                    .request()
                    .delete();
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    public String getUuid(final String username) throws CrowdClientException {
        LOGGER.info("getUuid");
        try {
            return client.target(restUrl)
                    .path(CROWD_USER_ATTRIBUTE_PATH)
                    .queryParam("username", username)
                    .request(MediaType.APPLICATION_XML)
                    .get(CrowdResponse.class)
                    .getUUID();
        } catch (NoSuchElementException e) {
            throw new CrowdClientException("Cannot find UUID for: " + username);
        } catch (NotFoundException e) {
            throw new CrowdClientException("Unknown RIPE NCC Access user: " + username);
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    public String getUsername(final String uuid) throws CrowdClientException {
        LOGGER.info("getUsername");
        try {
            return client.target(restUrl)
                    .path(CROWD_UUID_SEARCH_PATH)
                    .queryParam("uuid", uuid)
                    .request(MediaType.APPLICATION_XML)
                    .get(CrowdUser.class)
                    .getName();
        } catch (NotFoundException e) {
            throw new CrowdClientException("Unknown RIPE NCC Access uuid: " + uuid);
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    public UserSession getUserSession(final String token) throws CrowdClientException {
        LOGGER.info("getUserSession");
        try {

            final CrowdSession crowdSession = client.target(restUrl)
                    .path(CROWD_SESSION_PATH)
                    .path(token)
                    .queryParam("validate-password", "false")
                    .queryParam("expand", "user")
                    .request(MediaType.APPLICATION_XML)
                    .post(Entity.xml("<?xml version=\"1.0\" encoding=\"UTF-8\"?><validation-factors/>"), CrowdSession.class);
            final CrowdUser user = crowdSession.getUser();
            return new UserSession(user.getName(), user.getDisplayName(), user.getActive(), crowdSession.getExpiryDate());
        } catch (BadRequestException e) {
            throw new CrowdClientException("Unknown RIPE NCC Access token: " + token);
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }
}
