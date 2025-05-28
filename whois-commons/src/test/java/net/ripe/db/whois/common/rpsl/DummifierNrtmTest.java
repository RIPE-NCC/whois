package net.ripe.db.whois.common.rpsl;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(MockitoExtension.class)
public class DummifierNrtmTest {

    @InjectMocks
    DummifierNrtm subject;

    @Test
    public void null_type() {
        assertThrows(IllegalArgumentException.class, () -> {
            subject.dummify(3, RpslObject.parse("FOOO:BAR\n"));
        });
    }

    @Test
    public void skip_objects_version_1_2() {
        for (ObjectType objectType : DummifierNrtm.SKIPPED_OBJECT_TYPES) {
            RpslObject object = createObject(objectType, "YAY", new RpslAttribute(AttributeType.REMARKS, "Remark!"), new RpslAttribute(AttributeType.SOURCE, "TEST"));

            assertThat(subject.isAllowed(1, object), is(true));
            assertThat(subject.isAllowed(2, object), is(true));

            if (objectType.equals(ObjectType.ROLE)) {
                assertThat(subject.dummify(1, object), is(DummifierNrtm.getPlaceholderRoleObject()));
                assertThat(subject.dummify(2, object), is(DummifierNrtm.getPlaceholderRoleObject()));
            } else {
                assertThat(subject.dummify(1, object), is(DummifierNrtm.getPlaceholderPersonObject()));
                assertThat(subject.dummify(2, object), is(DummifierNrtm.getPlaceholderPersonObject()));
            }

        }
    }

    @Test
    public void skip_objects_version_3() {
        for (ObjectType objectType : DummifierNrtm.SKIPPED_OBJECT_TYPES) {
            final RpslObject object = createObject(objectType, "YAY", new RpslAttribute(AttributeType.REMARKS, "Remark!"));

            assertThat(subject.isAllowed(3, object), is(false));

            try {
                subject.dummify(3, object);
                fail("Didn't throw IllegalArgumentException for " + objectType);
            } catch (IllegalArgumentException e) {
                assertThat(e.getMessage(), is("The given object type should be skipped" + objectType));
            }
        }
    }

    @Test
    public void allow_objects_version_3() {
        for (ObjectType objectType : ObjectType.values()) {
            if (DummifierNrtm.SKIPPED_OBJECT_TYPES.contains(objectType)) {
                continue;
            }

            final RpslObject rpslObject = createObject(objectType, "YAY", new RpslAttribute(AttributeType.REMARKS, "Remark!"));

            assertThat(subject.isAllowed(3, rpslObject), is(true));
        }
    }

    @Test
    public void dummify_removes_double_person_role_references() {
        final ArrayList<RpslAttribute> attributes = Lists.newArrayList(createObject(ObjectType.INETNUM, "10.0.0.0").getAttributes());

        final String tempValue = "VALUE";
        for (AttributeType personRoleReference : DummifierNrtm.PERSON_ROLE_REFERENCES) {
            final RpslAttribute attribute = new RpslAttribute(personRoleReference.getName(), tempValue);

            attributes.add(attribute);
            attributes.add(attribute);
        }

        assertThat(attributes, hasSize(3 + 2 * DummifierNrtm.PERSON_ROLE_REFERENCES.size()));

        attributes.add(new RpslAttribute(AttributeType.SOURCE, "TEST"));

        final RpslObject rpslObject = new RpslObject(0, attributes);
        final RpslObject dummifiedObject = subject.dummify(3, rpslObject);

        for (AttributeType personRoleReference : DummifierNrtm.PERSON_ROLE_REFERENCES) {
            final List<RpslAttribute> rpslAttributes = dummifiedObject.findAttributes(personRoleReference);
            assertThat(personRoleReference.toString(), rpslAttributes, hasSize(1));
            assertThat(rpslAttributes.get(0).getValue(), is(not(tempValue)));
        }

        assertThat(dummifiedObject.findAttributes(AttributeType.CREATED, AttributeType.LAST_MODIFIED), hasSize(2));
    }

    @Test
    public void dummify_adds_remarks() {
        final RpslObject routeObject = RpslObject.parse(
                "route:          10/8\n" +
                "origin:         AS3333\n" +
                "source:         TEST");

        final RpslObject dummifiedRouteObject = subject.dummify(3, routeObject);

        assertThat(dummifiedRouteObject.toString(), is(
                "route:          10/8\n" +
                "origin:         AS3333\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n"));
    }

