package net.ripe.db.whois.common.sso;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Component
public class CrowdClient {
    private String restUrl;
    private Client client;

    @Autowired
    public CrowdClient(@Value("${rest.crowd.url}") final String translatorUrl,
                       @Value("${rest.crowd.user}") final String crowdAuthUser,
                       @Value("${rest.crowd.password}") final String crowdAuthPassword) {
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

    public String getUuid(final String username) {
        try {
            return client.target(restUrl)
                    .path("rest/usermanagement/latest/user/attribute")
                    .queryParam("username", username)
                    .request()
                    .get(CrowdResponse.class)
                    .getUUID();
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Unknown RIPE Access user: " + username);
        }
    }

    public String getUsername(final String uuid) {
        try {
            return client.target(restUrl)
                    .path("rest/sso/latest/uuid-search")
                    .queryParam("uuid", uuid)
                    .request()
                    .get(CrowdUser.class)
                    .getName();
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Unknown RIPE Access uuid: " + uuid);
        }
    }

    public UserSession getUserSession(final String token) {
        try {
            CrowdUser user = client.target(restUrl)
                    .path("rest/usermanagement/latest/session")
                    .path(token)
                    .request()
                    .get(CrowdSession.class)
                    .getUser();
            return new UserSession(user.getName(), user.getActive());
        } catch (BadRequestException e) {
            throw new IllegalArgumentException("Unknown RIPE Access token: " + token);
        }
    }

    @XmlRootElement(name = "attributes")
    static class CrowdResponse {
        @XmlElement(name = "attribute")
        private List<CrowdAttribute> attributes;

        public CrowdResponse() {}

        public CrowdResponse(List<CrowdAttribute> attributes) {
            this.attributes = attributes;
        }

        public List<CrowdAttribute> getAttributes() {
            return attributes;
        }

        public String getUUID() {
            final CrowdAttribute attributeElement = Iterables.find(attributes, new Predicate<CrowdAttribute>() {
                @Override
                public boolean apply(final CrowdAttribute input) {
                    return input.getName().equals("uuid");
                }
            });

            return attributeElement.getValues().get(0).getValue();
        }
    }

    @XmlRootElement
    static class CrowdAttribute {
        @XmlElement
        private List<CrowdValue> values;
        @XmlAttribute(name="name")
        private String name;

        public CrowdAttribute() {}

        public CrowdAttribute(List<CrowdValue> values, String name) {
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
        @XmlElement(name="value")
        private String value;

        public CrowdValue() {}

        public CrowdValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @XmlRootElement(name = "user")
    static class CrowdUser {
        @XmlAttribute(name="name")
        private String name;
        @XmlElement(name="active")
        private Boolean active;

        public CrowdUser() {}

        public CrowdUser(String name, Boolean active) {
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
        @XmlElement(name="user")
        private CrowdUser user;

        public CrowdSession() {}

        public CrowdSession(CrowdUser user) {
            this.user = user;
        }

        public CrowdUser getUser() {
            return user;
        }
    }

    @XmlRootElement(name = "error")
    static class CrowdError {
        @XmlElement(name="reason")
        private String reason;
        @XmlElement(name="message")
        private String message;

        public CrowdError() {}

        public CrowdError(String reason, String message) {
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
