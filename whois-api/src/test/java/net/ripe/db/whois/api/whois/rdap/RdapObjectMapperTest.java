package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Queues;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class RdapObjectMapperTest {

    @Test
    public void ip() {
        Ip result = (Ip)build((RpslObject.parse(
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
                "source:         TEST")));

        assertThat(result.getHandle(), is("10.0.0.0 - 10.255.255.255"));
    }

    @Test
    public void person() {
        Entity result = (Entity)build(RpslObject.parse(
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
                "source:        TEST"));

        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventAction(), is("last changed"));
        assertThat(result.getEvents().get(0).getEventActor(), is("first@last.org"));

        DateTime checkDate = DateTimeFormat.forPattern("yyyyMMdd").parseLocalDate("20120220").toDateTime(new LocalTime(0, 0, 0));
        assertThat(result.getEvents().get(0).getEventDate().toString(), is(checkDate.toString()));
    }

    @Test
    public void person_no_changed_date() {
        Entity result = (Entity)build(RpslObject.parse(
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
                "changed:       first@last.org\n" +
                "source:        TEST"));

        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventAction(), is("last changed"));
        assertThat(result.getEvents().get(0).getEventActor(), is("first@last.org"));
        assertThat(result.getEvents().get(0).getEventDate(), is(nullValue()));
    }

    @Test
    public void domain() {
        final Domain result = (Domain)build(RpslObject.parse("" +
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
                "source:   TEST\n"));

        assertThat(result.getHandle(), is("31.12.202.in-addr.arpa"));

    }


    private Object build(final RpslObject... rpslObjects) {
        final RdapObjectMapper subject = new RdapObjectMapper("http://localhost/", "http://localhost/", Queues.newArrayDeque(Arrays.asList(rpslObjects)));
        return subject.build();
    }
}