    @Test
    public void strip_optional_from_organisation() {
        final List<RpslAttribute> attributes = Lists.newArrayList();

        for (AttributeType attributeType : AttributeType.values()) {
            if (attributeType != AttributeType.ORGANISATION
                    //we have already added created/last mod. in the make object method
                    && attributeType != AttributeType.CREATED
                    && attributeType != AttributeType.LAST_MODIFIED) {

                final RpslAttribute rpslAttribute = new RpslAttribute(attributeType, "FOO");
                attributes.add(rpslAttribute);
            }
        }

        final RpslObject organisation = createObject(ObjectType.ORGANISATION, "FOO", attributes);
        final RpslObject dummifiedOrganisation = subject.dummify(3, organisation);

        assertThat(dummifiedOrganisation.toString(), is(
                        "organisation:   FOO\n" +
                        "created:        2001-02-04T17:00:00Z\n" +
                        "last-modified:  2001-02-04T17:00:00Z\n" +
                        "abuse-c:        FOO\n" +
                        "address:        Dummy address for FOO\n" +
                        "country:        FOO\n" +
                        "e-mail:         unread@ripe.net\n" +
                        "mnt-by:         FOO\n" +
                        "mnt-ref:        FOO\n" +
                        "org-name:       FOO\n" +
                        "org-type:       FOO\n" +
                        "source:         FOO\n"));
    }

    @Test
    public void strip_optional_from_mntner() {
        final List<RpslAttribute> attributes = Lists.newArrayList();

        for (AttributeType attributeType : AttributeType.values()) {
            if (attributeType != AttributeType.MNTNER
                    //we have already added created/last mod. in the make object method
                    && attributeType != AttributeType.CREATED
                    && attributeType != AttributeType.LAST_MODIFIED) {

                final RpslAttribute rpslAttribute = new RpslAttribute(attributeType, "FOO");
                attributes.add(rpslAttribute);
            }
        }

        final RpslObject mntner = createObject(ObjectType.MNTNER, "FOO", attributes);
        final RpslObject dummifiedMntner = subject.dummify(3, mntner);

        assertThat(dummifiedMntner.toString(), is(
                "mntner:         FOO\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "admin-c:        DUMY-RIPE\n" +
                "auth:           MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security\n" +
                "mnt-by:         FOO\n" +
                "source:         FOO\n" +
                "upd-to:         unread@ripe.net\n"));
    }

    @Test
    public void strip_optional_from_mntner_and_make_replacements() {
        final RpslObject mntner = RpslObject.parse(
                "mntner:         FOO\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "address:        REPLACEME\n" +
                "address:        REPLACEME\n" +
                "auth:           REPLACEME\n" +
                "auth:           REPLACEME\n" +
                "e-mail:         REPLACEME\n" +
                "e-mail:         REPLACEME\n" +
                "fax-no:         REPLACEME\n" +
                "fax-no:         REPLACEME\n" +
                "phone:          REPLACEME\n" +
                "phone:          REPLACEME\n" +
                "upd-to:         REPLACEME\n" +
                "upd-to:         REPLACEME\n" +
                "source:         TEST");

        final RpslObject dummifiedMtner = subject.dummify(3, mntner);

        assertThat(dummifiedMtner.toString(), is(
                "mntner:         FOO\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "auth:           MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security\n" +
                "upd-to:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n"));
    }

    @Test
    public void strip_optional_from_organisation_and_make_replacements() {
        final RpslObject organisation = RpslObject.parse(
                "organisation:   FOO\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "address:        REPLACEME\n" +
                "address:        REPLACEME\n" +
                "auth:           REPLACEME\n" +
                "auth:           REPLACEME\n" +
                "e-mail:         REPLACEME\n" +
                "e-mail:         REPLACEME\n" +
                "fax-no:         REPLACEME\n" +
                "fax-no:         REPLACEME\n" +
                "phone:          REPLACEME\n" +
                "phone:          REPLACEME\n" +
                "upd-to:         REPLACEME\n" +
                "upd-to:         REPLACEME\n" +
                "source:         TEST");

        final RpslObject dummifiedOrganisation = subject.dummify(3, organisation);

        assertThat(dummifiedOrganisation.toString(), is(
                "organisation:   FOO\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
                "address:        Dummy address for FOO\n" +
                "e-mail:         unread@ripe.net\n" +
                "source:         TEST\n" +
                "remarks:        ****************************\n" +
                "remarks:        * THIS OBJECT IS MODIFIED\n" +
                "remarks:        * Please note that all data that is generally regarded as personal\n" +
                "remarks:        * data has been removed from this object.\n" +
                "remarks:        * To view the original object, please query the RIPE Database at:\n" +
                "remarks:        * http://www.ripe.net/whois\n" +
                "remarks:        ****************************\n"));
    }

