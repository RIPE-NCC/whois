package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static net.ripe.db.whois.common.rpsl.ObjectType.PERSON;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class RdapObjectMapperTest {

    @Test
    public void ip() {
        final VersionLookupResult versionLookupResult = new VersionLookupResult(Lists.<VersionInfo>newArrayList(new VersionInfo(true, 1, 1, 2345234523l, Operation.UPDATE)), INETNUM, "10.0.0.0 - 10.255.255.255");
        final Ip result = (Ip) map((RpslObject.parse("" +
                "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          some descr\n" +
                "country:        NL\n" +
                "admin-c:        TP1-TEST\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         TST-MNT\n" +
                "mnt-lower:      TST-MNT\n" +
                "mnt-domains:    TST-MNT\n" +
                "mnt-routes:     TST-MNT\n" +
                "mnt-irt:        irt-IRT1\n" +
                "notify:         notify@test.net\n" +
                "org:            ORG-TOL1-TEST\n" +
                "changed:        ripe@test.net 20120101\n" +
                "source:         TEST")), versionLookupResult);

        assertThat(result.getHandle(), is("10.0.0.0 - 10.255.255.255"));
        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventAction(), is("ADD"));
        assertThat(result.getEvents().get(0).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
    }

    @Test
    public void person() {
        final VersionLookupResult versionLookupResult = new VersionLookupResult(Lists.<VersionInfo>newArrayList(new VersionInfo(true, 1, 1, 2345234523l, Operation.UPDATE)), PERSON, "FL1-TEST");
        final Entity result = (Entity) map(RpslObject.parse("" +
                "person:        First Last\n" +
                "address:       Singel 258\n" +
                "phone:         +31 20 123456\n" +
                "fax-no:        +31 20 123457\n" +
                "e-mail:        first@last.org\n" +
                "org:           ORG-TOL1-TEST\n" +
                "nic-hdl:       FL1-TEST\n" +
                "remarks:       remark\n" +
                "notify:        first@last.org\n" +
                "abuse-mailbox: first@last.org\n" +
                "mnt-by:        TST-MNT\n" +
                "changed:       first@last.org 20120220\n" +
                "source:        TEST"), versionLookupResult);

        assertThat(result.getEvents(), hasSize(0));
    }

    private Object map(final RpslObject rpslObject, final VersionLookupResult versionLookupResult) {
        return RdapObjectMapper.map("http://localhost/", rpslObject, versionLookupResult);
    }
}
