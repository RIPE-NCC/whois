package net.ripe.db.whois.api.whois.mapper;

import net.ripe.db.whois.api.whois.domain.Attribute;
import net.ripe.db.whois.api.whois.domain.WhoisObject;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class WhoisObjectClientMapperTest {

    private static final String BASE_URL = "http://rest.db.ripe.net/lookup";

    private WhoisObjectClientMapper mapper;

    @Before
    public void setup() {
        mapper = new WhoisObjectClientMapper(BASE_URL);
    }

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

        final WhoisObject whoisObject = mapper.map(rpslObject, true);

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
                new Attribute("admin-c", "TP1-TEST", null, null, null),
                new Attribute("upd-to", "dbtest@ripe.net", null, null, null),
                new Attribute("auth", "MD5-PW", "Filtered", null, null),
                new Attribute("auth", "PGPKEY-28F6CD6C", null, null, null),
                new Attribute("mnt-by", "TST-MNT", null, null, null),
                new Attribute("referral-by", "TST-MNT", null, null, null),
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

        final WhoisObject whoisObject = mapper.map(rpslObject);

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
                new Attribute("tech-c", "TS1-TEST", null, null, null),
                new Attribute("admin-c", "TS1-TEST", null, null, null),
                new Attribute("members", "as1", null, null, null),
                new Attribute("members", "as2", null, null, null),
                new Attribute("members", "as3", null, null, null),
                new Attribute("mnt-by", "TS1-MNT", null, null, null),
                new Attribute("changed", "hostmaster@ripe.net 20121115", null, null, null),
                new Attribute("source", "TEST", null, null, null)
        ));
    }
}
