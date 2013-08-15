package net.ripe.db.whois.api.whois;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.domain.*;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.VersionDateTime;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.TagResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class WhoisObjectMapperTest {

    @Test
    public void map_rpsl_mntner() throws Exception {
        final RpslObject rpslObject = RpslObject.parse(
                "mntner:      TST-MNT\n" +
                        "descr:       MNTNER for test\n" +
                        "admin-c:     TP1-TEST\n" +
                        "upd-to:      dbtest@ripe.net\n" +
                        "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ # test\n" +
                        "auth:        PGPKEY-28F6CD6C\n" +
                        "mnt-by:      TST-MNT\n" +
                        "referral-by: TST-MNT\n" +
                        "changed:     dbtest@ripe.net\n" +
                        "source:      TEST\n");

        final WhoisObject subject = WhoisObjectMapper.map(rpslObject, true);

        assertThat(subject.getType(), is("mntner"));
        assertThat(subject.getSource().getId(), is("test"));
        assertThat(subject.getLink().getType(), is("locator"));
        assertThat(subject.getLink().getHref(), is("http://rest.db.ripe.net/lookup/test/mntner/TST-MNT"));
        assertThat(subject.getPrimaryKey(), hasSize(1));
        final Attribute primaryKeyAttribute = subject.getPrimaryKey().get(0);
        assertThat(primaryKeyAttribute.getName(), is("mntner"));
        assertThat(primaryKeyAttribute.getValue(), is("TST-MNT"));
        assertThat(subject.getAttributes(), contains(
                new Attribute("mntner", "TST-MNT", null, null, null),
                new Attribute("descr", "MNTNER for test", null, null, null),
                new Attribute("admin-c", "TP1-TEST", null, "person-role", new Link("locator", "http://rest.db.ripe.net/lookup/test/person-role/TP1-TEST")),
                new Attribute("upd-to", "dbtest@ripe.net", null, null, null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "PGPKEY-28F6CD6C", null, "key-cert", new Link("locator", "http://rest.db.ripe.net/lookup/test/key-cert/PGPKEY-28F6CD6C")),
                new Attribute("mnt-by", "TST-MNT", null, "mntner", new Link("locator", "http://rest.db.ripe.net/lookup/test/mntner/TST-MNT")),
                new Attribute("referral-by", "TST-MNT", null, "mntner", new Link("locator", "http://rest.db.ripe.net/lookup/test/mntner/TST-MNT")),
                new Attribute("changed", "dbtest@ripe.net", null, null, null),
                new Attribute("source", "TEST", "Filtered", null, null)
        ));
    }

    @Test
    public void map_rpsl_as_set_members_multiple_values() throws Exception {
        final RpslObject rpslObject = RpslObject.parse("" +
                "as-set:    AS-set-attendees\n" +
                "descr:     AS-set containing all attendees' ASNs.\n" + // TODO: on transform map to &apos;
                "tech-c:    TS1-TEST\n" +
                "admin-c:   TS1-TEST\n" +
                "members:   as1,as2,as3,\n" +
                "mnt-by:    TS1-MNT\n" +
                "changed:   hostmaster@ripe.net 20121115\n" +
                "source:    TEST");

        final WhoisObject subject = WhoisObjectMapper.map(rpslObject);

        assertThat(subject.getType(), is("as-set"));
        assertThat(subject.getSource().getId(), is("test"));
        assertThat(subject.getLink().getType(), is("locator"));
        assertThat(subject.getLink().getHref(), is("http://rest.db.ripe.net/lookup/test/as-set/AS-set-attendees"));
        assertThat(subject.getPrimaryKey(), hasSize(1));
        final Attribute primaryKeyAttribute = subject.getPrimaryKey().get(0);
        assertThat(primaryKeyAttribute.getName(), is("as-set"));
        assertThat(primaryKeyAttribute.getValue(), is("AS-set-attendees"));
        assertThat(subject.getAttributes(), containsInAnyOrder(
                new Attribute("as-set", "AS-set-attendees", null, null, null),
                new Attribute("descr", "AS-set containing all attendees' ASNs.", null, null, null),
                new Attribute("tech-c", "TS1-TEST", null, "person-role", new Link("locator", "http://rest.db.ripe.net/lookup/test/person-role/TS1-TEST")),
                new Attribute("admin-c", "TS1-TEST", null, "person-role", new Link("locator", "http://rest.db.ripe.net/lookup/test/person-role/TS1-TEST")),
                new Attribute("members", "as1", null, "aut-num", new Link("locator", "http://rest.db.ripe.net/lookup/test/aut-num/as1")),
                new Attribute("members", "as2", null, "aut-num", new Link("locator", "http://rest.db.ripe.net/lookup/test/aut-num/as2")),
                new Attribute("members", "as3", null, "aut-num", new Link("locator", "http://rest.db.ripe.net/lookup/test/aut-num/as3")),
                new Attribute("mnt-by", "TS1-MNT", null, "mntner", new Link("locator", "http://rest.db.ripe.net/lookup/test/mntner/TS1-MNT")),
                new Attribute("changed", "hostmaster@ripe.net 20121115", null, null, null),
                new Attribute("source", "TEST", null, null, null)
        ));
    }

    @Test
    public void map_versions() {
        final DeletedVersionResponseObject deleted = new DeletedVersionResponseObject(new VersionDateTime(new LocalDateTime()), ObjectType.AUT_NUM, "AS102");

        final List<VersionResponseObject> versionInfos = Lists.newArrayList(
            new VersionResponseObject(2, Operation.UPDATE, 3, new VersionDateTime(new LocalDateTime()), ObjectType.AUT_NUM, "AS102"),
            new VersionResponseObject(2, Operation.UPDATE, 4, new VersionDateTime(new LocalDateTime()), ObjectType.AUT_NUM, "AS102"));

        final List<WhoisVersion> result = WhoisObjectMapper.mapVersions(Lists.newArrayList(deleted), versionInfos);

        assertThat(result, hasSize(3));
        final WhoisVersion deletedVersion = result.get(0);
        assertThat(deletedVersion.getOperation(), nullValue());
        assertThat(deletedVersion.getRevision(), nullValue());
        assertThat(deletedVersion.getDeletedDate(), is(not(nullValue())));

        final WhoisVersion whoisVersion1 = result.get(1);
        assertThat(whoisVersion1.getOperation(), is("ADD/UPD"));
        assertThat(whoisVersion1.getRevision(), is(3));
        assertThat(whoisVersion1.getDate(), is(not(nullValue())));

        final WhoisVersion whoisVersion2 = result.get(2);
        assertThat(whoisVersion2.getOperation(), is("ADD/UPD"));
        assertThat(whoisVersion2.getRevision(), is(4));
        assertThat(whoisVersion2.getDate(), is(not(nullValue())));
    }

    @Test
    public void map_tags() {
        final List<WhoisTag> tags = WhoisObjectMapper.map(RpslObject.parse("mntner: TEST-MNT\nsource: TEST"),
                Lists.newArrayList(
                new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("foo"),  "foo data"),
                new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("bar"),  "bar data"),
                new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("barf"),  "barf data"))).getTags();

        assertThat(tags, hasSize(3));
        final WhoisTag tag1 = tags.get(0);
        assertThat(tag1.getId(), is("foo"));
        assertThat(tag1.getData(), is("foo data"));

        final WhoisTag tag2 = tags.get(1);
        assertThat(tag2.getId(), is("bar"));
        assertThat(tag2.getData(), is("bar data"));

        final WhoisTag tag3 = tags.get(2);
        assertThat(tag3.getId(), is("barf"));
        assertThat(tag3.getData(), is("barf data"));
    }
}