    @Test
    public void allow_role_with_abuse_mailbox() {
        final RpslObject role = RpslObject.parse(
                "role:          Test Role\n" +
                "created:       2001-02-04T17:00:00Z\n" +
                "last-modified: 2001-02-04T17:00:00Z\n" +
                "nic-hdl:       TR1-TEST\n" +
                "abuse-mailbox: abuse@mailbox.com\n" +
                "source:        TEST");

        assertThat(subject.isAllowed(3, role), is(true));
    }

    @Test
    public void dummify_role_with_abuse_mailbox() {
        final RpslObject rpslObject = RpslObject.parse(1, "" +
                "role:          Test Role\n" +
                "created:       2001-02-04T17:00:00Z\n" +
                "last-modified: 2001-02-04T17:00:00Z\n" +
                "nic-hdl:       TR1-TEST\n" +
                "abuse-mailbox: abuse@mailbox.com\n" +
                "source:        TEST");

        final RpslObject dummified = subject.dummify(3, rpslObject);

        assertThat(dummified.toString(), is("" +
                "role:           Test Role\n" +
                "created:        2001-02-04T17:00:00Z\n" +
                "last-modified:  2001-02-04T17:00:00Z\n" +
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

    @Test
    public void dummify_mntner_keeps_dummy_auth_line_only() {
        final RpslObject mntner = RpslObject.parse(11, "" +
                "mntner: AARDVARK-MNT\n" +
                "descr: Mntner for guy's objects\n" +
                "admin-c: FB99999-RIPE\n" +
                "tech-c: FB99999-RIPE\n" +
                "upd-to: guy@ripe.net\n" +
                "auth: X509-1\n" +
                "auth: X509-1689\n" +
                "auth: MD5-PW $1$SaltSalt$ThisIsABrokenMd5Hash.\n" +
                "auth: SSO 1234-5678-9abc-dead-beef\n" +
                "notify: guy@ripe.net\n" +
                "mnt-by: AARDVARK-MNT\n" +
                "created: 2001-02-04T17:00:00Z\n" +
                "last-modified: 2001-02-04T17:00:00Z\n" +
                "source: RIPE # Filtered");

        final RpslObject dummified = subject.dummify(3, mntner);

        assertThat(dummified.findAttribute(AttributeType.AUTH), is(new RpslAttribute("auth", "MD5-PW $1$SaltSalt$DummifiedMD5HashValue.   # Real value hidden for security")));
        assertThat(dummified.getValueForAttribute(AttributeType.CREATED).toString(), is("2001-02-04T17:00:00Z"));
        assertThat(dummified.getValueForAttribute(AttributeType.LAST_MODIFIED).toString(), is("2001-02-04T17:00:00Z"));
    }

    // helper methods

    private static RpslObject createObject(final ObjectType objectType, final String pkey, final RpslAttribute... rpslAttributes) {
        final List<RpslAttribute> attributeList = Lists.newArrayList();

        attributeList.add(new RpslAttribute(AttributeType.getByName(objectType.getName()), pkey));
        attributeList.add(new RpslAttribute(AttributeType.CREATED, "2001-02-04T17:00:00Z"));
        attributeList.add(new RpslAttribute(AttributeType.LAST_MODIFIED, "2001-02-04T17:00:00Z"));

        switch (objectType) {
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

    private static RpslObject createObject(final ObjectType objectType, final String pkey, Iterable<RpslAttribute> rpslAttributes) {
        return createObject(objectType, pkey, Iterables.toArray(rpslAttributes, RpslAttribute.class));
    }
}
