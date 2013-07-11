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
import org.junit.Ignore;
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
        assertThat(result.getEndAddress(), is("10.255.255.255/32"));
        assertThat(result.getIpVersion(), is("v4"));
        assertThat(result.getName(), is("RIPE-NCC"));
        assertThat(result.getParentHandle(), is(nullValue()));
        assertThat(result.getStartAddress(), is("10.0.0.0/32"));
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
        assertThat(result.getStartAutnum(), is(102l));
        assertThat(result.getEndAutnum(), is(102l));
        assertThat(result.getEvents(), hasSize(2));
        assertThat(result.getEvents().get(0).getEventAction(), is("registration"));
        assertThat(result.getEvents().get(0).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getEvents().get(1).getEventAction(), is("last changed"));
        assertThat(result.getEvents().get(1).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getName(), is("End-User-2"));
        assertThat(result.getType(), is("DIRECT ALLOCATION"));
        assertThat(result.getLinks(), hasSize(3));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("self"));          // TODO: is extra link correct?
        assertThat(result.getLinks().get(2).getRel(), is("copyright"));
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
        assertThat(result.getEvents().get(0).getEventActor(), is(nullValue()));
        assertThat(result.getEvents().get(1).getEventAction(), is("last changed"));
        assertThat(result.getEvents().get(1).getEventDate(), is((LocalDateTime.parse("2044-04-26T00:02:03.000"))));
        assertThat(result.getEvents().get(1).getEventActor(), is(nullValue()));
        assertThat(result.getLinks(), hasSize(3));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("self"));          // TODO: is extra link correct?
        assertThat(result.getLinks().get(2).getRel(), is("copyright"));
        assertThat(result.getRemarks().get(0).getDescription().get(0), is("enum domain"));
    }

    @Test
    public void domain_31_12_202_in_addr_arpa() {
        final Domain result = (Domain)map((RpslObject.parse("" +
                "domain:   31.12.202.in-addr.arpa\n" +
                "descr:    Test domain\n" +
                "admin-c:  TP1-TEST\n" +
                "tech-c:   TP1-TEST\n" +
                "zone-c:   TP1-TEST\n" +
                "notify:   notify@test.net.au\n" +
                "nserver:  ns1.test.com.au 10.0.0.1\n" +
                "nserver:  ns1.test.com.au 2001:10::1\n" +
                "nserver:  ns2.test.com.au 10.0.0.2\n" +
                "nserver:  ns2.test.com.au 2001:10::2\n" +
                "nserver:  ns3.test.com.au\n" +
                "ds-rdata: 52151 1 1 13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "ds-rdata: 17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78\n" +
                "ds-rdata: 17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e\n" +
                "changed:  test@test.net.au 20010816\n" +
                "changed:  test@test.net.au 20121121\n" +
                "mnt-by:   OWNER-MNT\n" +
                "source:   TEST\n")), VERSION_LOOKUP_RESULT);

        assertThat(result.getHandle(), is("31.12.202.in-addr.arpa"));
    }

    @Test
    public void domain_102_130_in_addr_arpa() {
        final Domain result = (Domain)map((RpslObject.parse("" +
                "domain:         102.130.in-addr.arpa\n" +
                "descr:          domain object for 130.102.0.0 - 130.102.255.255\n" +
                "country:        AU\n" +
                "admin-c:        HM53-AP\n" +
                "tech-c:         HM53-AP\n" +
                "zone-c:         HM53-AP\n" +
                "nserver:        NS1.UQ.EDU.AU\n" +
                "nserver:        NS2.UQ.EDU.AU\n" +
                "nserver:        NS3.UQ.EDU.AU\n" +
                "nserver:        ns4.uqconnect.net\n" +
                "notify:         hostmaster@uq.edu.au\n" +
                "mnt-by:         MAINT-AU-UQ\n" +
                "changed:        hm-changed@apnic.net 20031020\n" +
                "changed:        hm-changed@apnic.net 20040319\n" +
                "changed:        d.thomas@its.uq.edu.au 20070226\n" +
                "source:         APNIC\n")), VERSION_LOOKUP_RESULT);

        assertThat(result.getHandle(), is("102.130.in-addr.arpa"));
    }

    @Ignore("TODO ds-rdata syntax")
    @Test
    public void domain_29_12_202_in_addr_arpa() {
        final Domain result = (Domain)map((RpslObject.parse("" +
                "domain:         29.12.202.in-addr.arpa\n" +
                "descr:          zone for 202.12.29.0/24\n" +
                "admin-c:        NO4-AP\n" +
                "tech-c:         AIC1-AP\n" +
                "zone-c:         NO4-AP\n" +
                "nserver:        cumin.apnic.net\n" +
                "nserver:        tinnie.apnic.net\n" +
                "nserver:        tinnie.arin.net\n" +
                "ds-rdata:       55264 5 1 ( 8bb4b233cbcf8593c6f153fccd4d805179b972a4 )\n" +
                "ds-rdata:       55264 5 2 ( b44b18643775f9fdc76ee312667c2b350c1e02f3e43f8027f2c55777a429095a )\n" +
                "mnt-by:         MAINT-APNIC-IS-AP\n" +
                "changed:        hm-changed@apnic.net 20120504\n" +
                "changed:        hm-changed@apnic.net 20120508\n" +
                "source:         APNIC\n")), VERSION_LOOKUP_RESULT);

        assertThat(result.getHandle(), is("29.12.202.in-addr.arpa"));
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
        return RdapObjectMapper.map("http://localhost/", "http://localhost/", rpslObject, versionLookupResult, Lists.<RpslObject>newArrayList());
    }
}
