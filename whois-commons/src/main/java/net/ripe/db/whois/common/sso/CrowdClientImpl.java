package net.ripe.db.whois.common.sso;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.ripe.db.whois.common.profiles.DeployedProfile;
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

@DeployedProfile
@Component
public class CrowdClientImpl implements CrowdClient {
    private final String restUrl;
    private final Client client;
    private final Unmarshaller uuidUnmarshaller;
    private final Unmarshaller usernameUnmarshaller;

    @Autowired
    public CrowdClientImpl(@Value("${rest.crowd.url}") final String translatorUrl,
                           @Value("${rest.crowd.user}") final String crowdAuthUser,
                           @Value("${rest.crowd.password}") final String crowdAuthPassword) {
        this.restUrl = translatorUrl;
        client = ClientBuilder.newBuilder().register(new HttpBasicAuthFilter(crowdAuthUser, crowdAuthPassword)).build();

        try {
            uuidUnmarshaller = JAXBContext.newInstance(CrowdResponse.class).createUnmarshaller();
            usernameUnmarshaller = JAXBContext.newInstance(CrowdUser.class).createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String getUuid(final String username) {
        final String url = String.format(
                "%s/rest/usermanagement/latest/user/attribute?username=%s",
                restUrl,
                username);

        String response;
        try {
            response = client.target(url).request().get(String.class);
        } catch (NotFoundException e) {
            return null;
        }

        return extractUUID(response);
    }

    @Override
    public String getUsername(final String uuid) {
        final String url = String.format(
                "%scrowd/rest/sso/latest/uuid=%s",
                restUrl,
                uuid);

        final String response = "";
        try {
            client.target(url).request().get(String.class);
        } catch (NotFoundException e) {
            return null;
        }

        return extractUsername(response);
    }

    private String extractUUID(final String response) {
        try {
            final CrowdResponse crowdResponse = (CrowdResponse)uuidUnmarshaller.unmarshal(new ByteArrayInputStream(response.getBytes()));
            return crowdResponse.getUUID();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private String extractUsername(final String response) {
        try {
            final CrowdUser crowdUser = (CrowdUser)usernameUnmarshaller.unmarshal(new ByteArrayInputStream(response.getBytes()));
            return crowdUser.getName();
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

        public String getName() {
            return name;
        }
    }
}
