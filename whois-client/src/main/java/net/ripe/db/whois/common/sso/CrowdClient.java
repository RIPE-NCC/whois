package net.ripe.db.whois.common.sso;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.NoSuchElementException;

// NB: we can't use the atlassian crowd-rest-client as uuid is a ripe-specific crowd plug-in
@Component
public class CrowdClient {
    private static final String CROWD_SESSION_PATH = "rest/usermanagement/1/session";

    private String restUrl;
    private Client client;

    @Autowired
    public CrowdClient(@Value("${crowd.rest.url}") final String translatorUrl,
                       @Value("${crowd.rest.user}") final String crowdAuthUser,
                       @Value("${crowd.rest.password}") final String crowdAuthPassword) {
        this.restUrl = translatorUrl;

        client = ClientBuilder.newBuilder()
                .register(HttpAuthenticationFeature.basic(crowdAuthUser, crowdAuthPassword))
                .build();
    }

    public void setRestUrl(final String url) {
        this.restUrl = url;
    }

    void setClient(final Client client) {
        this.client = client;
    }

    public String login(final String username, final String password) throws CrowdClientException {
        final CrowdAuthenticationContext crowdAuth = new CrowdAuthenticationContext(username, password);

        try {
            final CrowdSession session = client.target(restUrl)
                    .path(CROWD_SESSION_PATH)
                    .request()
                    .post(Entity.entity(crowdAuth, MediaType.APPLICATION_XML), CrowdSession.class);
            return session.getToken();
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    public void logout(final String username) throws CrowdClientException {
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
        try {
            return client.target(restUrl)
                    .path("rest/usermanagement/1/user/attribute")
                    .queryParam("username", username)
                    .request()
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
        try {
            return client.target(restUrl)
                    .path("rest/sso/1/uuid-search")
                    .queryParam("uuid", uuid)
                    .request()
                    .get(CrowdUser.class)
                    .getName();
        } catch (NotFoundException e) {
            throw new CrowdClientException("Unknown RIPE NCC Access uuid: " + uuid);
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    public UserSession getUserSession(final String token) throws CrowdClientException {
        try {
            CrowdUser user = client.target(restUrl)
                    .path(CROWD_SESSION_PATH)
                    .path(token)
                    .request()
                    .get(CrowdSession.class)
                    .getUser();
            return new UserSession(user.getName(), user.getActive());
        } catch (BadRequestException e) {
            throw new CrowdClientException("Unknown RIPE NCC Access token: " + token);
        } catch (WebApplicationException | ProcessingException e) {
            throw new CrowdClientException(e);
        }
    }

    // domain classes

    @XmlRootElement(name = "attributes")
    static class CrowdResponse {
        @XmlElement(name = "attribute")
        private List<CrowdAttribute> attributes;

        public CrowdResponse() {
            // required no-arg constructor
        }

        public CrowdResponse(final List<CrowdAttribute> attributes) {
            this.attributes = attributes;
        }

        public List<CrowdAttribute> getAttributes() {
            return attributes;
        }

        public String getUUID() {
            final CrowdAttribute uuid = Iterables.find(attributes, new Predicate<CrowdAttribute>() {
                @Override
                public boolean apply(final CrowdAttribute input) {
                    return input.getName().equals("uuid");
                }
            });

            return uuid.getValues().get(0).getValue();
        }
    }

    @XmlRootElement
    static class CrowdAttribute {
        @XmlElement
        private List<CrowdValue> values;
        @XmlAttribute(name = "name")
        private String name;

        public CrowdAttribute() {
            // required no-arg constructor
        }

        public CrowdAttribute(final List<CrowdValue> values, final String name) {
            this.values = values;
            this.name = name;
        }

        public List<CrowdValue> getValues() {
            return values;
        }

        public String getName() {
            return name;
        }
    }

    @XmlRootElement
    static class CrowdValue {
        @XmlElement(name = "value")
        private String value;

        public CrowdValue() {
            // required no-arg constructor
        }

        public CrowdValue(final String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @XmlRootElement(name = "user")
    static class CrowdUser {
        @XmlAttribute(name = "name")
        private String name;
        @XmlElement(name = "active")
        private Boolean active;

        public CrowdUser() {
            // required no-arg constructor
        }

        public CrowdUser(final String name, final Boolean active) {
            this.name = name;
            this.active = active;
        }

        public Boolean getActive() {
            return active;
        }

        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "session")
    static class CrowdSession {
        @XmlElement(name = "user")
        private CrowdUser user;
        @XmlElement(name = "token")
        private String token;

        public CrowdSession() {
            // required no-arg constructor
        }

        public CrowdSession(final CrowdUser user, final String token) {
            this.user = user;
            this.token = token;
        }

        public CrowdUser getUser() {
            return user;
        }

        public String getToken() {
            return token;
        }
    }

    @XmlRootElement(name = "authentication-context")
    static class CrowdAuthenticationContext {
        @XmlElement(name = "username")
        private String username;
        @XmlElement(name = "password")
        private String password;

        CrowdAuthenticationContext() {
            // required no-arg constructor
        }

        CrowdAuthenticationContext(final String username, final String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }

    @XmlRootElement(name = "error")
    static class CrowdError {
        @XmlElement(name = "reason")
        private String reason;
        @XmlElement(name = "message")
        private String message;

        public CrowdError() {
            // required no-arg constructor
        }

        public CrowdError(final String reason, final String message) {
            this.reason = reason;
            this.message = message;
        }

        public String getReason() {
            return reason;
        }

        public String getMessage() {
            return message;
        }
    }
}
