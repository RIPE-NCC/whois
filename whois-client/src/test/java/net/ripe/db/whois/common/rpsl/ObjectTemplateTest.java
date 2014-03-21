package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.Message;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class ObjectTemplateTest {
    private static final String MAINTAINER_OBJECT_STRING = "" +
            "mntner:          DEV-MNT\n" +
            "descr:           DEV maintainer\n" +
            "admin-c:         VM1-DEV\n" +
            "tech-c:          VM1-DEV\n" +
            "upd-to:          v.m@example.net\n" +
            "mnt-nfy:         auto@example.net\n" +
            "auth:            MD5-PW $1$q8Su3Hq/$rJt5M3TNLeRE4UoCh5bSH/\n" +
            "remarks:         password: secret\n" +
            "mnt-by:      DEV-MNT\n" +
            "referral-by:     DEV-MNT\n" +
            "changed:         BECHA@example.net 20101010\n" +
            "source:          DEV";

    private ObjectTemplate subject;

    @Before
    public void setUp() throws Exception {
        subject = ObjectTemplate.getTemplate(ObjectType.MNTNER);
    }

    @Test(expected = IllegalStateException.class)
    public void getObjectSpec_null() {
        ObjectTemplate.getTemplate(null);
    }

    @Test
    public void getObjectType() {
        assertThat(subject.getObjectType(), is(ObjectType.MNTNER));
    }

    @Test
    public void validate_no_errors() {
        final ObjectMessages objectMessages = subject.validate(RpslObject.parse(MAINTAINER_OBJECT_STRING));

        assertThat(objectMessages.hasErrors(), is(false));
    }

    @Test
    public void getMultipleAttributes(){
        final ObjectTemplate template = ObjectTemplate.getTemplate(ObjectType.AS_BLOCK);
        Set<AttributeType> multipleAttributes = template.getMultipleAttributes();
        assertThat(multipleAttributes.size(), is(6));
    }

  @Test
    public void validate_mandatory_missing() {
        final RpslObject rpslObject = RpslObject.parse(MAINTAINER_OBJECT_STRING.substring(0, MAINTAINER_OBJECT_STRING.lastIndexOf('\n') + 1));
        final ObjectMessages objectMessages = subject.validate(rpslObject);

        assertThat(objectMessages.hasErrors(), is(true));
        assertThat(objectMessages.getMessages().getErrors(), contains(ValidationMessages.missingMandatoryAttribute(AttributeType.SOURCE)));

        assertZeroAttributeErrors(rpslObject, objectMessages);
    }

    @Test
    public void validate_single_occurs_multiple_times() {
        final RpslObject rpslObject = RpslObject.parse(MAINTAINER_OBJECT_STRING + "\nsource: RIPE\n");
        final ObjectMessages objectMessages = subject.validate(rpslObject);

        assertThat(objectMessages.hasErrors(), is(true));
        assertThat(objectMessages.getMessages().getErrors(), contains(ValidationMessages.tooManyAttributesOfType(AttributeType.SOURCE)));

        assertZeroAttributeErrors(rpslObject, objectMessages);
    }

    @Test
    public void validate_unknown_attribute() {
        final RpslObject rpslObject = RpslObject.parse(MAINTAINER_OBJECT_STRING + "\ninvalid: invalid\n");
        final ObjectMessages objectMessages = subject.validate(rpslObject);

        assertThat(objectMessages.hasErrors(), is(true));
        assertThat(objectMessages.getMessages().getErrors(), hasSize(0));

        final List<RpslAttribute> attributes = rpslObject.getAttributes();
        final RpslAttribute lastAttribute = attributes.get(attributes.size() - 1);
        final Collection<Message> messages = objectMessages.getMessages(lastAttribute).getAllMessages();

        assertThat(messages, hasSize(1));
        assertThat(messages, contains(ValidationMessages.unknownAttribute("invalid")));
    }

    @Test
    public void validate_invalid_attribute() {
        final RpslObject rpslObject = RpslObject.parse(MAINTAINER_OBJECT_STRING + "\nperson: Harry\n");
        final ObjectMessages objectMessages = subject.validate(rpslObject);

        assertThat(objectMessages.hasErrors(), is(true));
        assertThat(objectMessages.getMessages().getErrors(), hasSize(0));

        final List<RpslAttribute> attributes = rpslObject.getAttributes();
        final RpslAttribute lastAttribute = attributes.get(attributes.size() - 1);
        final Collection<Message> messages = objectMessages.getMessages(lastAttribute).getAllMessages();

        assertThat(messages, hasSize(1));
        assertThat(messages, contains(ValidationMessages.invalidAttributeForObject(AttributeType.PERSON)));
    }

    @Test
    public void isSet() {
        for (ObjectType objectType : ObjectType.values()) {
            assertThat(objectType.getName().toLowerCase().contains("set"), is(ObjectTemplate.getTemplate(objectType).isSet()));
        }
    }

    private void assertZeroAttributeErrors(final RpslObject rpslObject, final ObjectMessages objectMessages) {
        for (final RpslAttribute attribute : rpslObject.getAttributes()) {
            assertThat(objectMessages.getMessages(attribute).getErrors(), hasSize(0));
        }
    }

    @Test
    public void stringTemplate() {
        final String template = ObjectTemplate.getTemplate(ObjectType.INETNUM).toString();
        assertThat(template, is("" +
                "inetnum:        [mandatory]  [single]     [primary/lookup key]\n" +
                "netname:        [mandatory]  [single]     [lookup key]\n" +
                "descr:          [mandatory]  [multiple]   [ ]\n" +
                "country:        [mandatory]  [multiple]   [ ]\n" +
                "geoloc:         [optional]   [single]     [ ]\n" +
                "language:       [optional]   [multiple]   [ ]\n" +
                "org:            [optional]   [single]     [inverse key]\n" +
                "sponsoring-org: [generated]  [single]     [ ]\n" +
                "admin-c:        [mandatory]  [multiple]   [inverse key]\n" +
                "tech-c:         [mandatory]  [multiple]   [inverse key]\n" +
                "status:         [mandatory]  [single]     [ ]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "mnt-lower:      [optional]   [multiple]   [inverse key]\n" +
                "mnt-domains:    [optional]   [multiple]   [inverse key]\n" +
                "mnt-routes:     [optional]   [multiple]   [inverse key]\n" +
                "mnt-irt:        [optional]   [multiple]   [inverse key]\n" +
                "changed:        [mandatory]  [multiple]   [ ]\n" +
                "source:         [mandatory]  [single]     [ ]\n"));
    }

    @Test
    public void verboseStringTemplate() {
        final String template = ObjectTemplate.getTemplate(ObjectType.INETNUM).toVerboseString();
        assertThat(template, containsString("" +
                "The inetnum class:\n" +
                "\n" +
                "      An inetnum object contains information on allocations and\n" +
                "      assignments of IPv4 address space.\n" +
                "\n" +
                "inetnum:        [mandatory]  [single]     [primary/lookup key]\n" +
                "netname:        [mandatory]  [single]     [lookup key]\n" +
                "descr:          [mandatory]  [multiple]   [ ]\n" +
                "country:        [mandatory]  [multiple]   [ ]\n" +
                "geoloc:         [optional]   [single]     [ ]\n" +
                "language:       [optional]   [multiple]   [ ]\n" +
                "org:            [optional]   [single]     [inverse key]\n" +
                "sponsoring-org: [generated]  [single]     [ ]\n" +
                "admin-c:        [mandatory]  [multiple]   [inverse key]\n" +
                "tech-c:         [mandatory]  [multiple]   [inverse key]\n" +
                "status:         [mandatory]  [single]     [ ]\n" +
                "remarks:        [optional]   [multiple]   [ ]\n" +
                "notify:         [optional]   [multiple]   [inverse key]\n" +
                "mnt-by:         [mandatory]  [multiple]   [inverse key]\n" +
                "mnt-lower:      [optional]   [multiple]   [inverse key]\n" +
                "mnt-domains:    [optional]   [multiple]   [inverse key]\n" +
                "mnt-routes:     [optional]   [multiple]   [inverse key]\n" +
                "mnt-irt:        [optional]   [multiple]   [inverse key]\n" +
                "changed:        [mandatory]  [multiple]   [ ]\n" +
                "source:         [mandatory]  [single]     [ ]\n" +
                "\n" +
                "The content of the attributes of the inetnum class are defined below:\n" +
                "\n"));
    }

    @Test
    public void allObjectTypesSupported() {
        for (final ObjectType objectType : ObjectType.values()) {
            ObjectTemplate.getTemplate(objectType);
        }
    }

    @Test
    public void allAttributesSupported() {
        for (final ObjectType objectType : ObjectType.values()) {
            final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(objectType);

            for (final AttributeTemplate attributeTemplate : objectTemplate.getAttributeTemplates()) {
                final AttributeType attributeType = attributeTemplate.getAttributeType();
                if (attributeType.getSyntax() == null) {
                    fail("Attribute syntax not supported for: " + attributeType);
                }
            }
        }
    }

    @Test
    public void type_or_keys_occur_only_once() {
        for (final ObjectType objectType : ObjectType.values()) {
            final ObjectTemplate objectTemplate = ObjectTemplate.getTemplate(objectType);

            boolean first = true;
            for (final AttributeTemplate attributeTemplate : objectTemplate.getAttributeTemplates()) {
                if (first) {
                    if (!attributeTemplate.getCardinality().equals(AttributeTemplate.Cardinality.SINGLE)) {
                        fail("Type attribute can occur only once: " + attributeTemplate);
                    }

                    if (attributeTemplate.getAttributeType().isListValue()) {
                        fail("Type attribute cannot be a list value: " + attributeTemplate);
                    }

                    first = false;
                }

                if (attributeTemplate.getKeys().contains(AttributeTemplate.Key.PRIMARY_KEY)) {
                    if (!attributeTemplate.getCardinality().equals(AttributeTemplate.Cardinality.SINGLE)) {
                        fail("Key attribute can occur only once: " + attributeTemplate);
                    }

                    if (attributeTemplate.getAttributeType().isListValue()) {
                        fail("Key attribute cannot be a list value: " + attributeTemplate);
                    }
                }
            }
        }
    }
}
