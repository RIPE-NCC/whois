package net.ripe.db.whois.api.rest.mapper;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class WhoisObjectServerMapperTest {
/*
    private static final String BASE_URL = "http://rest.db.ripe.net/lookup";

    @Mock
    private ReferencedTypeResolver referencedTypeResolver;
    private WhoisObjectServerMapper subject;

    @Before
    public void setup() {
        subject = new WhoisObjectServerMapper(referencedTypeResolver, BASE_URL);
    }

    @Test
    public void map_rpsl_mntner() throws Exception {
        when(referencedTypeResolver.getReferencedType(AttributeType.ADMIN_C, CIString.ciString("TP1-TEST"))).thenReturn("person");
        when(referencedTypeResolver.getReferencedType(AttributeType.AUTH, CIString.ciString("PGPKEY-28F6CD6C"))).thenReturn("key-cert");
        when(referencedTypeResolver.getReferencedType(AttributeType.MNT_BY, CIString.ciString("TST-MNT"))).thenReturn("mntner");
        when(referencedTypeResolver.getReferencedType(AttributeType.REFERRAL_BY, CIString.ciString("TST-MNT"))).thenReturn("mntner");

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

        final WhoisObject whoisObject = subject.map(rpslObject);

        assertThat(whoisObject.getType(), is("mntner"));
        assertThat(whoisObject.getSource().getId(), is("test"));
        assertThat(whoisObject.getLink().getType(), is("locator"));
        assertThat(whoisObject.getLink().getHref(), is("http://rest.db.ripe.net/lookup/test/mntner/TST-MNT"));
        assertThat(whoisObject.getPrimaryKey(), hasSize(1));
        final Attribute primaryKeyAttribute = whoisObject.getPrimaryKey().get(0);
        assertThat(primaryKeyAttribute.getName(), is("mntner"));
        assertThat(primaryKeyAttribute.getValue(), is("TST-MNT"));
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "TST-MNT", null, null, null),
                new Attribute("descr", "MNTNER for test", null, null, null),
                new Attribute("admin-c", "TP1-TEST", null, "person", new Link("locator", "http://rest.db.ripe.net/lookup/test/person/TP1-TEST")),
                new Attribute("upd-to", "dbtest@ripe.net", null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null),
                new Attribute("auth", "PGPKEY-28F6CD6C", null, "key-cert", new Link("locator", "http://rest.db.ripe.net/lookup/test/key-cert/PGPKEY-28F6CD6C")),
                new Attribute("mnt-by", "TST-MNT", null, "mntner", new Link("locator", "http://rest.db.ripe.net/lookup/test/mntner/TST-MNT")),
                new Attribute("referral-by", "TST-MNT", null, "mntner", new Link("locator", "http://rest.db.ripe.net/lookup/test/mntner/TST-MNT")),
                new Attribute("changed", "dbtest@ripe.net", null, null, null),
                new Attribute("source", "TEST", null, null, null)
        ));
    }

    @Test
    public void map_rpsl_as_set_members_multiple_values() throws Exception {
        when(referencedTypeResolver.getReferencedType(eq(AttributeType.TECH_C), any(CIString.class))).thenReturn("person");
        when(referencedTypeResolver.getReferencedType(eq(AttributeType.ADMIN_C), any(CIString.class))).thenReturn("person");
        when(referencedTypeResolver.getReferencedType(eq(AttributeType.MEMBERS), any(CIString.class))).thenReturn("aut-num");
        when(referencedTypeResolver.getReferencedType(eq(AttributeType.MNT_BY), any(CIString.class))).thenReturn("mntner");


        final RpslObject rpslObject = RpslObject.parse("" +
                "as-set:    AS-set-attendees\n" +
                "descr:     AS-set containing all attendees' ASNs.\n" + // TODO: on transform map to &apos;
                "tech-c:    TS1-TEST\n" +
                "admin-c:   TS1-TEST\n" +
                "members:   as1,as2,as3\n" +
                "mnt-by:    TS1-MNT\n" +
                "changed:   hostmaster@ripe.net 20121115\n" +
                "source:    TEST");

        final WhoisObject whoisObject = subject.map(rpslObject);

        assertThat(whoisObject.getType(), is("as-set"));
        assertThat(whoisObject.getSource().getId(), is("test"));
        assertThat(whoisObject.getLink().getType(), is("locator"));
        assertThat(whoisObject.getLink().getHref(), is("http://rest.db.ripe.net/lookup/test/as-set/AS-set-attendees"));
        assertThat(whoisObject.getPrimaryKey(), hasSize(1));
        final Attribute primaryKeyAttribute = whoisObject.getPrimaryKey().get(0);
        assertThat(primaryKeyAttribute.getName(), is("as-set"));
        assertThat(primaryKeyAttribute.getValue(), is("AS-set-attendees"));
        assertThat(whoisObject.getAttributes(), containsInAnyOrder(
                new Attribute("as-set", "AS-set-attendees", null, null, null),
                new Attribute("descr", "AS-set containing all attendees' ASNs.", null, null, null),
                new Attribute("tech-c", "TS1-TEST", null, "person", new Link("locator", "http://rest.db.ripe.net/lookup/test/person/TS1-TEST")),
                new Attribute("admin-c", "TS1-TEST", null, "person", new Link("locator", "http://rest.db.ripe.net/lookup/test/person/TS1-TEST")),
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

        final List<WhoisVersion> whoisVersions = subject.mapVersions(Lists.newArrayList(deleted), versionInfos);

        assertThat(whoisVersions, hasSize(3));
        final WhoisVersion deletedVersion = whoisVersions.get(0);
        assertThat(deletedVersion.getOperation(), nullValue());
        assertThat(deletedVersion.getRevision(), nullValue());
        assertThat(deletedVersion.getDeletedDate(), is(not(nullValue())));

        final WhoisVersion whoisVersion1 = whoisVersions.get(1);
        assertThat(whoisVersion1.getOperation(), is("ADD/UPD"));
        assertThat(whoisVersion1.getRevision(), is(3));
        assertThat(whoisVersion1.getDate(), is(not(nullValue())));

        final WhoisVersion whoisVersion2 = whoisVersions.get(2);
        assertThat(whoisVersion2.getOperation(), is("ADD/UPD"));
        assertThat(whoisVersion2.getRevision(), is(4));
        assertThat(whoisVersion2.getDate(), is(not(nullValue())));
    }

    @Test
    public void map_tags() {
        final List<WhoisTag> tags = subject.map(RpslObject.parse("mntner: TEST-MNT\nsource: TEST"),
                Lists.newArrayList(
                        new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("foo"), "foo data"),
                        new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("bar"), "bar data"),
                        new TagResponseObject(CIString.ciString("TEST-DBM"), CIString.ciString("barf"), "barf data"))).getTags();

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

    @Test
    public void buildAttribute_lacking_attributeType() {
        final Attribute attribute = subject.buildAttribute(
                new RpslAttribute("key", "value"),
                CIString.ciString("value"),
                "TEST");

        assertThat(attribute.getLink(), is(nullValue()));
        assertThat(attribute.getValue(), is("value"));
        assertThat(attribute.getName(), is("key"));
    }

    @Test
    public void buildAttribute_attributeType_given() {
        when(referencedTypeResolver.getReferencedType(AttributeType.NIC_HDL, CIString.ciString("TP-TEST"))).thenReturn(AttributeType.ROLE.getName());

        final Attribute attribute = subject.buildAttribute(
                new RpslAttribute(AttributeType.NIC_HDL, "TP-TEST"),
                CIString.ciString("TP-TEST"),
                "TEST");

        assertThat(attribute.getLink().toString(), is("locator: http://rest.db.ripe.net/lookup/TEST/role/TP-TEST"));
        assertThat(attribute.getName(), is("nic-hdl"));
        assertThat(attribute.getValue(), is("TP-TEST"));
    }
    */
}
