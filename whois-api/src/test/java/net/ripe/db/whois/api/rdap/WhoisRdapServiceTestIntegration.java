package net.ripe.db.whois.api.rdap;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import net.ripe.db.whois.api.fulltextsearch.FullTextIndex;
import net.ripe.db.whois.api.rdap.domain.Action;
import net.ripe.db.whois.api.rdap.domain.Autnum;
import net.ripe.db.whois.api.rdap.domain.Domain;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Event;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.Nameserver;
import net.ripe.db.whois.api.rdap.domain.Notice;
import net.ripe.db.whois.api.rdap.domain.RdapObject;
import net.ripe.db.whois.api.rdap.domain.Remark;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.api.rdap.domain.SearchResult;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.support.TestWhoisLog;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.support.DateMatcher.isBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class WhoisRdapServiceTestIntegration extends AbstractRdapIntegrationTest {

    @Autowired
    FullTextIndex fullTextIndex;
    @Autowired
    TestWhoisLog queryLog;


    @BeforeAll
    public static void beforeClass() {
        System.setProperty("rdap.entity.max.results", "3");
    }

    @BeforeEach
    public void setup() {
        databaseHelper.addObject("" +
                "person:        Test Person\n" +
                "nic-hdl:       TP1-TEST\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z");
        databaseHelper.addObject("" +
                "mntner:        OWNER-MNT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        OWNER-MNT\n" +
                "referral-by:   OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.updateObject("" +
                "person:        Test Person\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "person:        Test Person2\n" +
                "address:       Test Address\n" +
                "phone:         +61-1234-1234\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       TP2-TEST\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "person:        Pauleth Palthen\n" +
                "address:       Singel 258\n" +
                "phone:         +31-1234567890\n" +
                "e-mail:        noreply@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "nic-hdl:       PP1-TEST\n" +
                "remarks:       remark\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "role:          First Role\n" +
                "address:       Singel 258\n" +
                "e-mail:        dbtest@ripe.net\n" +
                "admin-c:       PP1-TEST\n" +
                "tech-c:        PP1-TEST\n" +
                "nic-hdl:       FR1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "domain:        31.12.202.in-addr.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "notify:        notify@test.net.au\n" +
                "nserver:       ns1.test.com.au 10.0.0.1\n" +
                "nserver:       ns2.test.com.au 2001:10::2\n" +
                "ds-rdata:      52151 1 1 13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "ds-rdata:      17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78\n" +
                "ds-rdata:      17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "organisation:  ORG-TEST1-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "admin-c:       PP1-TEST\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "as-block:       AS100 - AS200\n" +
                "descr:          ARIN ASN block\n" +
                "org:            ORG-TEST1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        databaseHelper.addObject("" +
                "inetnum:        0.0.0.0 - 255.255.255.255\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv4 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        databaseHelper.addObject("" +
                "inet6num:       ::/0\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();
    }

    // inetnum

    // Ref. draft-ietf-weirds-json-response, section 5.9 "An Example"
    @Test
    public void lookup_inetnum_range_reserved() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.2.0 - 192.0.2.255"));
        assertThat(ip.getIpVersion(), is("v4"));
        assertThat(ip.getLang(), is("en"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("192.0.2.0"));
        assertThat(ip.getEndAddress(), is("192.0.2.255"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getType(), is("OTHER"));
        assertThat(ip.getObjectClassName(), is("ip network"));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));
        assertThat(ip.getStatus(), contains("reserved"));

        assertThat(ip.getPort43(), is("whois.ripe.net"));
        assertThat(ip.getRdapConformance(), hasSize(3));
        assertThat(ip.getRdapConformance(), containsInAnyOrder("rdap_level_0", "cidr0", "nro_rdap_profile_0"));


        final List<Remark> remarks = ip.getRemarks();
        assertThat(remarks, hasSize(1));
        assertThat(remarks.get(0).getDescription(), contains("TEST network"));

        final List<Event> events = ip.getEvents();
        assertThat(events, hasSize(2));

        final List<Notice> notices = ip.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(0).getDescription(), contains("This output has been filtered."));
        assertThat(notices.get(0).getLinks(), hasSize(0));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(1).getDescription(), contains("Objects returned came from source", "TEST"));
        assertThat(notices.get(1).getLinks(), hasSize(0));

        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/ip/192.0.2.0/24");
        assertCopyrightLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/192.0.2.0/24");
    }

    @Test
    public void lookup_inetnum_less_specific_active_status() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:      2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/192.0.0.255")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);


        assertThat(ip.getHandle(), is("192.0.0.0 - 192.255.255.255"));
        assertThat(ip.getIpVersion(), is("v4"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("192.0.0.0"));
        assertThat(ip.getEndAddress(), is("192.255.255.255"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getLang(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));
        assertThat(ip.getStatus(), contains("active"));
    }

    @Test
    public void lookup_inetnum_multiple_country_codes() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "country:      DE\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/192.0.0.255")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.0.0 - 192.255.255.255"));
        assertThat(ip.getIpVersion(), is("v4"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("192.0.0.0"));
        assertThat(ip.getEndAddress(), is("192.255.255.255"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getLang(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));

        final List<Notice> notices = ip.getNotices();
        assertThat(notices, hasSize(5));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Multiple country attributes found"));
        assertThat(notices.get(1).getDescription().get(0), is("There are multiple country attributes NL, DE in 192.0.0.0 - 192.255.255.255, but only the first country NL was returned."));
        assertThat(notices.get(2).getTitle(), is("Source"));
        assertThat(notices.get(3).getTitle(), is("Terms and Conditions"));
    }

    @Test
    public void lookup_inetnum_multiple_language_codes() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     EN\n" +
                "language:     DK\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/192.0.0.255")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.0.0 - 192.255.255.255"));
        assertThat(ip.getIpVersion(), is("v4"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getLang(), is("EN"));
        assertThat(ip.getStartAddress(), is("192.0.0.0"));
        assertThat(ip.getEndAddress(), is("192.255.255.255"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));

        final List<Notice> notices = ip.getNotices();
        assertThat(notices, hasSize(5));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Multiple language attributes found"));
        assertThat(notices.get(1).getDescription().get(0), is("There are multiple language attributes EN, DK in 192.0.0.0 - 192.255.255.255, but only the first language EN was returned."));
        assertThat(notices.get(2).getTitle(), is("Source"));
        assertThat(notices.get(3).getTitle(), is("Terms and Conditions"));
    }

    //

    @Test
    public void lookup_org_single_language_codes() {
        databaseHelper.addObject("" +
                "organisation:   ORG-AC1-TEST\n" +
                "org-name:       Acme Carpets\n" +
                "org-type:       OTHER\n" +
                "address:        Singel 258\n" +
                "e-mail:         bitbucket@ripe.net\n" +
                "descr:          Acme Carpet Organisation\n" +
                "remark:         some remark\n" +
                "phone:          +31 1234567\n" +
                "fax-no:         +31 98765432\n" +
                "geoloc:         52.375599 4.899902\n" +
                "language:       DK\n" +
                "admin-c:        TP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:        2022-08-14T11:48:28Z\n" +
                "last-modified:  2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        final Entity entity = createResource("entity/ORG-AC1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("ORG-AC1-TEST"));
        assertThat(entity.getLang(), is("DK"));

        // no notice for single language
        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));
        assertThat(notices.get(3).getTitle(), is("Whois Inaccuracy Reporting"));
    }

    @Test
    public void lookup_org_multiple_language_codes() {
        databaseHelper.addObject("" +
                "organisation:  ORG-LANG-TEST\n" +
                "org-name:      Organisation One\n" +
                "org-type:      LIR\n" +
                "language:      DK\n" +
                "language:      EN\n" +
                "descr:         Test organisation\n" +
                "address:       One Org Street\n" +
                "e-mail:        test@ripe.net\n" +
                "admin-c:       TP2-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "tech-c:        TP2-TEST\n" +
                "mnt-ref:       OWNER-MNT\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified: 2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/ORG-LANG-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("ORG-LANG-TEST"));
        assertThat(entity.getLang(), is("DK"));

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(5));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Multiple language attributes found"));
        assertThat(notices.get(1).getDescription().get(0), is("There are multiple language attributes DK, EN in ORG-LANG-TEST, but only the first language DK was returned."));
        assertThat(notices.get(2).getTitle(), is("Source"));
        assertThat(notices.get(3).getTitle(), is("Terms and Conditions"));

    }

    @Test
    public void lookup_entity_case_insensitive_person() {
        databaseHelper.addObject("" +
                "person:        Test Person case\n" +
                "nic-hdl:       gruk-RIPE\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Entity upperCaseEntity = createResource("entity/GRUK-RIPE")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(upperCaseEntity.getHandle(), equalTo("gruk-RIPE"));

        final Entity exactSearchEntity = createResource("entity/gruk-RIPE")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(exactSearchEntity.getHandle(), equalTo("gruk-RIPE"));

        final Entity mixedCaseEntity = createResource("entity/gRuk-RIpE")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(mixedCaseEntity.getHandle(), equalTo("gruk-RIPE"));
    }

    @Test
    public void lookup_inetnum_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("ip/193.0.0.0")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });
        final RdapObject error = notFoundException.getResponse().readEntity(RdapObject.class);
        assertThat(error.getErrorCode(), is(HttpStatus.NOT_FOUND_404));
        assertThat(error.getErrorTitle(), is("404 Not Found"));
        assertThat(error.getDescription().get(0), is("Requested object not found"));
    }

    @Test
    public void lookup_inetnum() {
        databaseHelper.addObject("" +
                "inetnum:      192.132.74.0 - 192.132.77.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        Ip ip = createResource("ip/192.132.75.165")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.132.74.0 - 192.132.77.255"));
        assertThat(ip.getIpVersion(), is("v4"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("192.132.74.0"));
        assertThat(ip.getEndAddress(), is("192.132.77.255"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getLang(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));

        assertThat(ip.getCidr0_cidrs().get(0).getV4prefix(), is("192.132.74.0"));
        assertThat(ip.getCidr0_cidrs().get(0).getLength(), is(23));
        assertThat(ip.getCidr0_cidrs().get(1).getV4prefix(), is("192.132.76.0"));
        assertThat(ip.getCidr0_cidrs().get(1).getLength(), is(23));

        assertThat(ip.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0", "nro_rdap_profile_0"));

        var notices = ip.getNotices();
        var inaccuracyNotice = notices.get(1);
        assertThat(inaccuracyNotice.getTitle(), is("Whois Inaccuracy Reporting"));
        assertThat(inaccuracyNotice.getDescription(), hasSize(1));
        assertThat(inaccuracyNotice.getDescription().get(0), is("If you see inaccuracies in the results, please visit:"));
        assertThat(inaccuracyNotice.getLinks(), hasSize(1));
        assertThat(inaccuracyNotice.getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/ip/192.132.75.165"));
        assertThat(inaccuracyNotice.getLinks().get(0).getRel(), is("inaccuracy-report"));
        assertThat(inaccuracyNotice.getLinks().get(0).getHref(), is("https://www.ripe.net/contact-form?topic=ripe_dbm&show_form=true"));
    }

    @Test
    public void lookup_inetnum_invalid_syntax_multislash() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("ip/192.0.0.0//32")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });
        assertThat(badRequestException.getResponse().readEntity(String.class), containsString("reason: Ambiguous URI empty segment"));
    }

    @Test
    public void lookup_inetnum_invalid_syntax() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("ip/invalid")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorStatus(badRequestException, 400);
        assertErrorDescription(badRequestException, "'invalid' is not an IP string literal.");
    }

    @Disabled("TODO: handle multiple mnt-by values")
    @Test
    public void lookup_inetnum_multiple_mntby() {
        databaseHelper.addObject("" +
                "mntner:         SECOND-MNT\n" +
                "descr:          Second Maintainer\n" +
                "admin-c:        TP1-TEST\n" +
                "upd-to:         noreply@ripe.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:         SECOND-MNT\n" +
                "created:        2011-07-28T00:35:42Z\n" +
                "last-modified:  2019-02-28T10:14:46Z\n" +
                "source:         TEST");
        databaseHelper.addObject("" +
                "inetnum:      192.132.74.0 - 192.132.77.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT,SECOND-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");
        ipTreeUpdater.rebuild();

        Ip ip = createResource("ip/192.132.75.165")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);
    }


    // inet6num

    @Test
    public void lookup_inet6num_with_prefix_length() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "language:       EN\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/2001:2002:2003::/48")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2001:2002:2003::/48"));
        assertThat(ip.getIpVersion(), is("v6"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getLang(), is("EN"));
        assertThat(ip.getStartAddress(), is("2001:2002:2003::"));
        assertThat(ip.getEndAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff"));
        assertThat(ip.getName(), is("RIPE-NCC"));
        assertThat(ip.getType(), is("ASSIGNED PA"));
        assertThat(ip.getObjectClassName(), is("ip network"));
        assertThat(ip.getParentHandle(), is("::/0"));
        assertThat(ip.getStatus(), contains("active"));

        assertThat(ip.getCidr0_cidrs(), hasSize(1));
        assertThat(ip.getCidr0_cidrs().get(0).getV6prefix(), is("2001:2002:2003::"));
        assertThat(ip.getCidr0_cidrs().get(0).getLength(), is(48));
        assertThat(ip.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0", "nro_rdap_profile_0"));

        assertThat(ip.getPort43(), is("whois.ripe.net"));
        assertThat(ip.getRdapConformance(), hasSize(3));
        assertThat(ip.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0", "nro_rdap_profile_0"));

        final List<Remark> remarks = ip.getRemarks();
        assertThat(remarks, hasSize(1));
        assertThat(remarks.get(0).getDescription(), contains("Private Network"));

        final List<Event> events = ip.getEvents();
        assertThat(events, hasSize(2));


        final List<Notice> notices = ip.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));

        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/ip/2001:2002:2003::/48");
        assertCopyrightLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/2001:2002:2003::/48");
    }

    @Test
    public void lookup_inet6num_less_specific() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/2001:2002:2003:2004::")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2001:2002:2003::/48"));
        assertThat(ip.getIpVersion(), is("v6"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("2001:2002:2003::"));
        assertThat(ip.getEndAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff"));
        assertThat(ip.getName(), is("RIPE-NCC"));
        assertThat(ip.getParentHandle(), is("::/0"));
    }

    @Test
    public void lookup_inet6num_single_country_code() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        FR\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "created:      2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/2001:2002:2003:2004::")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2001:2002:2003::/48"));
        assertThat(ip.getIpVersion(), is("v6"));
        assertThat(ip.getCountry(), is("FR"));
        assertThat(ip.getStartAddress(), is("2001:2002:2003::"));
        assertThat(ip.getEndAddress(), is("2001:2002:2003:ffff:ffff:ffff:ffff:ffff"));
        assertThat(ip.getName(), is("RIPE-NCC"));
        assertThat(ip.getParentHandle(), is("::/0"));
    }

    @Test
    public void lookup_inet6num_status_reserved() {
        final RpslObject inet6num = RpslObject.parse("" +
                "inet6num: ff00::/8\n" +
                "netname: RIPE-NCC\n" +
                "descr: some description\n" +
                "country: DK\n" +
                "admin-c: TP1-TEST\n" +
                "tech-c: TP1-TEST\n" +
                "status: ASSIGNED\n" +
                "mnt-by: OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source: TEST\n");
        databaseHelper.addObject(inet6num);
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/ff00::/8")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("ff00::/8"));
        assertThat(ip.getStatus(), contains("reserved"));
        assertThat(ip.getIpVersion(), is("v6"));
    }


    @Test
    public void lookup_inet6num_is_case_insensitive() {
        databaseHelper.addObject("" +
                "inet6num:       2001:200a::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");
        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/2001:200A::")      // uppercase key in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2001:200a::/48"));
        assertThat(ip.getIpVersion(), is("v6"));
        assertThat(ip.getStartAddress(), is("2001:200a::"));
        assertThat(ip.getEndAddress(), is("2001:200a:0:ffff:ffff:ffff:ffff:ffff"));
        assertThat(ip.getName(), is("RIPE-NCC"));
        assertThat(ip.getParentHandle(), is("::/0"));
    }

    @Test
    public void lookup_inet6num_not_found() {
        final Ip ip = createResource("ip/2001:2002:2003::/48")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("::/0"));
        assertThat(ip.getIpVersion(), is("v6"));
        assertThat(ip.getStatus(), contains("administrative"));
    }

    // person entity

    @Test
    public void lookup_person_entity() {
        final Entity entity = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertCommon(entity);
        assertThat(entity.getHandle(), equalTo("PP1-TEST"));
        assertThat(entity.getRoles(), hasSize(0));
        assertThat(entity.getEntitySearchResults(), hasSize(1));
        assertThat(entity.getVCardArray(), hasSize(2));
        assertThat(entity.getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entity.getVCardArray().get(1).toString(), equalTo("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Pauleth Palthen], " +
                "[kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31-1234567890], " +
                "[email, {type=email}, text, noreply@ripe.net]]"));

        assertThat(entity.getObjectClassName(), is("entity"));

        assertThat(entity.getRemarks(), hasSize(1));
        assertThat(entity.getRemarks().get(0).getDescription(), contains("remark"));

        final List<Event> events = entity.getEvents();
        assertThat(events, hasSize(2));
        assertThat(events.get(0).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.REGISTRATION));

        assertThat(events.get(1).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(1).getEventAction(), is(Action.LAST_CHANGED));

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));

        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/entity/PP1-TEST");
    }

    @Test
    public void lookup_entity_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("entity/ORG-BAD1-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested organisation not found: ORG-BAD1-TEST");
    }

    @Test
    public void lookup_entity_invalid_syntax() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entity/12345")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "Bad organisation or mntner syntax: 12345");
    }

    @Test
    public void lookup_entity_no_accept_header() {
        final Entity entity = createResource("entity/PP1-TEST")
                .request()
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("PP1-TEST"));
        assertCommon(entity);
    }

    @Test
    public void lookup_entity_logged_in_query_log() {
        createResource("entity/PP1-TEST")
                .request()
                .get(Entity.class);

        assertThat(queryLog.getMessages(), hasSize(1));
        assertThat(queryLog.getMessage(0), containsString(" PW-API-INFO <1+0+0> "));
        assertThat(queryLog.getMessage(0), containsString(" PP1-TEST"));
    }

    // role entity

    @Test
    public void lookup_role_entity() {
        final Entity entity = createResource("entity/FR1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertCommon(entity);
        assertThat(entity.getHandle(), equalTo("FR1-TEST"));
        assertThat(entity.getRoles(), hasSize(0));
        assertThat(entity.getVCardArray(), hasSize(2));
        assertThat(entity.getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entity.getVCardArray().get(1).toString(), equalTo("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, First Role], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[email, {type=email}, text, dbtest@ripe.net]]"));

        assertThat(entity.getEntitySearchResults(), hasSize(2));
        assertThat(entity.getEntitySearchResults().get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entity.getEntitySearchResults().get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entity.getEntitySearchResults().get(1).getHandle(), is("PP1-TEST"));
        assertThat(entity.getEntitySearchResults().get(1).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));

        final List<Event> events = entity.getEvents();
        assertThat(events, hasSize(2));
        assertThat(events.get(0).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.REGISTRATION));

        assertThat(events.get(1).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(1).getEventAction(), is(Action.LAST_CHANGED));

        assertThat(entity.getRemarks(), hasSize(0));

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));

        assertTnCNotice(notices.get(2),"https://rdap.db.ripe.net/entity/FR1-TEST");
    }

    @Test
    public void lookup_role_nested_object_links() {
        final Entity entity = createResource("entity/FR1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        final List<Entity> entities = entity.getEntitySearchResults();
        assertThat(entities.get(0).getLinks().get(0).getHref(), is("https://rdap.db.ripe.net/entity/OWNER-MNT"));
        assertThat(entities.get(0).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/entity/FR1-TEST"));
        assertThat(entities.get(0).getLinks().get(1).getValue(), is("http://www.ripe.net/data-tools/support/documentation/terms"));

        assertThat(entities.get(1).getLinks().get(0).getHref(), is("https://rdap.db.ripe.net/entity/PP1-TEST"));
        assertThat(entities.get(1).getLinks().get(0).getValue(), is("https://rdap.db.ripe.net/entity/FR1-TEST"));
        assertThat(entities.get(1).getLinks().get(1).getValue(), is("http://www.ripe" +
                ".net/data-tools/support/documentation/terms"));
    }
    // domain

    @Test
    public void lookup_domain_object() {
        final Domain domain = createResource("domain/31.12.202.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertCommon(domain);
        assertThat(domain.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("31.12.202.in-addr.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));

        assertThat(domain.getNameservers(), hasSize(2));
        assertThat(Lists.newArrayList(domain.getNameservers().get(0).getLdhName(), domain.getNameservers().get(1).getLdhName()),
                containsInAnyOrder("ns1.test.com.au", "ns2.test.com.au"));
        assertThat(Lists.newArrayList(domain.getNameservers().get(0).getLdhName(), domain.getNameservers().get(1).getLdhName()),
                containsInAnyOrder("ns1.test.com.au", "ns2.test.com.au"));
        assertThat(Lists.newArrayList(domain.getNameservers().get(0).getIpAddresses(), domain.getNameservers().get(1).getIpAddresses()),
                containsInAnyOrder(new Nameserver.IpAddresses(Lists.newArrayList("10.0.0.1/32"), null),
                        new Nameserver.IpAddresses(null, Lists.newArrayList("2001:10::2/128"))));

        assertThat(domain.getSecureDNS().isDelegationSigned(), is(Boolean.TRUE));
        assertThat(domain.getSecureDNS().getDsData(), hasSize(3));
        assertThat(domain.getSecureDNS().getDsData().get(0).getDigest(), is("13ee60f7499a70e5aadaf05828e7fc59e8e70bc1"));
        assertThat(domain.getSecureDNS().getDsData().get(0).getKeyTag(), is(52151L));
        assertThat(domain.getSecureDNS().getDsData().get(0).getAlgorithm(), is(1));
        assertThat(domain.getSecureDNS().getDsData().get(0).getDigestType(), is(1));
        assertThat(domain.getSecureDNS().getDsData().get(1).getDigest(), is("2e58131e5fe28ec965a7b8e4efb52d0a028d7a78"));
        assertThat(domain.getSecureDNS().getDsData().get(2).getDigest(), is("8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e"));

        assertThat(domain.getRemarks(), hasSize(1));
        assertThat(domain.getRemarks().get(0).getDescription(), contains("Test domain"));
        assertThat(domain.getNetwork().getHandle(), is("0.0.0.0 - 255.255.255.255"));

        final List<Event> events = domain.getEvents();
        assertThat(events, hasSize(2));
        assertThat(events.get(0).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.REGISTRATION));

        assertThat(events.get(1).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(1).getEventAction(), is(Action.LAST_CHANGED));

        final List<Entity> entities = domain.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(1).getHandle(), is("TP1-TEST"));

        final List<Notice> notices = domain.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/domain/31.12.202.in-addr.arpa");

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(1));
        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void lookup_domain_object_inet4num(){

        databaseHelper.addObject("" +
                "inetnum:       80.179.52.0 - 80.179.55.255\n" +
                "netname:       SANDBOX11470-IPv4-ALLOCATION\n" +
                "org:           ORG-TEST1-TEST\n" +
                "country:       EU\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2013-12-10T16:54:20Z\n" +
                "last-modified: 2013-12-10T16:54:20Z\n" +
                "source:        RIPE\n");

        databaseHelper.addObject("" +
                "domain:        52.179.80.in-addr.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "notify:        notify@test.net.au\n" +
                "nserver:       ns1.test.com.au 10.0.0.1\n" +
                "nserver:       ns2.test.com.au 2001:10::2\n" +
                "ds-rdata:      52151 1 1 13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "ds-rdata:      17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78\n" +
                "ds-rdata:      17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified: 2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        ipTreeUpdater.rebuild();

        final Domain domain = createResource("domain/52.179.80.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertCommon(domain);
        assertThat(domain.getHandle(), equalTo("52.179.80.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("52.179.80.in-addr.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork().getHandle(), is("80.179.52.0 - 80.179.55.255"));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(1));
        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void lookup_domain_object_inet6num(){

        databaseHelper.addObject("" +
                "inet6num:      2a00:2cce::/32\n" +
                "netname:       SANDBOX11470-IPv4-ALLOCATION\n" +
                "org:           ORG-TEST1-TEST\n" +
                "country:       EU\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        ALLOCATED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:       2013-12-10T16:54:20Z\n" +
                "last-modified: 2013-12-10T16:54:20Z\n" +
                "source:        RIPE\n");

        databaseHelper.addObject("" +
                "domain:        e.c.c.2.0.0.a.2.ip6.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "notify:        notify@test.net.au\n" +
                "nserver:       ns1.test.com.au 10.0.0.1\n" +
                "nserver:       ns2.test.com.au 2001:10::2\n" +
                "ds-rdata:      52151 1 1 13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "ds-rdata:      17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78\n" +
                "ds-rdata:      17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified: 2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        ipTreeUpdater.rebuild();

        final Domain domain = createResource("domain/e.c.c.2.0.0.a.2.ip6.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertCommon(domain);
        assertThat(domain.getHandle(), equalTo("e.c.c.2.0.0.a.2.ip6.arpa"));
        assertThat(domain.getLdhName(), equalTo("e.c.c.2.0.0.a.2.ip6.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork().getHandle(), is("2a00:2cce::/32"));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(1));
        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void lookup_domain_object_without_inetnum(){

        databaseHelper.addObject("" +
                "domain:        52.179.80.in-addr.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
                "notify:        notify@test.net.au\n" +
                "nserver:       ns1.test.com.au 10.0.0.1\n" +
                "nserver:       ns2.test.com.au 2001:10::2\n" +
                "ds-rdata:      52151 1 1 13ee60f7499a70e5aadaf05828e7fc59e8e70bc1\n" +
                "ds-rdata:      17881 5 1 2e58131e5fe28ec965a7b8e4efb52d0a028d7a78\n" +
                "ds-rdata:      17881 5 2 8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified: 2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        databaseHelper.deleteObject(RpslObject.parse("inetnum:        0.0.0.0 - 255.255.255.255\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv4 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         OTHER\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST"));

        ipTreeUpdater.rebuild();

        final Domain domain = createResource("domain/52.179.80.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertCommon(domain);
        assertThat(domain.getHandle(), equalTo("52.179.80.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("52.179.80.in-addr.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork(), is(nullValue()));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(1));
        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }
    @Test
    public void lookup_domain_object_is_case_insensitive() {
        final Domain domain = createResource("domain/31.12.202.IN-AddR.ARPA")       // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertCommon(domain);
        assertThat(domain.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("31.12.202.in-addr.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork().getHandle(), is("0.0.0.0 - 255.255.255.255"));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(1));
        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("test")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(RdapObject.class);
        });
        assertErrorTitle(notFoundException, "HTTP 404 Not Found");
        assertErrorStatus(notFoundException, 404);
    }


    @Test
    public void domain_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("domain/10.in-addr.arpa")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Domain.class);
        });
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorStatus(notFoundException, 404);
        assertErrorDescription(notFoundException, "Requested object not found");
    }

    @Test
    public void lookup_forward_domain() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("domain/ripe.net")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Domain.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "RIPE NCC does not support forward domain queries.");
    }

    // autnum

    @Test
    public void lookup_autnum_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("autnum/1")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested object not found");
    }

    @Test
    public void lookup_autnum_not_found_still_flat_conformance() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("autnum/1")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        final RdapObject rdapObject = notFoundException.getResponse().readEntity(RdapObject.class);
        assertThat(rdapObject.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "nro_rdap_profile_asn_flat_0"));
    }
    @Test
    public void lookup_autnum_invalid_syntax() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("autnum/XYZ")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "Invalid syntax (ASXYZ)");
    }

    @Test
    public void lookup_asBlock_bad_request() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("as-block/XYZ")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "unknown objectType");
    }

    @Test
    public void lookup_autnum_head_method() {
        final Response response = createResource("autnum/102").request().head();

        assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode()));
    }

    @Test
    public void lookup_autnum_not_found_head_method() {
        final Response response = createResource("autnum/1").request().head();

        assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode()));
    }

    @Test
    public void lookup_single_autnum() {
        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(autnum.getHandle(), equalTo("AS102"));
        assertThat(autnum.getStartAutnum(), is(102L));
        assertThat(autnum.getEndAutnum(), is(102L));
        assertThat(autnum.getName(), equalTo("AS-TEST"));
        assertThat(autnum.getObjectClassName(), is("autnum"));
        assertThat(autnum.getStatus(), contains("active"));

        final List<Event> events = autnum.getEvents();
        assertThat(events, hasSize(2));
        assertThat(events.get(0).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(0).getEventAction(), is(Action.REGISTRATION));

        assertThat(events.get(1).getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(events.get(1).getEventAction(), is(Action.LAST_CHANGED));

        final List<Entity> entities = autnum.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(1).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(1).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));

        assertCopyrightLink(autnum.getLinks(), "https://rdap.db.ripe.net/autnum/102");

        final List<Notice> notices = autnum.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/autnum/102");

        final List<Remark> remarks = autnum.getRemarks();
        assertThat(remarks, hasSize(1));
        assertThat(remarks.get(0).getDescription().get(0), is("A single ASN"));

        assertThat("nro_rdap_profile_asn_flat_0", is(autnum.getRdapConformance().get(0)));
    }

    @Test
    public void lookup_reserved_autnum() {
        databaseHelper.addObject("" +
                "aut-num:       AS64496\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Autnum autnum = createResource("autnum/64496")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(autnum.getObjectClassName(), is("autnum"));
        assertThat(autnum.getHandle(), equalTo("AS64496"));

        assertThat(autnum.getStatus(), contains("reserved"));
    }


    @Test
    public void lookup_autnum_with_rdap_json_content_type() {
        final Response response = createResource("autnum/102")
                .request("application/rdap+json")
                .get();

        assertThat(response.getMediaType(), is(new MediaType("application", "rdap+json")));
        final String entity = response.readEntity(String.class);
        assertThat(entity, containsString("\"handle\" : \"AS102\""));
        assertThat(entity, containsString("rdapConformance\" : [ \"nro_rdap_profile_asn_flat_0\", \"cidr0\", \"rdap_level_0\", \"nro_rdap_profile_0\" ]"));
    }

    @Test
    public void lookup_autnum_with_application_json_content_type() {
        final Response response = createResource("autnum/102")
                .request("application/json")
                .get();

        assertThat(response.getMediaType(), is(new MediaType("application", "rdap+json")));
        final String entity = response.readEntity(String.class);
        assertThat(entity, containsString("\"handle\" : \"AS102\""));
        assertThat(entity,
                containsString("rdapConformance\" : [ \"nro_rdap_profile_asn_flat_0\", \"cidr0\", \"rdap_level_0\", \"nro_rdap_profile_0\" ]"));
    }

    @Test
    public void lookup_autnum_within_block() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("autnum/1500")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not found");
        assertErrorDescription(notFoundException, "Redirect URI not found");
    }

    @Test
    public void lookup_autnum_abuse_contact_from_org() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 358\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "e-mail:        work@test.com\n" +
                "e-mail:        personal@test.com\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "organisation:  ORG-TO2-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       AB-TEST\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.updateObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TO2-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        final List<Entity> entities = autnum.getEntitySearchResults();
        assertThat(entities, hasSize(4));

        assertThat(entities.get(0).getHandle(), is("ORG-TO2-TEST"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(1).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(1).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(2).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(2).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));
        assertThat(entities.get(3).getHandle(), is("AB-TEST"));
        assertThat(entities.get(3).getRoles(), contains(Role.ABUSE));
        assertThat(entities.get(3).getVCardArray(), hasSize(2));
        assertThat(entities.get(3).getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entities.get(3).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Abuse Contact], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 358}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678], " +
                "[email, {type=email}, text, work@test.com], " +
                "[email, {type=email}, text, personal@test.com], " +
                "[email, {type=abuse}, text, abuse@test.net]]"));
    }

    @Test
    public void lookup_autnum_irt_mnt_vcard() {
        databaseHelper.addObject("" +
            "irt: irt-IRT1\n" +
            "address: Street 1\n" +
            "e-mail: test@ripe.net\n" +
            "admin-c: TP1-TEST\n" +
            "tech-c: TP1-TEST\n" +
            "auth: MD5-PW \\$1\\$fU9ZMQN9\\$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
            "mnt-by: OWNER-MNT\n" +
            "source: TEST\n");

        databaseHelper.updateObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "mnt-irt:       irt-IRT1\n"  +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        final List<Entity> entities = autnum.getEntitySearchResults();
        assertThat(entities, hasSize(3));

        assertThat(entities.get(0).getHandle(), is("irt-IRT1"));
        assertThat(entities.get(0).getRoles(), contains(Role.ABUSE));
        assertThat(entities.get(0).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, irt-IRT1], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Street 1}, text, [, , , , , , ]], " +
                "[email, {type=email}, text, test@ripe.net]]"));

        assertThat(entities.get(1).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(1).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(1).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, OWNER-MNT], " +
                "[kind, {}, text, individual]]"));

        assertThat(entities.get(2).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(2).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));
        assertThat(entities.get(2).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Person], " +
                "[kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]"));

    }
    @Test
    public void lookup_autnum_has_abuse_contact_object() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 358\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "e-mail:        work@test.com\n" +
                "e-mail:        personal@test.com\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.updateObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "abuse-c:       AB-TEST\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        final List<Entity> entities = autnum.getEntitySearchResults();
        assertThat(entities, hasSize(4));

        assertThat(entities.get(0).getHandle(), is("ORG-TEST1-TEST"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(1).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(2).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(3).getHandle(), is("AB-TEST"));
        assertThat(entities.get(3).getRoles(), contains(Role.ABUSE));
        assertThat(entities.get(3).getVCardArray(), hasSize(2));
        assertThat(entities.get(3).getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entities.get(3).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Abuse Contact], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 358}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678], " +
                "[email, {type=email}, text, work@test.com], " +
                "[email, {type=email}, text, personal@test.com], " +
                "[email, {type=abuse}, text, abuse@test.net]]"));
    }

    @Test
    public void lookup_autnum_has_invalid_abuse_contact() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 358\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "e-mail:        work@test.com\n" +
                "e-mail:        personal@test.com\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.updateObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "abuse-c:       AB-TEST\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO abuse_email (address, status, created_at) values (?, ?, ?)", "abuse@test.net", "SUSPECT", LocalDateTime.now()
        );

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(autnum.getRemarks().size(), is(1));
        assertThat(
                autnum.getRemarks().get(0).getDescription(),
                contains("Abuse contact for 'AS102' is 'abuse@test.net'\n" +
                        "Abuse-mailbox validation failed. Please refer to ORG-TEST1-TEST for further information.\n") );
    }

    @Test
    public void lookup_autnum_has_invalid_abuse_contact_should_add_object_remarks_description() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 358\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "e-mail:        work@test.com\n" +
                "e-mail:        personal@test.com\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.updateObject("" +
                "aut-num:       AS102\n" +
                "as-name:       AS-TEST\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "descr:       test description\n" +
                "remarks:       test remarks\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "abuse-c:       AB-TEST\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.getInternalsTemplate().update(
                "INSERT INTO abuse_email (address, status, created_at) values (?, ?, ?)", "abuse@test.net", "SUSPECT", LocalDateTime.now()
        );

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertThat(autnum.getRemarks().size(), is(2));
        assertThat(
                autnum.getRemarks().get(0).getDescription(),
                contains("Abuse contact for 'AS102' is 'abuse@test.net'\n" +
                        "Abuse-mailbox validation failed. Please refer to ORG-TEST1-TEST for further information.\n") );

        assertThat(
                autnum.getRemarks().get(1).getDescription().get(0), is("test description"));
        assertThat(
                autnum.getRemarks().get(1).getDescription().get(1), is("test remarks"));

    }
    // general

    @Test
    public void lookup_invalid_type() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("unknown/example.com")
                    .request("application/rdap+json")
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "unknown objectType");
    }

    @Test
    public void multiple_modification_gives_correct_events() {
        final String response = syncupdate(
                "aut-num:   AS102\n" +
                        "as-name:   AS-TEST\n" +
                        "descr:     Modified ASN\n" +
                        "admin-c:   TP1-TEST\n" +
                        "tech-c:    TP1-TEST\n" +
                        "mnt-by:    OWNER-MNT\n" +
                        "source:    TEST\n" +
                        "created:         2022-08-14T11:48:28Z\n" +
                        "last-modified:   2022-10-25T12:22:39Z\n" +
                        "password: test");
        assertThat(response, containsString("Modify SUCCEEDED: [aut-num] AS102"));

        final Autnum autnum = createResource("autnum/102")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        final List<Event> events = autnum.getEvents();
        assertThat(events, hasSize(2));

        assertThat(events.get(0).getEventAction(), is(Action.REGISTRATION));
        assertThat(events.get(0).getEventDate(), isBefore(LocalDateTime.now()));

        assertThat(events.get(1).getEventAction(), is(Action.LAST_CHANGED));
        assertThat(events.get(1).getEventDate(), isBefore(LocalDateTime.now()));
    }

    @Test
    public void lookup_as_block_when_no_autnum_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("autnum/103")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested object not found");
    }

    @Test
    public void lookup_inetnum_abuse_contact_as_vcard() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "organisation:  ORG-TO2-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       AB-TEST\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TO2-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/192.0.0.128")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        final List<Entity> entities = ip.getEntitySearchResults();
        assertThat(entities, hasSize(4));

        assertThat(entities.get(0).getHandle(), is("ORG-TO2-TEST"));
        assertThat(entities.get(0).getRoles().get(0), is(Role.REGISTRANT));

        assertThat(entities.get(2).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(2).getRoles(), contains(Role.TECHNICAL));

        assertThat(entities.get(1).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(1).getRoles().get(0), is(Role.REGISTRANT));

        assertThat(entities.get(3).getHandle(), is("AB-TEST"));
        assertThat(entities.get(3).getRoles(), contains(Role.ABUSE));
        assertThat(entities.get(3).getVCardArray(), hasSize(2));
        assertThat(entities.get(3).getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entities.get(3).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Abuse Contact], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678], " +
                "[email, {type=abuse}, text, abuse@test.net]]"));
    }


    @Test
    public void lookup_inetnum_multiple_remarks() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 258\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AB-TEST\n" +
                "abuse-mailbox: abuse@test.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "organisation:  ORG-TO2-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "abuse-c:       AB-TEST\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "descr:        TEST1 network\n" +
                "remarks:        TEST network remark\n" +
                "remarks:        TEST1 network remark\n" +
                "org:          ORG-TO2-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/192.0.0.128")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getRemarks().size(), is(1));
        assertThat(ip.getRemarks().get(0).getDescription().size(), is(4));

        assertThat(ip.getRemarks().get(0).getDescription().get(0), is("TEST network"));
        assertThat(ip.getRemarks().get(0).getDescription().get(1), is("TEST1 network"));
        assertThat(ip.getRemarks().get(0).getDescription().get(2), is("TEST network remark"));
        assertThat(ip.getRemarks().get(0).getDescription().get(3), is("TEST1 network remark"));
    }
    // organisation entity

    @Test
    public void lookup_org_entity_handle() {
        final Entity response = createResource("entity/ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getAutnums(), is(empty()));
        assertThat(response.getNetworks(), is(empty()));
    }

    @Test
    public void lookup_org_autnum_entity_handle() {
        databaseHelper.addObject("" +
                "aut-num:       AS64496\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Entity response = createResource("entity/ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getNetworks(), is(empty()));
        assertThat(response.getAutnums().get(0).getName(), equalTo("AS-TEST"));

        assertThat(response.getRemarks().size(), is(1));

        assertThat(response.getRemarks().get(0).getDescription().get(0), is("Drugs and gambling"));
        assertThat(response.getRemarks().get(0).getDescription().get(1), is("Nice to deal with generally"));
    }

    @Test
    public void lookup_org_inetnum_entity_handle() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        final Entity response = createResource("entity/ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getAutnums(), is(empty()));
        assertThat(response.getNetworks().get(0).getName(), equalTo("TEST-NET-NAME"));
    }

    @Test
    public void lookup_org_inet6num_entity_handle() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "country:        NL\n" +
                "language:       EN\n" +
                "tech-c:         TP1-TEST\n" +
                "status:         ASSIGNED PA\n" +
                "mnt-by:         OWNER-MNT\n" +
                "mnt-lower:      OWNER-MNT\n" +
                "org:            ORG-TEST1-TEST\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        final Entity response = createResource("entity/ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getAutnums(), is(empty()));
        assertThat(response.getNetworks().get(0).getName(), equalTo("RIPE-NCC"));
    }

    @Test
    public void lookup_org_inetnum_autnum_entity_handle() {
        databaseHelper.addObject("" +
                "aut-num:       AS64496\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.192.0 - 109.111.223.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        final Entity response = createResource("entity/ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));

        assertThat(response.getAutnums().get(0).getName(), equalTo("AS-TEST"));
        assertThat(response.getNetworks().get(0).getName(), equalTo("TEST-NET-NAME"));
    }
    @Test
    public void lookup_org_max_inetnum_handle() {
        databaseHelper.addObject("" +
                "aut-num:       AS64496\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.193.192 - 109.111.193.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.192.0 - 109.111.223.255\n" +
                "netname:      TEST-NET-NAME-TOP-LEVEL\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        final Entity response = createResource("entity/org-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), equalTo("ORG-TEST1-TEST"));
        assertThat(response.getAutnums(), hasSize(1));

        assertThat(response.getAutnums().get(0).getName(), equalTo("AS-TEST"));
        assertThat(response.getNetworks().get(0).getName(), equalTo("TEST-NET-NAME-TOP-LEVEL"));
    }

    @Test
    public void lookup_org_max_inetnum_with_legacy_handle() {
        databaseHelper.addObject("" +
                "aut-num:       AS64496\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.193.192 - 109.111.193.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.192.0 - 109.111.223.255\n" +
                "netname:      TEST-NET-NAME-TOP-LEVEL\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.193.0 - 109.111.200.255\n" +
                "netname:      TEST-NET-NAME-LEGACY\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       LEGACY\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        final Entity response = createResource("entity/org-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(response.getHandle(), is("ORG-TEST1-TEST"));
        assertThat(response.getAutnums(), hasSize(1));

        assertThat(response.getAutnums().get(0).getName(), is("AS-TEST"));
        assertThat(response.getNetworks().get(0).getName(), is("TEST-NET-NAME-TOP-LEVEL"));
    }

    @Test
    public void lookup_org_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("entity/ORG-NONE-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested organisation not found: ORG-NONE-TEST");
    }

    @Test
    public void lookup_org_error_correct_conformance() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("entity/ORG-NONE-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        final RdapObject rdapObject = notFoundException.getResponse().readEntity(RdapObject.class);
        assertThat(rdapObject.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0",
                "nro_rdap_profile_0"));
    }
    @Test
    public void lookup_org_invalid_syntax() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entity/ORG-INVALID")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "Bad organisation or mntner syntax: ORG-INVALID");
    }

    @Test
    public void lookup_mntner_entity() {
        final Entity entity = createResource("entity/OWNER-MNT")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("OWNER-MNT"));

        assertThat(entity.getHandle(), is("OWNER-MNT"));
        final List<Object> vCardArray = entity.getVCardArray();
        assertThat(vCardArray.get(0).toString(), is("vcard"));
        assertThat(vCardArray.get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, OWNER-MNT], " +
                "[kind, {}, text, individual]]"));

        final List<Entity> entities = entity.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(0).getVCardArray().get(0), is("vcard"));
        assertThat(entities.get(0).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, OWNER-MNT], " +
                "[kind, {}, text, individual]]"));

        assertThat(entities.get(1).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(1).getRoles(), contains(Role.ADMINISTRATIVE));
        assertThat(entities.get(1).getVCardArray().get(0), is("vcard"));
        assertThat(entities.get(1).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Test Person], " +
                "[kind, {}, text, individual], " +
                "[adr, {label=Singel 258}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678]]"));

        assertThat(entity.getLinks(), hasSize(2));
        assertThat(entity.getLinks().get(0).getRel(), is("self"));
        assertThat(entity.getLinks().get(1).getRel(), is("copyright"));

        assertThat(entity.getEvents(), hasSize(2));
        assertThat(entity.getEvents().get(0).getEventAction(), is(Action.REGISTRATION));
        assertThat(entity.getEvents().get(1).getEventAction(), is(Action.LAST_CHANGED));

        assertThat(entity.getPort43(), is("whois.ripe.net"));
    }

    @Test
    public void lookup_for_primary_key_for_entity() {
        databaseHelper.addObject("" +
                "mntner:        AZRT\n" +
                "descr:         Owner Maintainer\n" +
                "admin-c:       TP1-TEST\n" +
                "upd-to:        noreply@ripe.net\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        OWNER-MNT\n" +
                "referral-by:   OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "role:          AZRT ABUSE\n" +
                "address:       Singel\n" +
                "e-mail:        dbtest@ripe.net\n" +
                "admin-c:       PP1-TEST\n" +
                "tech-c:        PP1-TEST\n" +
                "nic-hdl:       FR2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "role:          AZRT OPS\n" +
                "address:       Singel\n" +
                "e-mail:        dbtest@ripe.net\n" +
                "admin-c:       PP1-TEST\n" +
                "tech-c:        PP1-TEST\n" +
                "nic-hdl:       FR3-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        Entity entity = createResource("entity/AZRT")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("AZRT"));
    }

    @Test
    public void lookup_org_entity() {
        databaseHelper.addObject("" +
                "organisation:  ORG-ONE-TEST\n" +
                "org-name:      Organisation One\n" +
                "org-type:      LIR\n" +
                "descr:         Test organisation\n" +
                "address:       One Org Street\n" +
                "e-mail:        test@ripe.net\n" +
                "language:      EN\n" +
                "admin-c:       TP2-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "tech-c:        TP2-TEST\n" +
                "mnt-ref:       OWNER-MNT\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/ORG-ONE-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertCommon(entity);
        assertThat(entity.getHandle(), equalTo("ORG-ONE-TEST"));
        assertThat(entity.getRoles(), hasSize(0));
        assertThat(entity.getLang(), is("EN"));
        assertThat(entity.getObjectClassName(), is("entity"));

        assertThat(entity.getEvents(), hasSize(2));
        final Event registrationEvent = entity.getEvents().get(0);
        assertThat(registrationEvent.getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(registrationEvent.getEventAction(), equalTo(Action.REGISTRATION));
        assertThat(registrationEvent.getEventActor(), is(nullValue()));

        final Event lastUpdateEvent = entity.getEvents().get(1);
        assertThat(lastUpdateEvent.getEventDate(), isBefore(LocalDateTime.now()));
        assertThat(lastUpdateEvent.getEventAction(), equalTo(Action.LAST_CHANGED));
        assertThat(lastUpdateEvent.getEventActor(), is(nullValue()));


        assertThat(entity.getEntitySearchResults(), hasSize(3));
        final List<Entity> entities = entity.getEntitySearchResults();
        Collections.sort(entities);
        assertThat(entities.get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(1).getHandle(), is("TP1-TEST"));
        assertThat(entities.get(1).getRoles(), contains(Role.TECHNICAL));
        assertThat(entities.get(2).getHandle(), is("TP2-TEST"));
        assertThat(entities.get(2).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL));

        assertThat(entity.getVCardArray(), hasSize(2));
        assertThat(entity.getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entity.getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Organisation One], " +
                "[kind, {}, text, org], " +
                "[adr, {label=One Org Street}, text, [, , , , , , ]], " +
                "[email, {type=email}, text, test@ripe.net]]"));

        assertCopyrightLink(entity.getLinks(), "https://rdap.db.ripe.net/entity/ORG-ONE-TEST");

        assertThat(entity.getRemarks(), hasSize(1));
        assertThat(entity.getRemarks().get(0).getDescription(), contains("Test organisation"));

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/entity/ORG-ONE-TEST");
    }

    // search

    // search - domain

    @Test
    public void search_domain_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            fullTextIndex.rebuild();
            createResource("domains?name=ripe.net")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested object not found: ripe.net");
    }

    @Test
    public void search_domain_exact_match() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("domains?name=31.12.202.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("31.12.202.in-addr.arpa"));
    }

    @Test
    public void search_domain_is_case_insensitive() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("domains?name=31.12.202.IN-AddR.arpa")     // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getDomainSearchResults().get(0).getHandle(), equalTo("31.12.202.in-addr.arpa"));
    }

    // search - nameservers

    @Test
    public void search_nameservers_not_found() {
        final ServerErrorException serverErrorException = assertThrows(ServerErrorException.class, () -> {
            fullTextIndex.rebuild();
            createResource("nameservers?name=ns1.ripe.net")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(serverErrorException, 501);
        assertErrorTitle(serverErrorException, "501 Not Implemented");
        assertErrorDescription(serverErrorException, "Nameserver not supported");
    }

    @Test
    public void lookup_nameserver_not_found() {
        final ServerErrorException serverErrorException = assertThrows(ServerErrorException.class, () -> {
            createResource("nameserver/test")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(serverErrorException, 501);
        assertErrorTitle(serverErrorException, "501 Not Implemented");
        assertErrorDescription(serverErrorException, "Nameserver not supported");
    }

    // search - entities - person

    @Test
    public void search_entity_person_by_name() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=Test%20Person")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP1-TEST"));
    }

    @Test
    public void search_entity_person_object_deleted_before_index_updated() {
        final RpslObject person = RpslObject.parse("person: Lost Person\nnic-hdl: LP1-TEST\nsource: TEST");
        databaseHelper.addObject(person);
        fullTextIndex.rebuild();
        databaseHelper.deleteObject(person);

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("entities?fn=Lost%20Person")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested object not found: Lost Person");
    }

    @Test
    public void search_entity_person_by_name_is_case_insensitive() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=tESt%20PeRSOn")       // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP1-TEST"));
    }

    @Test
    public void search_entity_person_umlaut() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl:TP3-TEST\ncreated:2022-08-14T11:48:28Z\nlast-modified:2022-10-25T12:22:39Z\nsource: TEST");
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=Tëst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP3-TEST"));
    }

    @Test
    public void search_entity_person_umlaut_latin1_encoded() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl:TP3-TEST\ncreated:2022-08-14T11:48:28Z\nlast-modified:2022-10-25T12:22:39Z\nsource: TEST");
        fullTextIndex.rebuild();

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("entities?fn=T%EBst%20Person3")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescriptionContains(notFoundException, "st Person3");
    }

    @Test
    public void search_entity_person_umlaut_utf8_encoded() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl:TP3-TEST\ncreated:2022-08-14T11:48:28Z\nlast-modified:2022-10-25T12:22:39Z\nsource: TEST");
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=T%C3%ABst%20Person3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP3-TEST"));
    }

    @Test
    public void search_entity_person_umlaut_substitution() {
        databaseHelper.addObject("person: Tëst Person3\nnic-hdl: TP3-TEST\nsource: TEST");
        fullTextIndex.rebuild();

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("entities?fn=Test%20Person3")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested object not found: Test Person3");
    }

    @Test
    public void search_entity_person_by_name_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            fullTextIndex.rebuild();
            createResource("entities?fn=Santa%20Claus")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested object not found: Santa Claus");
    }

    @Test
    public void search_entity_person_by_handle() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=TP2-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP2-TEST"));
    }

    @Test
    public void search_entity_person_by_handle_is_case_insensitive() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=Tp2-tESt")       // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("TP2-TEST"));
    }

    @Test
    public void search_entity_person_by_handle_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            fullTextIndex.rebuild();
            createResource("entities?handle=XYZ-TEST")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "404 Not Found");
        assertErrorDescription(notFoundException, "Requested object not found: XYZ-TEST");
    }

    // search - entities - role

    @Test
    public void search_entity_role_by_name() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=FR*-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("FR1-TEST"));
    }

    @Test
    public void search_entity_role_by_handle() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=F*st%20Role")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("FR1-TEST"));
    }

    // search - entities - organisation

    @Test
    public void search_entity_organisation_by_name() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=organisation")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_name_is_case_insensitive() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?fn=ORGanisAtioN")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_organisation_by_handle() {
        fullTextIndex.rebuild();

        final SearchResult response = createResource("entities?handle=ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(response.getEntitySearchResults().get(0).getHandle(), equalTo("ORG-TEST1-TEST"));
    }

    @Test
    public void search_entity_without_query_params() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entities")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "The server is not able to process the request");
    }

    @Test
    public void search_entity_both_fn_and_handle_query_params() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entities?fn=XXXX&handle=YYYY")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "The server is not able to process the request");
    }

    @Test
    public void search_entity_empty_name() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entities?fn=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "Empty search term");
    }

    @Test
    public void search_entity_empty_handle() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entities?handle=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "400 Bad Request");
        assertErrorDescription(badRequestException, "Empty search term");
    }

    @Test
    public void search_entity_multiple_object_response() {
        fullTextIndex.rebuild();

        final SearchResult result = createResource("entities?handle=*TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        assertThat(
                result.getEntitySearchResults()
                        .stream()
                        .map(Entity::getHandle)
                        .collect(Collectors.toList()),
                containsInAnyOrder("TP1-TEST", "TP2-TEST", "PP1-TEST", "FR1-TEST", "ORG-TEST1-TEST"));
        assertThat(
                result.getEntitySearchResults()
                        .stream()
                        .filter(entity -> entity.getHandle().equals("ORG-TEST1-TEST"))
                        .map(RdapObject::getNotices)
                        .flatMap(Collection::stream)
                        .map(Notice::getTitle)
                        .collect(Collectors.toList()),
                containsInAnyOrder("Source", "Filtered", "Whois Inaccuracy Reporting"));
        assertThat(result.getNotices(), hasSize(1));
        assertThat(result.getNotices().get(0).getTitle(), is("Terms and Conditions"));
    }

    // Cross-origin requests

    @Test
    public void cross_origin_preflight_request_from_apps_db_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .options();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://apps.db.ripe.net"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is(nullValue()));
    }

    @Test
    public void cross_origin_preflight_request_from_outside_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "http://www.foo.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .options();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("http://www.foo.net"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is(nullValue()));
    }

    @Test
    public void cross_origin_preflight_post_request_from_outside_ripe_net_is_not_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "http://www.foo.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.POST)
                .options();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is(nullValue()));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS), is(nullValue()));
        assertThat(response.getHeaderString(HttpHeaders.ALLOW), containsString("GET"));
        assertThat(response.getHeaderString(HttpHeaders.ALLOW), not(containsString("POST")));
    }

    @Test
    public void cross_origin_preflight_get_request_from_outside_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "http://www.foo.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET)
                .options();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("http://www.foo.net"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS).split("[,]"), arrayContainingInAnyOrder("GET","OPTIONS"));
    }

    @Test
    public void cross_origin_preflight_options_request_from_outside_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "http://www.foo.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET)
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("http://www.foo.net"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is(nullValue()));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_from_apps_db_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://apps.db.ripe.net"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is(nullValue()));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_from_outside_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "https://www.foo.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://www.foo.net"));
        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS), is(nullValue()));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_preflight_request_malformed_origin() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "?invalid?")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .options();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("?invalid?"));
    }

    @Test
    public void cross_origin_get_request_malformed_origin() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "?invalid?")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("?invalid?"));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_host_and_port() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "https://www.foo.net:8443")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://www.foo.net:8443"));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_without_origin() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
    }

    @Test
    public void get_help_response() {
        final RdapObject help = createResource("help")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(RdapObject.class);

        assertThat(help.getPort43(), is("whois.ripe.net"));
        assertThat(help.getRdapConformance(), hasSize(4));
        assertThat(help.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0", "nro_rdap_profile_0",
                "nro_rdap_profile_asn_flat_0"));

        final List<Notice> notices = help.getNotices();
        assertThat(notices, hasSize(1));
        assertTnCNotice(notices.get(0), "https://rdap.db.ripe.net/help");

        assertCopyrightLink(help.getLinks(), "https://rdap.db.ripe.net/help");
    }

    private void assertCommon(RdapObject object) {
        assertThat(object.getPort43(), is("whois.ripe.net"));
        assertThat(object.getRdapConformance(), hasSize(3));
        assertThat(object.getRdapConformance(), containsInAnyOrder("rdap_level_0", "cidr0", "nro_rdap_profile_0"));
    }

    private void assertCopyrightLink(final List<Link> links, final String value) {
        assertThat(links, hasSize(2));
        Collections.sort(links);

        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));

        assertThat(links.get(1).getRel(), is("self"));
        assertThat(links.get(1).getValue(), is(value));
        assertThat(links.get(1).getHref(), is(value));
    }

    private void assertTnCNotice(final Notice notice, final String value) {
        assertThat(notice.getTitle(), is("Terms and Conditions"));
        assertThat(notice.getDescription(), contains("This is the RIPE Database query service. The objects are in RDAP format."));
        assertThat(notice.getLinks().get(0).getHref(), is("https://apps.db.ripe.net/docs/HTML-Terms-And-Conditions"));

        assertThat(notice.getLinks().get(0).getRel(), is("terms-of-service"));
        assertThat(notice.getLinks().get(0).getHref(), is("https://apps.db.ripe.net/docs/HTML-Terms-And-Conditions"));
        assertThat(notice.getLinks().get(0).getType(), is("application/pdf"));
        assertThat(notice.getLinks().get(0).getValue(), is(value));
    }

}
