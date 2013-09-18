package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DummifierLegacyTest {

    @InjectMocks DummifierLegacy subject;

    private static RpslObject makeObject(ObjectType type, String pkey, RpslAttribute... rpslAttributes) {
        final List<RpslAttribute> attributeList = Lists.newArrayList();

        attributeList.add(new RpslAttribute(AttributeType.getByName(type.getName()), pkey));

        switch (type) {
            case ROUTE:
            case ROUTE6:
                attributeList.add(new RpslAttribute(AttributeType.ORIGIN, "AS3333"));
                break;
            case PERSON:
            case ROLE:
                attributeList.add(new RpslAttribute(AttributeType.NIC_HDL, pkey));
                break;
        }

        attributeList.addAll(Arrays.asList(rpslAttributes));
        return new RpslObject(0, attributeList);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_type() {
        subject.dummify(3, RpslObject.parse("FOOO:BAR\n"));
    }

    @Test
    public void skip_objects_version_1_2() {
        for (ObjectType objectType : DummifierLegacy.SKIPPED_OBJECT_TYPES) {
            RpslObject object = makeObject(objectType, "YAY", new RpslAttribute(AttributeType.REMARKS, "Remark!"), new RpslAttribute(AttributeType.SOURCE, "TEST"));

            assertTrue(subject.isAllowed(1, object));
            assertTrue(subject.isAllowed(2, object));

            if (objectType.equals(ObjectType.ROLE)) {
                assertEquals(subject.dummify(1, object), DummifierLegacy.PLACEHOLDER_ROLE_OBJECT);
                assertEquals(subject.dummify(2, object), DummifierLegacy.PLACEHOLDER_ROLE_OBJECT);
            } else {
                assertEquals(subject.dummify(1, object), DummifierLegacy.PLACEHOLDER_PERSON_OBJECT);
                assertEquals(subject.dummify(2, object), DummifierLegacy.PLACEHOLDER_PERSON_OBJECT);
            }
        }
    }

    @Test
    public void skip_objects_version_3() {
        for (ObjectType objectType : DummifierLegacy.SKIPPED_OBJECT_TYPES) {
            RpslObject object = makeObject(objectType, "YAY", new RpslAttribute(AttributeType.REMARKS, "Remark!"));

            assertFalse(subject.isAllowed(3, object));
            try {
                subject.dummify(3, object);
                fail("Didn't throw IllegalArgumentException for " + objectType);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Test
    public void allow_objects_version_3() {
        for (ObjectType objectType : ObjectType.values()) {
            if (DummifierLegacy.SKIPPED_OBJECT_TYPES.contains(objectType)) {
                continue;
            }

            assertTrue(subject.isAllowed(3, makeObject(objectType, "YAY", new RpslAttribute(AttributeType.REMARKS, "Remark!"))));
        }
    }

    @Test
    public void dummify_removes_double_person_role_references() {
        final ArrayList<RpslAttribute> attributes = Lists.newArrayList(makeObject(ObjectType.INETNUM, "10.0.0.0").getAttributes());

        final String tempValue = "VALUE";
        for (AttributeType personRoleReference : DummifierLegacy.PERSON_ROLE_REFERENCES) {
            final RpslAttribute attribute = new RpslAttribute(personRoleReference.getName(), tempValue);

            attributes.add(attribute);
            attributes.add(attribute);
        }

        assertThat(attributes, hasSize(1 + 2 * DummifierLegacy.PERSON_ROLE_REFERENCES.size()));

        attributes.add(new RpslAttribute(AttributeType.SOURCE, "TEST"));

        final RpslObject rpslObject = new RpslObject(0, attributes);
        final RpslObject dummifiedObject = subject.dummify(3, rpslObject);

        for (AttributeType personRoleReference : DummifierLegacy.PERSON_ROLE_REFERENCES) {
            final List<RpslAttribute> rpslAttributes = dummifiedObject.findAttributes(personRoleReference);
            assertThat(personRoleReference.toString(), rpslAttributes, hasSize(1));
            assertThat(rpslAttributes.get(0).getValue(), is(not(tempValue)));
        }
    }

    @Test
    public void dummify_adds_remarks() {
        RpslObject dummifiedObject = subject.dummify(3, makeObject(ObjectType.ROUTE, "10/8", new RpslAttribute(AttributeType.SOURCE, "TEST")));

        assertThat(dummifiedObject.findAttributes(AttributeType.REMARKS), hasSize(7));
    }

    @Test
    public void strip_optional_from_org_and_mntner() {
        for (ObjectType objectType : DummifierLegacy.STRIPPED_OBJECT_TYPES) {
            // Make a list of RpslAttributes that do not match my ObjectType
            AttributeType objectAttributeType = AttributeType.getByName(objectType.getName());

            List<RpslAttribute> optionalAttributes = Lists.newArrayList();
            for (AttributeType attributeType : AttributeType.values()) {
                if (!attributeType.equals(objectAttributeType)) {
                    final RpslAttribute rpslAttribute = new RpslAttribute(attributeType, "FOO");
                    optionalAttributes.add(rpslAttribute);
                }
            }

            final RpslObject rpslObject = makeObject(objectType, "FOO", optionalAttributes.toArray(new RpslAttribute[optionalAttributes.size()]));
            final RpslObject dummifiedObject = subject.dummify(3, rpslObject);

            assertThat(dummifiedObject.getAttributes(), hasSize(ObjectTemplate.getTemplate(objectType).getMandatoryAttributes().size() + 1));
            assertThat(dummifiedObject.findAttributes(AttributeType.ABUSE_C), hasSize(1));
        }
    }

    @Test
    public void trip_optional_from_org_and_mntner() {
        final String tempValue = "REPLACEME";

        for (ObjectType objectType : DummifierLegacy.STRIPPED_OBJECT_TYPES) {
            // Make a list of RpslAttributes that do not match my ObjectType
            AttributeType objectAttributeType = AttributeType.getByName(objectType.getName());

            List<RpslAttribute> optionalAttributes = Lists.newArrayList();
            for (AttributeType attributeType : DummifierLegacy.DUMMIFICATION_REPLACEMENTS.keySet()) {
                if (!attributeType.equals(objectAttributeType)) {
                    final RpslAttribute rpslAttribute = new RpslAttribute(attributeType, tempValue);
                    optionalAttributes.add(rpslAttribute);
                    optionalAttributes.add(rpslAttribute);
                }
            }

            optionalAttributes.add(new RpslAttribute(AttributeType.SOURCE, "TEST"));

            final RpslObject rpslObject = makeObject(objectType, "FOO", optionalAttributes.toArray(new RpslAttribute[optionalAttributes.size()]));
            final RpslObject dummifiedObject = subject.dummify(3, rpslObject);

            for (RpslAttribute attribute : dummifiedObject.getAttributes()) {
                if (DummifierLegacy.DUMMIFICATION_REPLACEMENTS.containsKey(attribute.getType())) {
                    assertThat(attribute.getValue(), is(not(tempValue)));
                }
            }
        }
    }

    @Test
    public void allow_role_with_abuse_mailbox() {
        final RpslObject rpslObject = RpslObject.parse(1, "" +
                "role:          Test Role\n" +
                "nic-hdl:       TR1-TEST\n" +
                "abuse-mailbox: abuse@mailbox.com\n" +
                "source:        TEST");

        assertTrue(subject.isAllowed(3, rpslObject));
    }

    @Test
    public void dummify_role_with_abuse_mailbox() {
        final RpslObject rpslObject = RpslObject.parse(1, "" +
                "role:          Test Role\n" +
                "nic-hdl:       TR1-TEST\n" +
                "abuse-mailbox: abuse@mailbox.com\n" +
                "source:        TEST");

        final RpslObject dummified = subject.dummify(3, rpslObject);

        assertThat(dummified.toString(), is("" +
                "role:           Test Role\n" +
                "nic-hdl:        TR1-TEST\n" +
                "abuse-mailbox:  abuse@mailbox.com\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n"));
    }
}
