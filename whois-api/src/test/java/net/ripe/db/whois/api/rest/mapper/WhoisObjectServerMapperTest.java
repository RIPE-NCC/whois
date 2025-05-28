package net.ripe.db.whois.api.rest.mapper;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.ReferencedTypeResolver;
import net.ripe.db.whois.api.rest.SourceResolver;
import net.ripe.db.whois.api.rest.domain.Attribute;
import net.ripe.db.whois.api.rest.domain.Link;
import net.ripe.db.whois.api.rest.domain.Parameters;
import net.ripe.db.whois.api.rest.domain.WhoisObject;
import net.ripe.db.whois.api.rest.domain.WhoisVersion;
import net.ripe.db.whois.api.rest.search.AbuseContactSearch;
import net.ripe.db.whois.api.rest.search.ResourceHolderSearch;
import net.ripe.db.whois.common.dao.VersionDateTime;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.search.ManagedAttributeSearch;
import net.ripe.db.whois.query.domain.DeletedVersionResponseObject;
import net.ripe.db.whois.query.domain.VersionResponseObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class WhoisObjectServerMapperTest {
    private static final String BASE_URL = "http://localhost/lookup";

    @Mock
    private ReferencedTypeResolver referencedTypeResolver;
    @Mock
    private ResourceHolderSearch resourceHolderSearch;
    @Mock
    private AbuseContactSearch abuseContactSearch;
    @Mock
    private ManagedAttributeSearch managedAttributeSearch;
    @Mock
    private Parameters parameters;
    @Mock
    private SourceResolver sourceResolver;

    private WhoisObjectServerMapper whoisObjectServerMapper;
    private WhoisObjectMapper whoisObjectMapper;

    @BeforeEach
    public void setup() {
        whoisObjectMapper = new WhoisObjectMapper(BASE_URL, new AttributeMapper[]{
                new FormattedServerAttributeMapper(referencedTypeResolver, sourceResolver, BASE_URL),
                new FormattedClientAttributeMapper()
        });
        whoisObjectServerMapper = new WhoisObjectServerMapper(whoisObjectMapper, resourceHolderSearch, abuseContactSearch, managedAttributeSearch, Lists.newArrayList());
        lenient().when(parameters.getUnformatted()).thenReturn(Boolean.FALSE);
        lenient().when(sourceResolver.getSource(anyString(), any(CIString.class), anyString())).thenReturn("test");
    }

    @Test
    public void map_rpsl_mntner() throws Exception {
        lenient().when(referencedTypeResolver.getReferencedType(AttributeType.ADMIN_C, ciString("TP1-TEST"))).thenReturn("person");
        lenient().when(referencedTypeResolver.getReferencedType(AttributeType.AUTH, ciString("PGPKEY-28F6CD6C"))).thenReturn("key-cert");
        lenient().when(referencedTypeResolver.getReferencedType(AttributeType.MNT_BY, ciString("TST-MNT"))).thenReturn("mntner");

        final RpslObject rpslObject = RpslObject.parse(
                "mntner:      TST-MNT\n" +
                        "descr:       MNTNER for test\n" +
                        "admin-c:     TP1-TEST\n" +
                        "upd-to:      dbtest@ripe.net\n" +
                        "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ # test\n" +
                        "auth:        PGPKEY-28F6CD6C\n" +
                        "mnt-by:      TST-MNT\n" +
                        "source:      TEST\n");

        final WhoisObject whoisObject = whoisObjectMapper.map(rpslObject, FormattedServerAttributeMapper.class);

        assertThat(whoisObject.getType(), is("mntner"));
        assertThat(whoisObject.getSource().getId(), is("test"));
        assertThat(whoisObject.getLink().getType(), is("locator"));
        assertThat(whoisObject.getLink().getHref(), is("http://localhost/lookup/test/mntner/TST-MNT"));
        assertThat(whoisObject.getPrimaryKey(), hasSize(1));
        final Attribute primaryKeyAttribute = whoisObject.getPrimaryKey().get(0);
        assertThat(primaryKeyAttribute.getName(), is("mntner"));
        assertThat(primaryKeyAttribute.getValue(), is("TST-MNT"));
        assertThat(whoisObject.getAttributes(), contains(
                new Attribute("mntner", "TST-MNT", null, null, null, null),
                new Attribute("descr", "MNTNER for test", null, null, null, null),
                new Attribute("admin-c", "TP1-TEST", null, "person", Link.create("http://localhost/lookup/test/person/TP1-TEST"), null),
                new Attribute("upd-to", "dbtest@ripe.net", null, null, null, null),
                new Attribute("auth", "MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/", "test", null, null, null),
                new Attribute("auth", "PGPKEY-28F6CD6C", null, "key-cert", Link.create("http://localhost/lookup/test/key-cert/PGPKEY-28F6CD6C"), null),
                new Attribute("mnt-by", "TST-MNT", null, "mntner", Link.create("http://localhost/lookup/test/mntner/TST-MNT"), null),
                new Attribute("source", "TEST", null, null, null, null)
        ));
    }

    @Test
    public void map_rpsl_as_set_members_multiple_values() throws Exception {
        lenient().when(referencedTypeResolver.getReferencedType(eq(AttributeType.TECH_C), any(CIString.class))).thenReturn("person");
        lenient().when(referencedTypeResolver.getReferencedType(eq(AttributeType.ADMIN_C), any(CIString.class))).thenReturn("person");
        lenient().when(referencedTypeResolver.getReferencedType(eq(AttributeType.MEMBERS), any(CIString.class))).thenReturn("aut-num");
        lenient().when(referencedTypeResolver.getReferencedType(eq(AttributeType.MNT_BY), any(CIString.class))).thenReturn("mntner");


        final RpslObject rpslObject = RpslObject.parse("" +
                "as-set:    AS-set-attendees\n" +
                "descr:     AS-set containing all attendees' ASNs.\n" + // TODO: on transform map to &apos;
                "tech-c:    TS1-TEST\n" +
                "admin-c:   TS1-TEST\n" +
                "members:   as1,as2,as3\n" +
                "mnt-by:    TS1-MNT\n" +
                "source:    TEST");

        final WhoisObject whoisObject = whoisObjectMapper.map(rpslObject, FormattedServerAttributeMapper.class);

        assertThat(whoisObject.getType(), is("as-set"));
        assertThat(whoisObject.getSource().getId(), is("test"));
        assertThat(whoisObject.getLink().getType(), is("locator"));
        assertThat(whoisObject.getLink().getHref(), is("http://localhost/lookup/test/as-set/AS-set-attendees"));
        assertThat(whoisObject.getPrimaryKey(), hasSize(1));
        final Attribute primaryKeyAttribute = whoisObject.getPrimaryKey().get(0);
        assertThat(primaryKeyAttribute.getName(), is("as-set"));
        assertThat(primaryKeyAttribute.getValue(), is("AS-set-attendees"));
        assertThat(whoisObject.getAttributes(), containsInAnyOrder(
                new Attribute("as-set", "AS-set-attendees", null, null, null, null),
                new Attribute("descr", "AS-set containing all attendees' ASNs.", null, null, null, null),
                new Attribute("tech-c", "TS1-TEST", null, "person", Link.create("http://localhost/lookup/test/person/TS1-TEST"), null),
                new Attribute("admin-c", "TS1-TEST", null, "person", Link.create("http://localhost/lookup/test/person/TS1-TEST"), null),
                new Attribute("members", "as1", null, "aut-num", Link.create("http://localhost/lookup/test/aut-num/as1"), null),
                new Attribute("members", "as2", null, "aut-num", Link.create("http://localhost/lookup/test/aut-num/as2"), null),
                new Attribute("members", "as3", null, "aut-num", Link.create("http://localhost/lookup/test/aut-num/as3"), null),
                new Attribute("mnt-by", "TS1-MNT", null, "mntner", Link.create("http://localhost/lookup/test/mntner/TS1-MNT"), null),
                new Attribute("source", "TEST", null, null, null, null)
        ));
    }

    @Test
    public void map_versions() {
        final DeletedVersionResponseObject deleted = new DeletedVersionResponseObject(new VersionDateTime(LocalDateTime.now()), ObjectType.AUT_NUM, "AS102");

        final List<VersionResponseObject> versionInfos = Lists.newArrayList(
                new VersionResponseObject(2, Operation.UPDATE, 3, new VersionDateTime(LocalDateTime.now()), ObjectType.AUT_NUM, "AS102"),
                new VersionResponseObject(2, Operation.UPDATE, 4, new VersionDateTime(LocalDateTime.now()), ObjectType.AUT_NUM, "AS102"));

        final List<WhoisVersion> whoisVersions = whoisObjectServerMapper.mapVersions(Lists.newArrayList(deleted), versionInfos);

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
}
