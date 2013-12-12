package net.ripe.db.whois.update.sso;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class SsoTranslator {
    public static final Splitter SPACE_SPLITTER = Splitter.on(' ');
    private final String restUrl;
    private final Client client;
    private final Unmarshaller uuidUnmarshaller;
    private final Unmarshaller usernameUnmarshaller;

    @Autowired
    public SsoTranslator(@Value("${rest.crowd.url}") final String translatorUrl,
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

    public String getUuidForUsername(final String username) {
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

        return extractUUID(response);
    }

    public String getUsernameForUuid(final String uuid) {
        final String url = String.format(
                "%scrowd/rest/sso/latest/uuid=%s",
                restUrl,
                uuid);

        final String response = "";
        try {
            client.target(url).request().get(String.class);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("Unknown RIPE Access uuid: " + uuid);
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

    public void populate(final Update update, final UpdateContext updateContext) {
        final RpslObject submittedObject = update.getSubmittedObject();
        if (!ObjectType.MNTNER.equals(submittedObject.getType())) {
            return;
        }

        for (final RpslAttribute auth : submittedObject.findAttributes(AttributeType.AUTH)) {
            final Iterator<String> authIterator = SPACE_SPLITTER.split(auth.getCleanValue()).iterator();
            final String passwordType = authIterator.next();
            if (passwordType.equalsIgnoreCase("SSO")) {
                String username = authIterator.next();
                if (!updateContext.hasSsoTranslationResult(username)) {
                    updateContext.addSsoTranslationResult(username, getUuidForUsername(username));
                }
            }
        }
    }

    public RpslObject translateAuthToUuid(UpdateContext updateContext, RpslObject rpslObject) {
        return translateAuth(updateContext, rpslObject);
    }

    public RpslObject translateAuthToUsername(final UpdateContext updateContext, final RpslObject rpslObject) {
        return translateAuth(updateContext, rpslObject);
    }

    private RpslObject translateAuth(final UpdateContext updateContext, final RpslObject rpslObject) {
        if (!ObjectType.MNTNER.equals(rpslObject.getType())) {
            return rpslObject;
        }

        Map<RpslAttribute, RpslAttribute> replace = Maps.newHashMap();
        for (RpslAttribute auth : rpslObject.findAttributes(AttributeType.AUTH)) {
            final Iterator<String> authIterator = SPACE_SPLITTER.split(auth.getCleanValue()).iterator();
            final String passwordType = authIterator.next().toUpperCase();
            if (passwordType.equals("SSO")) {
                String token = authIterator.next();
                String authValue = "SSO " + updateContext.getSsoTranslationResult(token);
                replace.put(auth, new RpslAttribute(auth.getKey(), authValue));
            }
        }

        if (replace.isEmpty()) {
            return rpslObject;
        } else {
            return new RpslObjectBuilder(rpslObject).replaceAttributes(replace).get();
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
