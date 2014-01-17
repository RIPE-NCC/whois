package net.ripe.db.whois.common.sso;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.glassfish.jersey.client.filter.HttpBasicAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.ByteArrayInputStream;
import java.util.List;

@Component
public class CrowdClient {
    private String restUrl;
    private final Client client;
    private final Unmarshaller unmarshaller;

    @Autowired
    public CrowdClient(@Value("${rest.crowd.url}") final String translatorUrl,
                       @Value("${rest.crowd.user}") final String crowdAuthUser,
                       @Value("${rest.crowd.password}") final String crowdAuthPassword) {
        this.restUrl = translatorUrl;
        client = ClientBuilder.newBuilder().register(new HttpBasicAuthFilter(crowdAuthUser, crowdAuthPassword)).build();

        try {
            unmarshaller = JAXBContext.newInstance(CrowdResponse.class, CrowdUser.class, CrowdSession.class, CrowdError.class).createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setRestUrl(final String url) {
        this.restUrl = url;
    }

    public String getUuid(final String username) {
        final String url = String.format(
                "%s/rest/usermanagement/latest/user/attribute?username=%s",
                restUrl,
                username);

        String response;
        try {
            response = client.target(url).request().get(String.class);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Unknown RIPE Access user: " + username);
        }

        return ((CrowdResponse)extractResponse(response)).getUUID();
    }

    public String getUsername(final String uuid) {
        final String url = String.format(
                "%s/rest/sso/latest/uuid-search?uuid=%s",
                restUrl,
                uuid);

        String response;
        try {
            response = client.target(url).request().get(String.class);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Unknown RIPE Access uuid: " + uuid);
        }

        return ((CrowdUser)extractResponse(response)).getName();
    }

    public UserSession getUserSession(final String token) {
        final String url = String.format(
                "%s/rest/usermanagement/latest/session/%s",
                restUrl,
                token);

        String response;
        try {
            response = client.target(url).request().get(String.class);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Unknown RIPE Access token: " + token);
        }

        final Object object = extractResponse(response);
        if (object instanceof CrowdSession) {
            CrowdUser user = ((CrowdSession) object).getUser();
            return new UserSession(user.getName(), user.getActive());
        }
        else {
            throw new IllegalStateException(((CrowdError) object).getMessage());
        }
    }

    private Object extractResponse(final String response) {
        try {
            return unmarshaller.unmarshal(new ByteArrayInputStream(response.getBytes()));
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }


    @XmlRootElement(name = "attributes")
    private static class CrowdResponse {
        @XmlElement(name = "attribute")
        private List<CrowdAttribute> attributes;

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
    private static class CrowdAttribute {
        @XmlElement
        private List<CrowdValue> values;

        @XmlAttribute(name="name")
        private String name;

        public List<CrowdValue> getValues() {
            return values;
        }

        public String getName() {
            return name;
        }
    }

    @XmlRootElement
    private static class CrowdValue {
        @XmlElement(name="value")
        private String value;

        public String getValue() {
            return value;
        }
    }

    @XmlRootElement(name = "user")
    private static class CrowdUser {
        @XmlAttribute(name="name")
        private String name;

        @XmlElement(name="active")
        private Boolean active;

        public Boolean getActive() {
            return active;
        }

        public String getName() {
            return name;
        }
    }

    @XmlRootElement(name = "session")
    private static class CrowdSession {
        @XmlElement(name="user")
        private CrowdUser user;

        public CrowdUser getUser() {
            return user;
        }
    }

    @XmlRootElement(name = "error")
    private static class CrowdError {
        @XmlElement(name="reason")
        private String reason;

        @XmlElement(name="message")
        private String message;

        public String getReason() {
            return reason;
        }

        public String getMessage() {
            return message;
        }
    }
}
