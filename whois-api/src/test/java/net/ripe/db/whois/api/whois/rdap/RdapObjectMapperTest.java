package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.whois.rdap.domain.Autnum;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.common.dao.VersionInfo;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import static net.ripe.db.whois.common.rpsl.ObjectType.INETNUM;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class RdapObjectMapperTest {
    private static final VersionLookupResult VERSION_LOOKUP_RESULT = new VersionLookupResult(Lists.<VersionInfo>newArrayList(new VersionInfo(true, 1, 1, 2345234523l, Operation.UPDATE)), INETNUM, "10.0.0.0 - 10.255.255.255");

    @Test
    public void ip() {
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
                "source:         TEST")), VERSION_LOOKUP_RESULT);

        assertThat(result.getHandle(), is("10.0.0.0 - 10.255.255.255"));
        assertThat(result.getEvents(), hasSize(2));
        assertThat(result.getEvents().get(0).getEventAction(), is("registration"));
        assertThat(result.getEvents().get(0).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getEvents().get(1).getEventAction(), is("last changed"));
        assertThat(result.getEvents().get(1).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getCountry(), is("NL"));
        assertThat(result.getEndAddress(), is("10.255.255.255"));
        assertThat(result.getIpVersion(), is("v4"));
        assertThat(result.getName(), is("RIPE-NCC"));
        assertThat(result.getParentHandle(), is(nullValue()));
        assertThat(result.getStartAddress(), is("10.0.0.0"));
        assertThat(result.getType(), is("OTHER"));
        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));
        assertThat(result.getRemarks().get(0).getDescription().get(0), is("some descr"));
    }

    @Test
    public void autnum() {
        final Autnum result = (Autnum) map((RpslObject.parse("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "member-of:      AS-TESTSET\n" +
                "descr:          description\n" +
                "import:         from AS1 accept ANY\n" +
                "export:         to AS1 announce AS2\n" +
                "default:        to AS1\n" +
                "mp-import:      afi ipv6.unicast from AS1 accept ANY\n" +
                "mp-export:      afi ipv6.unicast to AS1 announce AS2\n" +
                "mp-default:     to AS1\n" +
                "remarks:        remarkable\n" +
                "org:            ORG-NCC1-RIPE\n" +
                "admin-c:        AP1-TEST\n" +
                "tech-c:         AP1-TEST\n" +
                "notify:         noreply@ripe.net\n" +
                "mnt-lower:      UPD-MNT\n" +
                "mnt-routes:     UPD-MNT\n" +
                "mnt-by:         UPD-MNT\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST\n" +
                "password:       update")), VERSION_LOOKUP_RESULT);

        assertThat(result.getHandle(), is("AS102"));
        assertThat(result.getStartAutnum(), is(nullValue()));
        assertThat(result.getEndAutnum(), is(nullValue()));
        assertThat(result.getEvents(), hasSize(2));
        assertThat(result.getEvents().get(0).getEventAction(), is("registration"));
        assertThat(result.getEvents().get(0).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getEvents().get(1).getEventAction(), is("last changed"));
        assertThat(result.getEvents().get(1).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getName(), is("End-User-2"));
        assertThat(result.getType(), is("DIRECT ALLOCATION"));
        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));
        assertThat(result.getRemarks().get(0).getDescription().get(0), is("description"));
    }

    @Test
    public void domain() {
        final Domain result = (Domain) map((RpslObject.parse("" +
                "domain:          2.1.2.1.5.5.5.2.0.2.1.e164.arpa\n" +
                "descr:           enum domain\n" +
                "admin-c:         TEST-PN\n" +
                "tech-c:          TEST-PN\n" +
                "zone-c:          TEST-PN\n" +
                "nserver:         ns.1.net\n" +
                "mnt-by:          RIPE-NCC-MNT\n" +
                "changed:         test@ripe.net 20120505\n" +
                "source:          TEST\n" +
                "password:        update")), VERSION_LOOKUP_RESULT);

        assertThat(result.getHandle(), is("2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));
        assertThat(result.getLdhName(), is("2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));
        assertThat(result.getNameservers(), hasSize(1));
        assertThat(result.getNameservers().get(0).getLdhName(), is("ns.1.net"));
        assertThat(result.getEvents(), hasSize(2));
        assertThat(result.getEvents().get(0).getEventAction(), is("registration"));
        assertThat(result.getEvents().get(0).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getEvents().get(1).getEventAction(), is("last changed"));
        assertThat(result.getEvents().get(1).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));
        assertThat(result.getRemarks().get(0).getDescription().get(0), is("enum domain"));
    }

    @Test
    public void person() {
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
                "source:        TEST"), VERSION_LOOKUP_RESULT);

        assertThat(result.getEvents(), hasSize(0));
    }

    private Object map(final RpslObject rpslObject, final VersionLookupResult versionLookupResult) {
        return RdapObjectMapper.map("http://localhost/", rpslObject, versionLookupResult);
    }
}
