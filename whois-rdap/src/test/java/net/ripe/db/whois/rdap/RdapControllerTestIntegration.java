package net.ripe.db.whois.rdap;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.jayway.jsonpath.JsonPath;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAcceptableException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.minidev.json.JSONArray;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.support.TestWhoisLog;
import net.ripe.db.whois.rdap.domain.Action;
import net.ripe.db.whois.rdap.domain.Autnum;
import net.ripe.db.whois.rdap.domain.Domain;
import net.ripe.db.whois.rdap.domain.Entity;
import net.ripe.db.whois.rdap.domain.Event;
import net.ripe.db.whois.rdap.domain.Ip;
import net.ripe.db.whois.rdap.domain.Link;
import net.ripe.db.whois.rdap.domain.LinkRelationType;
import net.ripe.db.whois.rdap.domain.Nameserver;
import net.ripe.db.whois.rdap.domain.Notice;
import net.ripe.db.whois.rdap.domain.RdapObject;
import net.ripe.db.whois.rdap.domain.Redaction;
import net.ripe.db.whois.rdap.domain.RelationType;
import net.ripe.db.whois.rdap.domain.Remark;
import net.ripe.db.whois.rdap.domain.Role;
import net.ripe.db.whois.rdap.domain.SearchResult;
import org.eclipse.jetty.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.ripe.db.whois.common.rpsl.AttributeType.COUNTRY;
import static net.ripe.db.whois.common.rpsl.AttributeType.LANGUAGE;
import static net.ripe.db.whois.common.support.DateMatcher.isBefore;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RdapControllerTestIntegration extends AbstractRdapIntegrationTest {

    @Autowired
    TestWhoisLog queryLog;

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
                "mbrs-by-ref:   OWNER-MNT\n" +
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
                "notify:       test@ripe.net\n" +
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
                "inetnum:        0.0.0.0 - 255.255.255.255\n" +
                "netname:        IANA-BLK\n" +
                "descr:          The whole IPv4 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED UNSPECIFIED\n" +
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
                "status:         ALLOCATED-BY-RIR\n" +
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
        assertThat(ip.getRdapConformance(), hasSize(7));
        assertThat(ip.getRdapConformance(), containsInAnyOrder("rdap_level_0", "cidr0", "nro_rdap_profile_0",
                "redacted", "geofeed1", "rirSearch1", "ips"));


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
        assertResourceCopyrightLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/192.0.2.0/24");
    }

    @Test
    public void lookup_inetnum_without_geofeed_then_conformance() {
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
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
    }

    @Test
    public void lookup_inetnum_geoFeed_attribute() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "geofeed:      https://test.net/geo/test.csv\n" +
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
        assertGeoFeedLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/192.0.2.0/24");
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
    }

    @Test
    public void lookup_inetnum_geoFeed_descr() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "descr:        GeoFeed https://test.net/geo/test.csv\n" +
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
        assertGeoFeedLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/192.0.2.0/24");
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
    }

    @Test
    public void lookup_inetnum_geoFeed_remarks() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "remarks:      GeoFeed https://test.net/geo/test.csv\n" +
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
        assertGeoFeedLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/192.0.2.0/24");
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
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
        assertThat(ip.getStartAddress(), is("192.0.0.0"));
        assertThat(ip.getEndAddress(), is("192.255.255.255"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getLang(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));
        assertThat(ip.getCountry(), is("NL"));

        assertMultipleValuesRedaction(ip, "$", COUNTRY, "NL, DE");
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

        assertMultipleValuesRedaction(ip, "$", LANGUAGE, "EN, DK");

    }

    // organisation

    @Test
    public void lookup_org_single_language_codes() {
        databaseHelper.addObject("" +
                "organisation:   ORG-AC1-TEST\n" +
                "org-name:       Acme Carpets\n" +
                "org-type:       OTHER\n" +
                "address:        Singel 258\n" +
                "descr:          Acme Carpet Organisation\n" +
                "remarks:         some remark\n" +
                "phone:          +31 1234567\n" +
                "fax-no:         +31 98765432\n" +
                "geoloc:         52.375599 4.899902\n" +
                "language:       DK\n" +
                "admin-c:        TP1-TEST\n" +
                "country:        NL\n"+
                "mnt-by:         OWNER-MNT\n" +
                "created:        2022-08-14T11:48:28Z\n" +
                "last-modified:  2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        final Entity entity = createResource("entity/ORG-AC1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getHandle(), equalTo("ORG-AC1-TEST"));
        assertThat(entity.getLang(), is("DK"));

        assertThat(entity.getRedacted().size(), is(0));
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

        assertMultipleValuesRedaction(entity, "$", LANGUAGE, "DK, EN");

        final List<Notice> notices = entity.getNotices();
        assertThat(notices, hasSize(4));
        Collections.sort(notices);
        assertThat(notices.get(0).getTitle(), is("Filtered"));
        assertThat(notices.get(1).getTitle(), is("Source"));
        assertThat(notices.get(2).getTitle(), is("Terms and Conditions"));

    }

    @Test
    public void lookup_org_single_language_codes_no_redaction(){
        databaseHelper.addObject("" +
                "organisation:  ORG-TEST2-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "country:      NL\n" +
                "language:      NL\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/ORG-TEST2-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getRedacted().size(), is(0));
        assertCommon(entity);
    }

    @Test
    public void lookup_org_with_abuse_contact_entity() {
        databaseHelper.addObject("" +
                "role:          Abuse Contact\n" +
                "address:       Singel 358\n" +
                "phone:         +31 6 12345678\n" +
                "nic-hdl:       AC1-TEST\n" +
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
                "abuse-c:       AC1-TEST\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "e-mail:        org@test.com\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/ORG-TO2-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getEntitySearchResults(), hasSize(2));
        assertThat(entity.getEntitySearchResults().get(0).getHandle(), is("OWNER-MNT"));
        assertThat(entity.getEntitySearchResults().get(0).getRoles().get(0).name(), is("REGISTRANT"));
        assertThat(entity.getEntitySearchResults().get(1).getHandle(), is("AC1-TEST"));
        assertThat(entity.getEntitySearchResults().get(1).getRoles().get(0).name(), is("ABUSE"));
    }


    @Test
    public void lookup_domain_multiple_redaction_inside_network() {

        databaseHelper.addObject("" +
                "inetnum:       80.179.52.0 - 80.179.55.255\n" +
                "netname:       SANDBOX11470-IPv4-ALLOCATION\n" +
                "org:           ORG-TEST1-TEST\n" +
                "country:       EU\n" +
                "country:       NZ\n" +
                "language:       EN\n" +
                "language:       NL\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "tech-c:       TP2-TEST\n" +
                "created:       2013-12-10T16:54:20Z\n" +
                "last-modified: 2013-12-10T16:54:20Z\n" +
                "source:        RIPE\n");

        databaseHelper.addObject("" +
                "domain:        52.179.80.in-addr.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
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

        assertMultipleValuesRedaction(domain, "$.network", COUNTRY, "EU, NZ");
        assertMultipleValuesRedaction(domain, "$.network", LANGUAGE, "EN, NL");
    }

    @Test
    public void lookup_org_multiple_attr_redactions_in_networks() {
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.255.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "country:      BR\n" +
                "language:      EN\n" +
                "language:      NL\n" +
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
        assertMultipleValuesRedaction(response, "$.networks[?(@.handle=='192.0.0.0 - 192.255.255.255')]", LANGUAGE, "EN, NL");

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
    public void lookup_inetnum_return_administrative_range() {
        final Ip ip = createResource("ip/193.0.0.0")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);

        assertThat(ip.getHandle(), is("193.0.0.0/8"));
        assertThat(ip.getStartAddress(), is("193.0.0.0"));
        assertThat(ip.getEndAddress(), is("193.255.255.255"));
        assertThat(ip.getName(), is("RIPE-NCC-MANAGED-ADDRESS-BLOCK"));
        assertThat(ip.getType(), is("ALLOCATED UNSPECIFIED"));
        assertThat(ip.getCountry(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));
        assertThat(ip.getStatus().getFirst(), is("administrative"));
    }

    @Test
    public void lookup_inetnum_administrative_range_exact() {

        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/002/8")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2.0.0.0/8"));
        assertThat(ip.getStartAddress(), is("2.0.0.0"));
        assertThat(ip.getEndAddress(), is("2.255.255.255"));
        assertThat(ip.getName(), is("RIPE-NCC-MANAGED-ADDRESS-BLOCK"));
        assertThat(ip.getType(), is("ALLOCATED UNSPECIFIED"));
        assertThat(ip.getCountry(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));
        assertThat(ip.getStatus().getFirst(), is("administrative"));

    }

    @Test
    public void lookup_bogon_range_not_found() {

        ipTreeUpdater.rebuild();

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("ip/0.0.0.0/8")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });
        final RdapObject error = notFoundException.getResponse().readEntity(RdapObject.class);
        assertThat(error.getErrorCode(), is(HttpStatus.NOT_FOUND_404));
        assertThat(error.getErrorTitle(), is("Not Found"));
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

        assertThat(ip.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0", "nro_rdap_profile_0",
                "redacted", "geofeed1", "rirSearch1", "ips"));

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
    public void lookup_inetnum_invalid_syntax_empty_segment() {
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

        assertThat(badRequestException.getResponse().readEntity(String.class), containsString("Ambiguous URI empty segment"));
    }

    @Test
    public void lookup_inetnum_invalid_syntax_encoded_forward_slash() {
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

        final Ip ip = createResource("ip/192.0.0.0%2F32")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.0.0 - 192.255.255.255"));
        assertThat(ip.getCountry(), is("NL"));
        assertThat(ip.getStartAddress(), is("192.0.0.0"));
        assertThat(ip.getEndAddress(), is("192.255.255.255"));
        assertThat(ip.getName(), is("TEST-NET-NAME"));
        assertThat(ip.getLang(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("0.0.0.0 - 255.255.255.255"));
        assertThat(ip.getStatus(), contains("active"));
    }

    @Test
    public void lookup_inetnum_empty_ip() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("ip/")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });

        assertThat(badRequestException.getResponse().readEntity(String.class), containsString("empty lookup term"));
    }

    @Test
    public void lookup_inetnum_invalid_syntax() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("ip/invalid")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });
        assertErrorTitle(badRequestException, "Bad Request");
        assertErrorStatus(badRequestException, 400);
        assertErrorDescription(badRequestException, "'invalid' is not an IP string literal.");
    }

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

        assertThat(ip.getEntitySearchResults().get(0).getHandle(), is("OWNER-MNT"));
        assertThat(ip.getEntitySearchResults().get(0).getRoles().get(0).name(), is("REGISTRANT"));
        assertThat(ip.getEntitySearchResults().get(1).getHandle(), is("SECOND-MNT"));
        assertThat(ip.getEntitySearchResults().get(1).getRoles().get(0).name(), is("REGISTRANT"));

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

        assertThat(ip.getPort43(), is("whois.ripe.net"));
        assertThat(ip.getRdapConformance(), hasSize(7));
        assertThat(ip.getRdapConformance(), containsInAnyOrder("rdap_level_0", "cidr0", "nro_rdap_profile_0",
                "redacted", "geofeed1", "rirSearch1", "ips"));

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
        assertResourceCopyrightLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/2001:2002:2003::/48");
    }

    @Test
    public void lookup_inet6num_without_geoFeed_then_conformance() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "geofeed:        https://test.net/geo/test.csv\n" +
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
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
    }
    @Test
    public void lookup_inet6num_geoFeed_attribute() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "geofeed:        https://test.net/geo/test.csv\n" +
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
        assertGeoFeedLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/2001:2002:2003::/48");
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
    }

    @Test
    public void lookup_inet6num_geoFeed_remarks() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "remarks:        Geofeed https://test.net/geo/test.csv\n" +
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
        assertGeoFeedLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/2001:2002:2003::/48");
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
    }

    @Test
    public void lookup_inet6num_geoFeed_remarks_case_insensitive() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "remarks:        geoFeed https://test.net/geo/test.csv\n" +
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
        assertGeoFeedLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/2001:2002:2003::/48");
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
    }

    @Test
    public void lookup_inet6num_geoFeed_descr() {
        databaseHelper.addObject("" +
                "inet6num:       2001:2002:2003::/48\n" +
                "netname:        RIPE-NCC\n" +
                "descr:          Private Network\n" +
                "descr:          Geofeed https://test.net/geo/test.csv\n" +
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
        assertGeoFeedLink(ip.getLinks(), "https://rdap.db.ripe.net/ip/2001:2002:2003::/48");
        assertThat(ip.getRdapConformance(), hasItem(RdapConformance.GEO_FEED_1.getValue()));
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
    public void lookup_inet6num_administrative() {
        final Ip ip = createResource("ip/2001:2002:2003::/48")
                        .request(MediaType.APPLICATION_JSON_TYPE)
                        .get(Ip.class);

        assertThat(ip.getHandle(), is("2001:2000::/19"));
        assertThat(ip.getStartAddress(), is("2001:2000::"));
        assertThat(ip.getEndAddress(), is("2001:3fff:ffff:ffff:ffff:ffff:ffff:ffff"));
        assertThat(ip.getName(), is("RIPE-NCC-MANAGED-ADDRESS-BLOCK"));
        assertThat(ip.getType(), is("ALLOCATED UNSPECIFIED"));
        assertThat(ip.getCountry(), is(nullValue()));
        assertThat(ip.getParentHandle(), is("::/0"));
        assertThat(ip.getStatus().getFirst(), is("administrative"));
    }

    @Test
    public void inetnum_inside_range_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            RestTest.target(getPort(), String.format("rdap/%s", "ip/192.0.0.1"))
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(String.class);
            fail();
        });
        final RdapObject error = notFoundException.getResponse().readEntity(RdapObject.class);
        assertThat(error.getErrorCode(), is(HttpStatus.NOT_FOUND_404));
        assertThat(error.getErrorTitle(), is("Not Found"));
        assertThat(error.getDescription().get(0), is("Requested object not found"));
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
        assertErrorTitle(notFoundException, "Not Found");
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
        assertErrorTitle(badRequestException, "Bad Request");
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
    public void lookup_xml_accept_returns_not_acceptable() {
        assertThrows(NotAcceptableException.class, () -> {
            createResource("entity/ORG-LANG-TEST")
                    .request(MediaType.APPLICATION_XML)
                    .get(Entity.class);
        });
    }

    @Test
    public void lookup_entity_logged_in_query_log() {
        createResource("entity/PP1-TEST")
                .request()
                .get(Entity.class);

        MatcherAssert.assertThat(queryLog.getMessages(), hasSize(1));
        MatcherAssert.assertThat(queryLog.getMessage(0), containsString(" PW-API-INFO <1+0+0> "));
        MatcherAssert.assertThat(queryLog.getMessage(0), containsString(" PP1-TEST"));
    }

    // role entity

    @Test
    public void lookup_role_entity() {
        final Entity entity = createResource("entity/FR1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getPort43(), is("whois.ripe.net"));
        assertThat(entity.getRdapConformance(), hasSize(4));
        assertThat(entity.getRdapConformance(), containsInAnyOrder("rdap_level_0", "cidr0", "nro_rdap_profile_0",
                "redacted"));

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

        assertTnCNotice(notices.get(2), "https://rdap.db.ripe.net/entity/FR1-TEST");
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

        assertDomain(domain);
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
        assertThat(links, hasSize(7));
        assertRelationLinks(links);
        assertThat(links.getLast().getRel(), is("copyright"));
        assertThat(links.getLast().getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void lookup_domain_object_inet4num() {

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

        assertDomain(domain);
        assertThat(domain.getHandle(), equalTo("52.179.80.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("52.179.80.in-addr.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork().getHandle(), is("80.179.52.0 - 80.179.55.255"));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(7));
        assertRelationLinks(links);
        assertThat(links.getLast().getRel(), is("copyright"));
        assertThat(links.getLast().getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void lookup_domain_object_inet6num() {

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

        assertDomain(domain);
        assertThat(domain.getHandle(), equalTo("e.c.c.2.0.0.a.2.ip6.arpa"));
        assertThat(domain.getLdhName(), equalTo("e.c.c.2.0.0.a.2.ip6.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork().getHandle(), is("2a00:2cce::/32"));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(7));
        assertThat(links.getLast().getRel(), is("copyright"));
        assertThat(links.getLast().getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void lookup_domain_object_without_inetnum() {

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

        assertDomain(domain);
        assertThat(domain.getHandle(), equalTo("52.179.80.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("52.179.80.in-addr.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork(), is(nullValue()));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(7));
        assertRelationLinks(links);

        assertThat(links.getLast().getRel(), is("copyright"));
        assertThat(links.getLast().getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void lookup_domain_object_is_case_insensitive() {
        final Domain domain = createResource("domain/31.12.202.IN-AddR.ARPA")       // mixed case in request
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertDomain(domain);
        assertThat(domain.getHandle(), equalTo("31.12.202.in-addr.arpa"));
        assertThat(domain.getLdhName(), equalTo("31.12.202.in-addr.arpa."));
        assertThat(domain.getObjectClassName(), is("domain"));
        assertThat(domain.getNetwork().getHandle(), is("0.0.0.0 - 255.255.255.255"));

        final List<Link> links = domain.getLinks();
        assertThat(links, hasSize(7));
        assertRelationLinks(links);
        assertThat(links.getLast().getRel(), is("copyright"));
        assertThat(links.getLast().getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
    }

    @Test
    public void not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("test")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(RdapObject.class);
        });
        assertErrorTitle(notFoundException, "Not Found");
        assertErrorStatus(notFoundException, 404);
    }


    @Test
    public void domain_not_found() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("domain/10.in-addr.arpa")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Domain.class);
        });
        assertErrorTitle(notFoundException, "Not Found");
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
        assertErrorTitle(badRequestException, "Bad Request");
        assertErrorDescription(badRequestException, "RIPE NCC does not support forward domain queries.");
    }

    @Test
    public void lookup_e164_domain_object_then_not_implemented() {
        final ServerErrorException notImplementedException = assertThrows(ServerErrorException.class, () -> {
            createResource("domain/1.2.3.4.e164.arpa")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Domain.class);
        });

        assertErrorStatus(notImplementedException, 501);
        assertErrorTitle(notImplementedException, "Not Implemented");
        assertErrorDescription(notImplementedException, "Support for ENUM zone (e164.arpa) is currently not implemented");
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
        assertErrorTitle(notFoundException, "Not Found");
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
                "nro_rdap_profile_0", "nro_rdap_profile_asn_flat_0", "redacted"));
    }

    @Test
    public void lookup_autnum_invalid_syntax() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("autnum/XYZ")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "Bad Request");
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
        assertErrorTitle(badRequestException, "Bad Request");
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

        assertResourceCopyrightLink(autnum.getLinks(), "https://rdap.db.ripe.net/autnum/102");

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
        assertThat(entity, containsString("\"links\" : [ {\n" +
                "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"rel\" : \"rdap-up\",\n" +
                "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-up/AS102\",\n" +
                "    \"type\" : \"application/rdap+json\"\n" +
                "  }, {\n" +
                "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"rel\" : \"rdap-up rdap-active\",\n" +
                "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-up/AS102?status=active\",\n" +
                "    \"type\" : \"application/rdap+json\"\n" +
                "  }, {\n" +
                "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"rel\" : \"rdap-down\",\n" +
                "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-down/AS102\",\n" +
                "    \"type\" : \"application/rdap+json\"\n" +
                "  }, {\n" +
                "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"rel\" : \"rdap-top\",\n" +
                "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-top/AS102\",\n" +
                "    \"type\" : \"application/rdap+json\"\n" +
                "  }, {\n" +
                "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"rel\" : \"rdap-top rdap-active\",\n" +
                "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-top/AS102?status=active\",\n" +
                "    \"type\" : \"application/rdap+json\"\n" +
                "  }, {\n" +
                "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"rel\" : \"rdap-bottom\",\n" +
                "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-bottom/AS102\",\n" +
                "    \"type\" : \"application/rdap+json\"\n" +
                "  }, {\n" +
                "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"href\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                "    \"type\" : \"application/rdap+json\"\n" +
                "  }, {\n" +
                "    \"value\" : \"http://www.ripe.net/data-tools/support/documentation/terms\",\n" +
                "    \"rel\" : \"copyright\",\n" +
                "    \"href\" : \"http://www.ripe.net/data-tools/support/documentation/terms\"\n" +
                "  } ]"));
        assertThat(entity, containsString("[ \"nro_rdap_profile_asn_flat_0\", \"rirSearch1\", " +
                "\"autnums\", \"cidr0\", \"rdap_level_0\", \"nro_rdap_profile_0\", \"redacted\" ]"));
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
                containsString("\"links\" : [ {\n" +
                        "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"rel\" : \"rdap-up\",\n" +
                        "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-up/AS102\",\n" +
                        "    \"type\" : \"application/rdap+json\"\n" +
                        "  }, {\n" +
                        "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"rel\" : \"rdap-up rdap-active\",\n" +
                        "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-up/AS102?status=active\",\n" +
                        "    \"type\" : \"application/rdap+json\"\n" +
                        "  }, {\n" +
                        "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"rel\" : \"rdap-down\",\n" +
                        "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-down/AS102\",\n" +
                        "    \"type\" : \"application/rdap+json\"\n" +
                        "  }, {\n" +
                        "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"rel\" : \"rdap-top\",\n" +
                        "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-top/AS102\",\n" +
                        "    \"type\" : \"application/rdap+json\"\n" +
                        "  }, {\n" +
                        "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"rel\" : \"rdap-top rdap-active\",\n" +
                        "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-top/AS102?status=active\"," +
                        "\n" +
                        "    \"type\" : \"application/rdap+json\"\n" +
                        "  }, {\n" +
                        "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"rel\" : \"rdap-bottom\",\n" +
                        "    \"href\" : \"https://rdap.db.ripe.net/autnums/rirSearch1/rdap-bottom/AS102\",\n" +
                        "    \"type\" : \"application/rdap+json\"\n" +
                        "  }, {\n" +
                        "    \"value\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"rel\" : \"self\",\n" +
                        "    \"href\" : \"https://rdap.db.ripe.net/autnum/102\",\n" +
                        "    \"type\" : \"application/rdap+json\"\n" +
                        "  }, {\n" +
                        "    \"value\" : \"http://www.ripe.net/data-tools/support/documentation/terms\",\n" +
                        "    \"rel\" : \"copyright\",\n" +
                        "    \"href\" : \"http://www.ripe.net/data-tools/support/documentation/terms\"\n" +
                        "  } ]"));
        assertThat(entity,
                containsString("[ \"nro_rdap_profile_asn_flat_0\", \"rirSearch1\", \"autnums\", " +
                        "\"cidr0\", \"rdap_level_0\", \"nro_rdap_profile_0\", \"redacted\" ]"));
    }

    @Test
    public void lookup_autnum_within_block() {
        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("autnum/1500")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(notFoundException, 404);
        assertErrorTitle(notFoundException, "Not found");
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
                "mnt-irt:       irt-IRT1\n" +
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
                "[[version, {}, text, 4.0], [fn, {}, text, irt-IRT1], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Street 1}, text, [, , , , , , ]]]"));

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
                "nic-hdl:       AC1-TEST\n" +
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
                "abuse-c:       AC1-TEST\n" +
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
        assertThat(entities.get(3).getHandle(), is("AC1-TEST"));
        assertThat(entities.get(3).getRoles(), contains(Role.ABUSE));
        assertThat(entities.get(3).getVCardArray(), hasSize(2));
        assertThat(entities.get(3).getVCardArray().get(0).toString(), is("vcard"));
        assertThat(entities.get(3).getVCardArray().get(1).toString(), is("" +
                "[[version, {}, text, 4.0], " +
                "[fn, {}, text, Abuse Contact], " +
                "[kind, {}, text, group], " +
                "[adr, {label=Singel 358}, text, [, , , , , , ]], " +
                "[tel, {type=voice}, text, +31 6 12345678], " +
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
                        "Abuse-mailbox validation failed. Please refer to ORG-TEST1-TEST for further information.\n"));
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
                        "Abuse-mailbox validation failed. Please refer to ORG-TEST1-TEST for further information.\n"));

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
        assertErrorTitle(badRequestException, "Bad Request");
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
        assertErrorTitle(notFoundException, "Not Found");
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
        assertErrorTitle(notFoundException, "Not Found");
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
                "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void lookup_org_invalid_syntax() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entity/ORG-INVALID")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "Bad Request");
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
        assertThat(entity.getLinks().get(0).getType(), is("application/rdap+json"));
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
                "mbrs-by-ref:   OWNER-MNT\n" +
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

        assertThat(entity.getPort43(), is("whois.ripe.net"));
        assertThat(entity.getRdapConformance(), hasSize(4));
        assertThat(entity.getRdapConformance(), containsInAnyOrder("rdap_level_0", "cidr0", "nro_rdap_profile_0",
                "redacted"));

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

    @Test
    public void lookup_nameserver_not_found() {
        final ServerErrorException serverErrorException = assertThrows(ServerErrorException.class, () -> {
            createResource("nameserver/test")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Autnum.class);
        });
        assertErrorStatus(serverErrorException, 501);
        assertErrorTitle(serverErrorException, "Not Implemented");
        assertErrorDescription(serverErrorException, "Nameserver not supported");
    }

    @Test
    public void lookup_db_duplicated_key_correct_error(){

        final RpslObject rpslObject = RpslObject.parse("""
                mntner:        TP1-TEST
                descr:         Owner Maintainer
                admin-c:       TP1-TEST
                upd-to:        noreply@ripe.net
                auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test
                mnt-by:        OWNER-MNT
                mbrs-by-ref:   OWNER-MNT
                created:         2011-07-28T00:35:42Z
                last-modified:   2019-02-28T10:14:46Z
                source:        TEST""");

        databaseHelper.addObject(rpslObject);

        final InternalServerErrorException internalServerErrorException = assertThrows(InternalServerErrorException.class, () -> createResource("entity/TP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class)
        );
        assertErrorStatus(internalServerErrorException, 500);
        assertErrorTitle(internalServerErrorException, "Internal Error");
        assertErrorDescription(internalServerErrorException, "More than one object matches primary key");
    }
    // Redactions

    @Test
    public void lookup_organisation_redactions() {
        createEntityRedactionObjects();

        databaseHelper.addObject("" +
                "organisation:  ORG-ONE-TEST\n" +
                "org-name:      Organisation One\n" +
                "org-type:      LIR\n" +
                "descr:         Test organisation\n" +
                "address:       One Org Street\n" +
                "e-mail:        test@ripe.net\n" +
                "language:      EN\n" +
                "admin-c:       TP2-TEST\n" + //has notify
                "tech-c:        TP1-TEST\n" +
                "tech-c:        TP2-TEST\n" + //has notify
                "notify:       test@ripe.net\n" +
                "notify:       test1@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "mnt-ref:       INCOMING-MNT\n" +
                "mnt-ref:       INCOMING2-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/ORG-ONE-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "TP2-TEST");

        assertCommon(entity);
    }

    @Test
    public void lookup_organisation_redactions_different_person() {
        createEntityRedactionObjects();

        databaseHelper.addObject("" +
                "person:        Tester Person\n" +
                "nic-hdl:       TP3-TEST\n" +
                "address:       One Org Street\n" +
                "e-mail:        test@ripe.net\n" +
                "notify:       test@ripe.net\n" +
                "notify:       test1@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "organisation:  ORG-ONE-TEST\n" +
                "org-name:      Organisation One\n" +
                "org-type:      LIR\n" +
                "descr:         Test organisation\n" +
                "address:       One Org Street\n" +
                "e-mail:        test@ripe.net\n" +
                "language:      EN\n" +
                "admin-c:       TP2-TEST\n" + //has notify
                "tech-c:        TP1-TEST\n" +
                "tech-c:        TP3-TEST\n" + //has notify
                "notify:       test@ripe.net\n" +
                "notify:       test1@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "mnt-ref:       INCOMING-MNT\n" +
                "mnt-ref:       INCOMING2-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/ORG-ONE-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "TP2-TEST");
        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "TP3-TEST");
        assertCommon(entity);
    }

    @Test
    public void lookup_person_without_redactions() {
        createEntityRedactionObjects();

        databaseHelper.addObject("" +
                "person:        Tester Person\n" +
                "nic-hdl:       TP3-TEST\n" +
                "address:       One Org Street\n" +
                "mnt-by:        OWNER-MNT\n" +
                "mnt-ref:       INCOMING-MNT\n" +
                "mnt-ref:       INCOMING2-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        final Entity entity = createResource("entity/TP3-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getRedacted().size(), is(0));
    }

    @Test
    public void lookup_aut_num_redactions() {
        databaseHelper.addObject("" +
                "aut-num:       AS103\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "admin-c:       TP2-TEST\n" + //has notify
                "tech-c:        TP1-TEST\n" +
                "notify:       test@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:       2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        final Autnum autnum = createResource("autnum/103")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Autnum.class);

        assertEmailRedactionForEntities(autnum, autnum.getEntitySearchResults(), "$", "TP2-TEST");
    }

    @Test
    public void lookup_org_inetnum_autnum_entity_redactions() {
        databaseHelper.addObject("" +
                "person:        Tester Person\n" +
                "nic-hdl:       TP3-TEST\n" +
                "address:       One Org Street\n" +
                "e-mail:        test@ripe.net\n" +
                "notify:       test@ripe.net\n" +
                "notify:       test1@ripe.net\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "aut-num:       AS64496\n" +
                "as-name:       AS-TEST\n" +
                "descr:         A single ASN\n" +
                "org:           ORG-TEST1-TEST\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP2-TEST\n" +
                "mnt-by:        OWNER-MNT\n" +
                "notify:       test@ripe.net\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.192.0 - 109.111.223.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST1-TEST\n" +
                "country:      NL\n" +
                "admin-c:       TP2-TEST\n" +
                "tech-c:       TP3-TEST\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        final Entity entity = createResource("entity/ORG-TEST1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getRedacted().size(), is(6));


        final Ip ip = entity.getNetworks().stream().filter(network -> network.getHandle().equals("109.111.192.0 - 109.111.223.255")).findFirst().get();
        assertEmailRedactionForEntities(entity, ip.getEntitySearchResults(), "$.networks", "ORG-TEST1-TEST");
        assertEmailRedactionForEntities(entity, ip.getEntitySearchResults(), "$.networks", "TP2-TEST");
        assertEmailRedactionForEntities(entity, ip.getEntitySearchResults(), "$.networks", "TP3-TEST");

        final Autnum autnum = entity.getAutnums().stream().filter(network -> network.getHandle().equals("AS64496")).findFirst().get();
        assertEmailRedactionForEntities(entity, autnum.getEntitySearchResults(), "$.autnums", "ORG-TEST1-TEST");
        assertEmailRedactionForEntities(entity, autnum.getEntitySearchResults(), "$.autnums", "TP2-TEST");

        assertEmailRedactionForEntities(entity, entity.getEntitySearchResults(), "$", "PP1-TEST");

        assertCommon(entity);
    }

    @Test
    public void lookup_org_with_inetnum_one_country_and_language_no_redactions() {
        databaseHelper.addObject("" +
                "organisation:  ORG-TEST2-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "country:      NL\n" +
                "language:      NL\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.192.0 - 109.111.223.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST2-TEST\n" +
                "country:      NL\n" +
                "language:      NL\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        final Entity entity = createResource("entity/ORG-TEST2-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getRedacted().size(), is(0));
        assertCommon(entity);
    }

    @Test
    public void lookup_org_with_inetnum_multiple_country_and_language() {
        databaseHelper.addObject("" +
                "organisation:  ORG-TEST2-TEST\n" +
                "org-name:      Test organisation\n" +
                "org-type:      OTHER\n" +
                "descr:         Drugs and gambling\n" +
                "remarks:       Nice to deal with generally\n" +
                "address:       1 Fake St. Fauxville\n" +
                "phone:         +01-000-000-000\n" +
                "fax-no:        +01-000-000-000\n" +
                "country:      NL\n" +
                "country:      ES\n" +
                "language:      NL\n" +
                "language:      ES\n" +
                "mnt-by:        OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "inetnum:      109.111.192.0 - 109.111.223.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "org:          ORG-TEST2-TEST\n" +
                "country:      NL\n" +
                "country:      ES\n" +
                "language:      NL\n" +
                "language:      ES\n" +
                "status:       OTHER\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        final Entity entity = createResource("entity/ORG-TEST2-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Entity.class);

        assertThat(entity.getRedacted().size(), is(3));

        assertMultipleValuesRedaction(entity, "$", LANGUAGE, "NL, ES");
        assertMultipleValuesRedaction(entity, "$.networks[?(@.handle=='109.111.192.0 - 109.111.223.255')]", COUNTRY, "NL, ES");
        assertCommon(entity);
    }

    @Test
    public void lookup_domain_object_inetnum_redactions(){

        databaseHelper.addObject("" +
                "inetnum:       80.179.52.0 - 80.179.55.255\n" +
                "netname:       SANDBOX11470-IPv4-ALLOCATION\n" +
                "org:           ORG-TEST1-TEST\n" +
                "country:       EU\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "status:        ALLOCATED PA\n" +
                "mnt-by:        OWNER-MNT\n" +
                "tech-c:       TP2-TEST\n" +
                "created:       2013-12-10T16:54:20Z\n" +
                "last-modified: 2013-12-10T16:54:20Z\n" +
                "source:        RIPE\n");

        databaseHelper.addObject("" +
                "domain:        52.179.80.in-addr.arpa\n" +
                "descr:         Test domain\n" +
                "admin-c:       TP1-TEST\n" +
                "tech-c:        TP1-TEST\n" +
                "zone-c:        TP1-TEST\n" +
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

        assertThat(domain.getRedacted().size(), is(2));

        assertEmailRedactionForEntities(domain, domain.getNetwork().getEntitySearchResults(), "$", "ORG-TEST1-TEST");
        assertEmailRedactionForEntities(domain, domain.getNetwork().getEntitySearchResults(), "$", "TP2-TEST");
    }

    // search - entities - organisation

    @Test
    public void search_entity_empty_handle() {
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("entities?handle=")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Entity.class);
        });
        assertErrorStatus(badRequestException, 400);
        assertErrorTitle(badRequestException, "Bad Request");
        assertErrorDescription(badRequestException, "Empty search term");
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
    }

    @Test
    public void cross_origin_get_request_from_apps_db_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "https://apps.db.ripe.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("https://apps.db.ripe.net"));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_from_outside_ripe_net_is_allowed() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "https://www.foo.net")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_malformed_origin() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "?invalid?")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_host_and_port() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.ORIGIN, "https://www.foo.net:8443")
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is("*"));
        assertThat(response.readEntity(Entity.class).getHandle(), is("PP1-TEST"));
    }

    @Test
    public void cross_origin_get_request_without_origin() {
        final Response response = createResource("entity/PP1-TEST")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header(HttpHeaders.HOST, "rdap.db.ripe.net")
                .get();

        assertThat(response.getHeaderString(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN), is(nullValue()));
    }

    @Test
    public void get_help_response() {
        final RdapObject help = createResource("help")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(RdapObject.class);

        assertThat(help.getPort43(), is("whois.ripe.net"));
        assertThat(help.getRdapConformance(), hasSize(11));
        assertThat(help.getRdapConformance(), containsInAnyOrder("cidr0", "rdap_level_0", "nro_rdap_profile_0",
                "nro_rdap_profile_asn_flat_0", "redacted", "geofeed1", "rirSearch1", "ips", "ipSearchResults",
                "autnums", "autnumSearchResults"));

        final List<Notice> notices = help.getNotices();
        assertThat(notices, hasSize(1));
        assertTnCNotice(notices.get(0), "https://rdap.db.ripe.net/help");

        assertCopyrightLink(help.getLinks(), "https://rdap.db.ripe.net/help");
    }

    /*RIR Search*/

    @Test
    public void get_invalid_relation_autnum_then_400(){
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("autnums/rirSearch1/upper/AS123")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(badRequestException, "Bad Request");
        assertErrorStatus(badRequestException, HttpStatus.BAD_REQUEST_400);
        assertErrorDescription(badRequestException, "Relation upper doesn't exist");
    }


    // up
    @Test
    public void get_up_autnum_then_501(){
        final ServerErrorException notImplementedException = assertThrows(ServerErrorException.class, () -> {
            createResource("autnums/rirSearch1/rdap-up/AS123")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(notImplementedException, "Not Implemented");
        assertErrorStatus(notImplementedException, HttpStatus.NOT_IMPLEMENTED_501);
        assertErrorDescription(notImplementedException, "Relation queries not allowed for autnum");
    }

    @Test
    public void get_up_then_parent(){
        loadIpv4RelationTreeExample();

        final Ip ip = createResource("ips/rirSearch1/rdap-up/192.0.2.0/28")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.2.0 - 192.0.2.127")); // /26
        assertThat(ip.getRdapConformance(), containsInAnyOrder("ips", "ipSearchResults", "rirSearch1", "geofeed1",
                "cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_up_active_status_then_parent(){
        loadIpv4RelationTreeExample();

        final Ip ip = createResource("ips/rirSearch1/rdap-up/192.0.2.0/28?status=active")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.2.0 - 192.0.2.127")); // /26
        assertThat(ip.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults", "geofeed1",
                "cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_non_existing_up_then_404(){
        loadIpv4RelationTreeExample();

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("ips/rirSearch1/rdap-up/192.0.2.0/24")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(notFoundException, "Not Found");
        assertErrorStatus(notFoundException, HttpStatus.NOT_FOUND_404);
        assertErrorDescription(notFoundException, "No up level object has been found for 192.0.2.0/24");
    }

    @Test
    public void get_wrong_key_parameter_then_400(){
        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("ips/rirSearch1/rdap-up/192.0.2")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(badRequestException, "Bad Request");
        assertErrorStatus(badRequestException, HttpStatus.BAD_REQUEST_400);
        assertErrorDescription(badRequestException, "'192.0.2' is not an IP string literal.");
    }


    @Test
    public void get_ipv6_up_then_parent(){
        loadIpv6RelationTreeExample();

        final Ip ip = createResource("ips/rirSearch1/rdap-up/2001:db8::/32")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2001::/16"));
        assertThat(ip.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults", "geofeed1",
                "cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "redacted"));
    }


    @Test
    public void get_domain_up_then_parent(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final Domain domain = createResource("domains/rirSearch1/rdap-up/1.2.0.192.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertThat(domain.getHandle(), is("2.0.192.in-addr.arpa"));
        assertThat(domain.getRdapConformance(), containsInAnyOrder("rirSearch1", "cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "redacted"));
    }


    @Test
    public void get_non_existing_domain_up_then_404(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("domains/rirSearch1/rdap-up/0.192.in-addr.arpa")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(notFoundException, "Not Found");
        assertErrorStatus(notFoundException, HttpStatus.NOT_FOUND_404);
        assertErrorDescription(notFoundException, "No up level object has been found for 192.0.0.0/16");
    }

    @Test
    public void if_inetnum_top_then_up(){

        databaseHelper.addObject("" +
                "inetnum:      193.0.0.0 - 193.0.23.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        ipTreeUpdater.rebuild();

        final Ip topIp = createResource("ips/rirSearch1/rdap-top/193.0.6.15")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        final Ip upIp = createResource("ips/rirSearch1/rdap-up/193.0.6.15")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(topIp.getHandle(), is(upIp.getHandle()));
        assertThat(topIp.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults", "geofeed1",
                "cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "redacted"));

    }

    @Test
    public void if_inet6num_top_then_up(){

        databaseHelper.addObject(
                "inet6num:       2001:2002:2003::/48\n" +
                        "netname:        RIPE-NCC\n" +
                        "descr:          Private Network\n" +
                        "country:        NL\n" +
                        "tech-c:         TP1-TEST\n" +
                        "status:         ALLOCATED-BY-RIR\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "mnt-lower:      OWNER-MNT\n" +
                        "created:         2022-08-14T11:48:28Z\n" +
                        "last-modified:   2022-10-25T12:22:39Z\n" +
                        "source:         TEST"
        );
        ipTreeUpdater.rebuild();

        final Ip topIp = createResource("ips/rirSearch1/rdap-top/2001:2002:2003::")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        final Ip upIp = createResource("ips/rirSearch1/rdap-up/2001:2002:2003::")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(topIp.getHandle(), is(upIp.getHandle()));
        assertThat(topIp.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults", "geofeed1",
                "cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "redacted"));

    }

    @Test
    public void get_up_with_non_active_status_then_501(){
        final ServerErrorException notImplementedException = assertThrows(ServerErrorException.class, () -> {
            createResource("ips/rirSearch1/rdap-up/192.0.2.0/28?status=administrative")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });

        assertErrorTitle(notImplementedException, "Not Implemented");
        assertErrorStatus(notImplementedException, HttpStatus.NOT_IMPLEMENTED_501);
        assertErrorDescription(notImplementedException, "administrative status is not implemented");
    }


    // Top
    @Test
    public void get_top_then_less_specific_allocated_assigned_first_parent(){
        loadIpv4RelationTreeExample();

        final Ip ip = createResource("ips/rirSearch1/rdap-top/192.0.2.0/28")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.2.0 - 192.0.2.255")); // /24
        assertThat(ip.getRdapConformance(), containsInAnyOrder("ips", "ipSearchResults", "rirSearch1", "geofeed1",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_top_active_status_then_less_specific_allocated_assigned_first_parent(){
        loadIpv4RelationTreeExample();

        final Ip ip = createResource("ips/rirSearch1/rdap-top/192.0.2.0/28?status=active")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("192.0.2.0 - 192.0.2.255")); // /24
        assertThat(ip.getRdapConformance(), containsInAnyOrder("ips", "ipSearchResults", "rirSearch1", "geofeed1",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_ipv6_top_then_less_specific_allocated_assigned_first_parent(){
        loadIpv6RelationTreeExample();

        databaseHelper.updateObject("" +
                "inet6num:       2000::/3\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-RIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        final Ip ip = createResource("ips/rirSearch1/rdap-top/2001:db8::/32")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2000::/3"));
        assertThat(ip.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults", "geofeed1",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_domain_top_then_less_specific_allocated_assigned_first_parent(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final Domain domain = createResource("domains/rirSearch1/rdap-top/1.2.0.192.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Domain.class);

        assertThat(domain.getHandle(), is("0.192.in-addr.arpa"));
        assertThat(domain.getRdapConformance(), containsInAnyOrder("rirSearch1", "cidr0", "rdap_level_0",
                "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_non_existing_domain_top_then_404(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("domains/rirSearch1/rdap-top/0.192.in-addr.arpa")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(notFoundException, "Not Found");
        assertErrorStatus(notFoundException, HttpStatus.NOT_FOUND_404);
        assertErrorDescription(notFoundException, "No top-level object has been found for 192.0.0.0/16");
    }

    @Test
    public void get_ipv6_top_not_found(){
        loadIpv6RelationTreeExample();

        //ALLOCATED-BY-RIR -> ALLOCATED-BY-RIR when the child and parent has this status, parent is administrative
        databaseHelper.updateObject("" +
                "inet6num:       2000::/3\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-RIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("ips/rirSearch1/rdap-top/2000::/3")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(notFoundException, "Not Found");
        assertErrorStatus(notFoundException, HttpStatus.NOT_FOUND_404);
        assertErrorDescription(notFoundException, "No top-level object has been found for 2000::/3");
    }


    @Test
    public void get_ipv6_top_found_if_assignment(){
        loadIpv6RelationTreeExample();

        databaseHelper.addObject("" +
                "inet6num:       2000::/2\n" +
                "netname:        TEST\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-RIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ips/rirSearch1/rdap-top/2000::/3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(ip.getHandle(), is("2000::/2"));
    }


    @Test
    public void get_non_existing_top_then_404(){
        loadIpv4RelationTreeExample();

        final NotFoundException notFoundException = assertThrows(NotFoundException.class, () -> {
            createResource("ips/rirSearch1/rdap-top/192.0.2.0/24")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });

        assertErrorTitle(notFoundException, "Not Found");
        assertErrorStatus(notFoundException, HttpStatus.NOT_FOUND_404);
        assertErrorDescription(notFoundException, "No top-level object has been found for 192.0.2.0/24");
    }

    @Test
    public void get_top_with_non_active_status_then_501(){
        final ServerErrorException notImplementedException = assertThrows(ServerErrorException.class, () -> {
            createResource("ips/rirSearch1/rdap-top/192.0.2.0/28?status=administrative")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(Ip.class);
        });

        assertErrorTitle(notImplementedException, "Not Implemented");
        assertErrorStatus(notImplementedException, HttpStatus.NOT_IMPLEMENTED_501);
        assertErrorDescription(notImplementedException, "administrative status is not implemented");
    }

    // Bottom
    @Test
    public void get_bottom_then_bottom(){
        loadIpv4RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-bottom/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(5));
        assertThat(ipResults.getFirst().getHandle(), is("192.0.2.0 - 192.0.2.0")); //32
        assertThat(ipResults.get(1).getHandle(), is("192.0.2.0 - 192.0.2.15")); //28
        assertThat(ipResults.get(2).getHandle(), is("192.0.2.0 - 192.0.2.127")); //25
        assertThat(ipResults.get(3).getHandle(), is("192.0.2.128 - 192.0.2.191")); //26
        assertThat(ipResults.get(4).getHandle(), is("192.0.2.192 - 192.0.2.255")); //26
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_bottom_ipv6_then_bottom(){
        loadIpv6RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-bottom/2000::/3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(6));
        assertThat(ipResults.getFirst().getHandle(), is("2001::/16"));
        assertThat(ipResults.get(1).getHandle(), is("2001::/23"));
        assertThat(ipResults.get(2).getHandle(), is("2001:db8::/32"));
        assertThat(ipResults.get(3).getHandle(), is("2400::/12"));
        assertThat(ipResults.get(4).getHandle(), is("2600::/12"));
        assertThat(ipResults.get(5).getHandle(), is("2800::/12"));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_bottom_ipv6_cover_parent_then_bottom(){
        loadIpv6RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-bottom/FC00::/7")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(2));
        assertThat(ipResults.getFirst().getHandle(), is("FC00::/8"));
        assertThat(ipResults.get(1).getHandle(), is("FD00::/8"));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_bottom_from_root_parent_then_bottom(){
        loadIpv6RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-bottom/::/0")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(9));
        assertThat(ipResults.getFirst().getHandle(), is("2001::/16"));
        assertThat(ipResults.get(1).getHandle(), is("2001::/23"));
        assertThat(ipResults.get(2).getHandle(), is("2000::/3"));
        assertThat(ipResults.get(3).getHandle(), is("2001:db8::/32"));
        assertThat(ipResults.get(4).getHandle(), is("2400::/12"));
        assertThat(ipResults.get(5).getHandle(), is("2600::/12"));
        assertThat(ipResults.get(6).getHandle(), is("2800::/12"));
        assertThat(ipResults.get(7).getHandle(), is("FC00::/8"));
        assertThat(ipResults.get(8).getHandle(), is("FD00::/8"));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_domain_bottom_then_bottom(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final SearchResult searchResult = createResource("domains/rirSearch1/rdap-bottom/0.192.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Domain> domainResults = searchResult.getDomainSearchResults();
        assertThat(domainResults.size(), is(2));
        assertThat(domainResults.getFirst().getHandle(), is("1.2.0.192.in-addr.arpa"));
        assertThat(domainResults.get(1).getHandle(), is("2.0.192.in-addr.arpa"));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_non_existing_domain_bottom_then_empty(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final SearchResult searchResult = createResource("domains/rirSearch1/rdap-bottom/1.2.0.192.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Domain> domainResults = searchResult.getDomainSearchResults();
        assertThat(domainResults, is(empty()));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_non_existing_bottom_then_empty_response(){
        loadIpv4RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-bottom/192.0.2.0/32")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults, is(empty()));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_bottom_wrong_type_then_400(){

        final BadRequestException badRequestException = assertThrows(BadRequestException.class, () -> {
            createResource("ip/rirSearch1/rdap-bottom/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);
        });
        assertErrorTitle(badRequestException, "Bad Request");
        assertErrorStatus(badRequestException, HttpStatus.BAD_REQUEST_400);
        assertErrorDescription(badRequestException, "Invalid or unknown type ip");
    }

    @Test
    public void slash_31_starting_by_255_resources_then_200(){
        databaseHelper.addObject(
                "inetnum:       37.210.0.0-37.211.255.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ALLOCATED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "mnt-lower:      OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        databaseHelper.addObject(
                "inetnum:       37.210.31.255 - 37.210.32.0\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");


        databaseHelper.addObject(
                "inetnum:       37.210.63.255 - 37.210.64.0\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        ipTreeUpdater.rebuild();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-bottom/37.210.0.0%20-%2037.211.255.255")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(2));
        assertThat(ipResults.getFirst().getHandle(), is("37.210.63.255 - 37.210.64.0"));
        assertThat(ipResults.getLast().getHandle(), is("37.210.31.255 - 37.210.32.0"));
    }

    @Test
    public void just_inetnum_within_range_should_be_included(){
        databaseHelper.addObject(
                "inetnum:       193.0.0.0 - 193.0.23.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ALLOCATED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "mnt-lower:      OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        databaseHelper.addObject(
                "inetnum:       193.0.0.0 - 193.0.7.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");


        databaseHelper.addObject(
                "inetnum:       193.0.8.0 - 193.0.8.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        databaseHelper.addObject(
                "inetnum:       193.0.9.0 - 193.0.9.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        databaseHelper.addObject(
                "inetnum:       193.0.10.0 - 193.0.11.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        databaseHelper.addObject(
                "inetnum:       193.0.12.0 - 193.0.13.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        databaseHelper.addObject(
                "inetnum:       193.0.14.0 - 193.0.15.255\n" +
                        "netname:        test" +
                        "descr:          issue causing IP\n" +
                        "country:        EU\n" +
                        "tech-c:         TP1-TEST\n" +
                        "admin-c:       TP1-TEST\n" +
                        "status:         ASSIGNED PA\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "created:       2011-07-28T00:35:42Z\n" +
                        "last-modified: 2019-02-28T10:14:46Z\n" +
                        "source:         TEST");

        ipTreeUpdater.rebuild();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-bottom/193.0.0.0/20")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(6));
        assertThat(ipResults.getFirst().getHandle(), is("193.0.8.0 - 193.0.8.255"));
        assertThat(ipResults.get(1).getHandle(), is("193.0.9.0 - 193.0.9.255"));
        assertThat(ipResults.get(2).getHandle(), is("193.0.10.0 - 193.0.11.255"));
        assertThat(ipResults.get(3).getHandle(), is("193.0.12.0 - 193.0.13.255"));
        assertThat(ipResults.get(4).getHandle(), is("193.0.0.0 - 193.0.7.255"));
        assertThat(ipResults.getLast().getHandle(), is("193.0.14.0 - 193.0.15.255"));
    }

    @Test
    public void bottom_with_status_then_501(){

        final ServerErrorException notImplementedException = assertThrows(ServerErrorException.class, () -> {
            createResource("ip/rirSearch1/rdap-bottom/192.0.2.0/24?status=active")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });
        assertErrorTitle(notImplementedException, "Not Implemented");
        assertErrorStatus(notImplementedException, HttpStatus.NOT_IMPLEMENTED_501);
        assertErrorDescription(notImplementedException, "Status is not implement in down and bottom relation");
    }

    // Down
    @Test
    public void get_down_then_immediate_child(){
        loadIpv4RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-down/192.0.2.0/24")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(2));
        assertThat(ipResults.getFirst().getHandle(), is("192.0.2.0 - 192.0.2.127"));
        assertThat(ipResults.get(1).getHandle(), is("192.0.2.128 - 192.0.2.255"));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_down_ipv6_then_immediate_child(){
        loadIpv6RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-down/2000::/3")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults.size(), is(4));
        assertThat(ipResults.getFirst().getHandle(), is("2001::/16"));
        assertThat(ipResults.get(1).getHandle(), is("2400::/12"));
        assertThat(ipResults.get(2).getHandle(), is("2600::/12"));
        assertThat(ipResults.get(3).getHandle(), is("2800::/12"));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_down_domain_then_immediate_child(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final SearchResult searchResult = createResource("domains/rirSearch1/rdap-down/0.192.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Domain> domainResults = searchResult.getDomainSearchResults();
        assertThat(domainResults.size(), is(1));
        assertThat(domainResults.getFirst().getHandle(), is("2.0.192.in-addr.arpa"));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_down_domain_no_child_then_empty_response(){
        loadIpv4RelationTreeExample();
        loadIpv4RelationDomainExample();

        final SearchResult searchResult = createResource("domains/rirSearch1/rdap-down/1.2.0.192.in-addr.arpa")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Domain> domainResults = searchResult.getDomainSearchResults();
        assertThat(domainResults, is(empty()));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void get_down_when_no_child_then_empty_response(){
        loadIpv4RelationTreeExample();

        final SearchResult searchResult = createResource("ips/rirSearch1/rdap-down/192.0.2.0/32")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> ipResults = searchResult.getIpSearchResults();
        assertThat(ipResults, is(empty()));
        assertThat(searchResult.getRdapConformance(), containsInAnyOrder("rirSearch1", "ips", "ipSearchResults",
                "cidr0", "rdap_level_0", "nro_rdap_profile_0", "redacted"));
    }

    @Test
    public void down_with_status_then_501(){

        final ServerErrorException notImplementedException = assertThrows(ServerErrorException.class, () -> {
            createResource("ip/rirSearch1/rdap-down/192.0.2.0/24?status=inactive")
                    .request(MediaType.APPLICATION_JSON_TYPE)
                    .get(SearchResult.class);
        });
        assertErrorTitle(notImplementedException, "Not Implemented");
        assertErrorStatus(notImplementedException, HttpStatus.NOT_IMPLEMENTED_501);
        assertErrorDescription(notImplementedException, "Status is not implement in down and bottom relation");
    }
    // Links

    @Test
    public void use_relation_links_then_up_bottom_top_down(){
        loadIpv4RelationTreeExample();

        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.0.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        ipTreeUpdater.rebuild();

        final Ip ip = createResource("ip/192.0.2.0/25")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        final Map<String, String> relationCalls = getRelationCallsFromLinks(ip.getLinks());

        assertThat(relationCalls.size(), is(6));

        //TOP (By default active)
        final Ip topIp = createResource(relationCalls.get(RelationType.TOP.getValue()))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(topIp.getHandle(), is("192.0.0.0 - 192.0.255.255")); //16


        //TOP active
        final Ip topActiveIp = createResource(relationCalls.get(LinkRelationType.concat(LinkRelationType.TOP, LinkRelationType.ACTIVE)))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(topActiveIp.getHandle(), is("192.0.0.0 - 192.0.255.255")); //16


        //BOTTOM
        final SearchResult bottomSearchResult = createResource(relationCalls.get(RelationType.BOTTOM.getValue()))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> bottomIpResults = bottomSearchResult.getIpSearchResults();
        assertThat(bottomIpResults.size(), is(2));
        assertThat(bottomIpResults.getFirst().getHandle(), is("192.0.2.0 - 192.0.2.0")); //32
        assertThat(bottomIpResults.get(1).getHandle(), is("192.0.2.0 - 192.0.2.15")); //28

        //DOWN
        final SearchResult downSearchResult = createResource(relationCalls.get(RelationType.DOWN.getValue()))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(SearchResult.class);

        final List<Ip> downIpResults = downSearchResult.getIpSearchResults();
        assertThat(downIpResults.size(), is(1));
        assertThat(bottomIpResults.getFirst().getHandle(), is("192.0.2.0 - 192.0.2.0")); //32

        //UP (by default active)
        final Ip upIp = createResource(relationCalls.get(RelationType.UP.getValue()))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(upIp.getHandle(), is("192.0.2.0 - 192.0.2.255")); //24

        //UP active
        final Ip upActiveIp = createResource(relationCalls.get(LinkRelationType.concat(LinkRelationType.UP, LinkRelationType.ACTIVE)))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(Ip.class);

        assertThat(upActiveIp.getHandle(), is("192.0.2.0 - 192.0.2.255")); //24
    }


    /* Helper methods*/

    private void assertRelationLinks(final List<Link> links){
        final Map<String, String> relationCalls = getRelationCallsFromLinks(links);
        assertThat(relationCalls.size(), is(6));
    }

    private void assertDomain(RdapObject object) {
        assertThat(object.getPort43(), is("whois.ripe.net"));
        assertThat(object.getRdapConformance(), hasSize(5));
        assertThat(object.getRdapConformance(), containsInAnyOrder(RdapConformance.RIR_SEARCH_1.getValue(),
                RdapConformance.LEVEL_0.getValue(),
                RdapConformance.CIDR_0.getValue(),
                RdapConformance.NRO_PROFILE_0.getValue(), RdapConformance.REDACTED.getValue()));
    }

    private void assertCommon(RdapObject object) {
        assertThat(object.getPort43(), is("whois.ripe.net"));
        assertThat(object.getRdapConformance(), hasSize(4));
        assertThat(object.getRdapConformance(), containsInAnyOrder(RdapConformance.LEVEL_0.getValue(),
                RdapConformance.CIDR_0.getValue(),
                RdapConformance.NRO_PROFILE_0.getValue(), RdapConformance.REDACTED.getValue()));
    }

    private void assertGeoFeedLink(final List<Link> links, final String value) {
        assertThat(links, hasSize(9));

        final Optional<Link> geoFeedLink = links.stream().filter(link -> link.getRel().equals("geo")).findFirst();
        assertThat(geoFeedLink.isPresent(), is(true));
        assertThat(geoFeedLink.get().getValue(), is(value));
        assertThat(geoFeedLink.get().getHref(), is("https://test.net/geo/test.csv"));
        assertThat(geoFeedLink.get().getType(), is("application/geofeed+csv"));
    }

    private void assertResourceCopyrightLink(final List<Link> links, final String value) {
        assertThat(links.size(), is(8));

        final List<Link> copyrightLinks = links.stream()
                .filter(link -> link.getRel().equals("copyright") || link.getRel().equals("self"))
                .collect(Collectors.toCollection(ArrayList::new));

        assertCopyrightLink(copyrightLinks, value);
    }

    private void assertCopyrightLink(final List<Link> links, final String value) {
        assertThat(links.size(), is(2));
        Collections.sort(links);

        assertThat(links.get(0).getRel(), is("copyright"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));
        assertThat(links.get(0).getHref(), is("http://www.ripe.net/data-tools/support/documentation/terms"));

        assertThat(links.get(1).getRel(), is("self"));
        assertThat(links.get(1).getType(), is("application/rdap+json"));
        assertThat(links.get(1).getValue(), is(value));
        assertThat(links.get(1).getHref(), is(value));
    }

    private void assertTnCNotice(final Notice notice, final String value) {
        assertThat(notice.getTitle(), is("Terms and Conditions"));
        assertThat(notice.getDescription(), contains("This is the RIPE Database query service. The objects are in RDAP format."));
        assertThat(notice.getLinks().get(0).getHref(), is("https://docs.db.ripe.net/terms-conditions.html"));

        assertThat(notice.getLinks().get(0).getRel(), is("terms-of-service"));
        assertThat(notice.getLinks().get(0).getHref(), is("https://docs.db.ripe.net/terms-conditions.html"));
        assertThat(notice.getLinks().get(0).getType(), is("application/pdf"));
        assertThat(notice.getLinks().get(0).getValue(), is(value));
    }

    private void assertMultipleValuesRedaction(final RdapObject rdapObject, final String prefix, final AttributeType type, final String multipleValues) {
        final String rdapAttrName = (type == LANGUAGE) ? "lang" : type.getName();
        final Redaction redaction = rdapObject.getRedacted().stream().filter(redact -> redact.getPostPath() != null && redact.getPostPath().contains(rdapAttrName)).findFirst().get();

        final Redaction expectedRedaction = Redaction.getRedactionByPartialValue(String.format("Multiple %s attributes found", type.getName()),
                String.format("%s.%s", prefix, rdapAttrName),
                String.format("There are multiple %s attributes %s found, but only the first %s %s returned.", type.getName(), multipleValues, type.getName(), multipleValues.split(",")[0]));

        assertThat(redaction, equalTo(expectedRedaction));

        final String entityJson = getEntityJson(rdapObject);
        final Object redactedElement = JsonPath.read(entityJson, redaction.getPostPath());

        if (redactedElement instanceof JSONArray) {
            //incase of networks  it is a list
            assertThat(((JSONArray) redactedElement).get(0), is(multipleValues.split(",")[0]));
        } else {
            assertThat(redactedElement, is(multipleValues.split(",")[0]));
        }

    }

    private void createEntityRedactionObjects() {
        databaseHelper.addObject("" +
                "mntner:        INCOMING-MNT\n" +
                "admin-c:       TP1-TEST\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        INCOMING-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");

        databaseHelper.addObject("" +
                "mntner:        INCOMING2-MNT\n" +
                "admin-c:       TP1-TEST\n" +
                "auth:          MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:        INCOMING2-MNT\n" +
                "created:         2011-07-28T00:35:42Z\n" +
                "last-modified:   2019-02-28T10:14:46Z\n" +
                "source:        TEST");
    }

    private void loadIpv4RelationTreeExample(){
        /*
                                   +--------------+
                                   | 192.0.2.0/24 |
                                   +--------------+
                                      /        \
                         +--------------+    +----------------+
                         | 192.0.2.0/25 |    | 192.0.2.128/25 |
                         +--------------+    +----------------+
                            /                   /          \
                +--------------+   +----------------+  +----------------+
                | 192.0.2.0/28 |   | 192.0.2.128/26 |  | 192.0.2.192/26 |
                +--------------+   +----------------+  +----------------+
                   /
            +--------------+
            | 192.0.2.0/32 |
            +--------------+
        */

        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.255\n" + // /24
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        // One branch
        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.127\n" + // /25
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.0\n" + // /32
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ASSIGNED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "inetnum:      192.0.2.0 - 192.0.2.15\n" + // /28
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");


        //Another branch

        databaseHelper.addObject("" +
                "inetnum:      192.0.2.128 - 192.0.2.255\n" + // /25
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "inetnum:      192.0.2.128 - 192.0.2.191\n" + // /26
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "inetnum:      192.0.2.192 - 192.0.2.255\n" + // /26
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        ipTreeUpdater.rebuild();
    }


    private void loadIpv4RelationDomainExample(){
        databaseHelper.addObject("" +
                "inetnum:      192.0.0.0 - 192.0.255.255\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "inetnum:      192.0.2.1 - 192.0.2.1\n" +
                "netname:      TEST-NET-NAME\n" +
                "descr:        TEST network\n" +
                "country:      NL\n" +
                "language:     en\n" +
                "tech-c:       TP1-TEST\n" +
                "status:       ALLOCATED PA\n" +
                "mnt-by:       OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:       TEST");

        databaseHelper.addObject("" +
                "domain:        0.192.in-addr.arpa\n" +
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

        databaseHelper.addObject("" +
                "domain:        2.0.192.in-addr.arpa\n" +
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


        databaseHelper.addObject("" +
                "domain:        1.2.0.192.in-addr.arpa\n" +
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
    }

    private void loadIpv6RelationTreeExample(){
        /*
                                        +--------------+
                                        |      /0      |
                                        +--------------+
                  /                                                         \
             +--------------+                                           +----------------+
             |   FC00::/7   |                                           |   2000::/3     |
             +--------------+                                           +----------------+
                 /        \                                             /        |         |       \
 +-----------------+   +-------------------+                    +------------+  +-----------+  +-----------+  +-----------+
 |    FC00::/8     |   |    FD00::/8       |                    | 2001::/16  |  | 2400::/12 |  | 2600::/12 |  | 2800::/12 |
 +-----------------+   +-------------------+                    +------------+  +-----------+  +-----------+  +-----------+
                                                                    /    |
                                                        +--------------+  +--------------+
                                                        | 2001:db8::/32 |  | 2001::/23   |
                                                        +--------------+  +--------------+
        */

        databaseHelper.addObject("" +
                "inet6num:       2000::/3\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");


        // One branch
        databaseHelper.addObject("" +
                "inet6num:       2001::/16\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");


        databaseHelper.addObject("" +
                "inet6num:       2001:db8::/32\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");


        databaseHelper.addObject("" +
                "inet6num:       2001::/23\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");


        databaseHelper.addObject("" +
                "inet6num:       2400::/12\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        databaseHelper.addObject("" +
                "inet6num:       2600::/12\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        databaseHelper.addObject("" +
                "inet6num:       2800::/12\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");


        databaseHelper.addObject("" +
                "inet6num:       FC00::/7\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        databaseHelper.addObject("" +
                "inet6num:       FC00::/8\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        databaseHelper.addObject("" +
                "inet6num:       FD00::/8\n" +
                "netname:        TEST\n" +
                "descr:          The whole IPv6 address space\n" +
                "country:        NL\n" +
                "tech-c:         TP1-TEST\n" +
                "admin-c:        TP1-TEST\n" +
                "status:         ALLOCATED-BY-LIR\n" +
                "mnt-by:         OWNER-MNT\n" +
                "created:         2022-08-14T11:48:28Z\n" +
                "last-modified:   2022-10-25T12:22:39Z\n" +
                "source:         TEST");

        ipTreeUpdater.rebuild();
    }
}
