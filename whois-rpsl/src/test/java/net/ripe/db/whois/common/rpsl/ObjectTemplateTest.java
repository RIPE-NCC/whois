package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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
            "mnt-by:          DEV-MNT\n" +
            "created:         2014-04-15T13:15:30Z\n" +
            "last-modified:   2014-04-15T13:15:30Z\n" +
            "source:          DEV";

    private ObjectTemplate subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = ObjectTemplate.getTemplate(ObjectType.MNTNER);
    }

    @Test
    public void getObjectSpec_null() {
        assertThrows(IllegalStateException.class, () -> {
            ObjectTemplate.getTemplate(null);
        });
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
        assertThat(multipleAttributes, hasSize(6));
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
        assertThat(template, is("""
                inetnum:        [mandatory]  [single]     [primary/lookup key]
                netname:        [mandatory]  [single]     [lookup key]
                descr:          [optional]   [multiple]   [ ]
                country:        [mandatory]  [multiple]   [ ]
                geofeed:        [optional]   [single]     [ ]
                geoloc:         [optional]   [single]     [ ]
                prefixlen:      [optional]   [single]     [ ]
                language:       [optional]   [multiple]   [ ]
                org:            [conditional][single]     [inverse key]
                sponsoring-org: [conditional][single]     [inverse key]
                admin-c:        [mandatory]  [multiple]   [inverse key]
                tech-c:         [mandatory]  [multiple]   [inverse key]
                abuse-c:        [optional]   [single]     [inverse key]
                status:         [mandatory]  [single]     [ ]
                assignment-size:[optional]   [single]     [ ]
                remarks:        [optional]   [multiple]   [ ]
                notify:         [optional]   [multiple]   [inverse key]
                mnt-by:         [mandatory]  [multiple]   [inverse key]
                mnt-lower:      [conditional][multiple]   [inverse key]
                mnt-domains:    [optional]   [multiple]   [inverse key]
                mnt-routes:     [optional]   [multiple]   [inverse key]
                mnt-irt:        [optional]   [multiple]   [inverse key]
                created:        [generated]  [single]     [ ]
                last-modified:  [generated]  [single]     [ ]
                source:         [mandatory]  [single]     [ ]
                """));
    }

    @Test
    public void verboseStringTemplate() {
        final String template = ObjectTemplate.getTemplate(ObjectType.INETNUM).toVerboseString();
        assertThat(template, containsString("""
                The inetnum class:

                      An inetnum object contains information on allocations and
                      assignments of IPv4 address space.

                inetnum:        [mandatory]  [single]     [primary/lookup key]
                netname:        [mandatory]  [single]     [lookup key]
                descr:          [optional]   [multiple]   [ ]
                country:        [mandatory]  [multiple]   [ ]
                geofeed:        [optional]   [single]     [ ]
                geoloc:         [optional]   [single]     [ ]
                prefixlen:      [optional]   [single]     [ ]
                language:       [optional]   [multiple]   [ ]
                org:            [conditional][single]     [inverse key]
                sponsoring-org: [conditional][single]     [inverse key]
                admin-c:        [mandatory]  [multiple]   [inverse key]
                tech-c:         [mandatory]  [multiple]   [inverse key]
                abuse-c:        [optional]   [single]     [inverse key]
                status:         [mandatory]  [single]     [ ]
                assignment-size:[optional]   [single]     [ ]
                remarks:        [optional]   [multiple]   [ ]
                notify:         [optional]   [multiple]   [inverse key]
                mnt-by:         [mandatory]  [multiple]   [inverse key]
                mnt-lower:      [conditional][multiple]   [inverse key]
                mnt-domains:    [optional]   [multiple]   [inverse key]
                mnt-routes:     [optional]   [multiple]   [inverse key]
                mnt-irt:        [optional]   [multiple]   [inverse key]
                created:        [generated]  [single]     [ ]
                last-modified:  [generated]  [single]     [ ]
                source:         [mandatory]  [single]     [ ]

                The content of the attributes of the inetnum class are defined below:

                """));
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

    @Test
    public void name_transformations() {
        assertThat(ObjectTemplate.getTemplate(ObjectType.MNTNER).getNameToFirstLower(), is("mntner") );
        assertThat(ObjectTemplate.getTemplate(ObjectType.INETNUM).getNameToFirstUpper(), is("Inetnum"));

        assertThat(ObjectTemplate.getTemplate(ObjectType.KEY_CERT).getNameToFirstLower(), is("keyCert") );
        assertThat(ObjectTemplate.getTemplate(ObjectType.KEY_CERT).getNameToFirstUpper(), is("KeyCert") );
    }
}
