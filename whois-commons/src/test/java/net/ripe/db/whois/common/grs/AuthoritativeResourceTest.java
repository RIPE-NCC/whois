package net.ripe.db.whois.common.grs;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AuthoritativeResourceTest {
    Logger logger = LoggerFactory.getLogger(AuthoritativeResourceTest.class);
    @Rule public TemporaryFolder folder = new TemporaryFolder();

    @Test(expected = IllegalArgumentException.class)
    public void unknown_file() throws IOException {
        AuthoritativeResource.loadFromFile(logger, "unknown", folder.getRoot().toPath().resolve("unknown"));
    }

    @Test
    public void empty_file() throws IOException {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromFile(logger, "RIPE-GRS", folder.newFile().toPath());
        assertThat(resourceData.getNrAutNums(), is(0));
        assertThat(resourceData.getNrInetnums(), is(0));
        assertThat(resourceData.getNrInet6nums(), is(0));
        assertThat(resourceData.isEmpty(), is(true));
    }

    @Test
    public void load_apnic() throws IOException {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "APNIC-GRS", getScanner("delegated-apnic-extended-latest"));
        assertThat(resourceData.getNrAutNums(), is(9876));
        assertThat(resourceData.getNrInetnums(), is(22204));
        assertThat(resourceData.getNrInet6nums(), is(18909));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void load_unexpected_source() throws IOException {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "TEST-GRS", new Scanner("" +
                "ripencc|EU|asn|7|1|19930901|allocated\n" +
                "ripencc|EU|asn|28|10|19930901|allocated\n" +
                "ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated\n" +
                "ripencc|EU|ipv4|2.16.0.0|524288|20100910|allocated\n" +
                "ripencc|DE|ipv6|2001:608::|32|19990812|allocated\n" +
                "ripencc|NL|ipv6|2001:610::|32|19990819|allocated\n"));

        assertThat(resourceData.getNrAutNums(), is(0));
        assertThat(resourceData.getNrInetnums(), is(0));
        assertThat(resourceData.getNrInet6nums(), is(0));
        assertThat(resourceData.isEmpty(), is(true));
    }

    @Test
    public void load_ripe() throws IOException {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", getScanner("delegated-ripencc-extended-latest"));
        assertThat(resourceData.getNrAutNums(), is(28184));
        assertThat(resourceData.getNrInetnums(), is(49504));
        assertThat(resourceData.getNrInet6nums(), is(33535));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void load_country_ignored() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|*|asn|7|1|19930901|allocated\n" +
                "ripencc|EU|asn|28|1|19930901|allocated\n"));

        assertThat(resourceData.getNrAutNums(), is(1));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS7")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS28")), is(true));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void load_type_unexpected() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|EU|nsa|7|1|19930901|allocated\n" +
                "ripencc|EU|asn|28|1|19930901|allocated\n"));

        assertThat(resourceData.getNrAutNums(), is(1));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS7")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS28")), is(true));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void load_start_invalid() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|EU|asn|a|1|19930901|allocated\n" +
                "ripencc|EU|asn|28|1|19930901|allocated\n"));

        assertThat(resourceData.getNrAutNums(), is(1));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS7")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS28")), is(true));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void isMaintainedInRirSpace_empty_file() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(""));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS123")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 10.0.0.0 - 10.255.255.255")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inet6num: 2001::/20")), is(false));
        assertThat(resourceData.isEmpty(), is(true));
    }

    @Test
    public void isMaintainedByRir_empty_file() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(""));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS123")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("10.0.0.0 - 10.255.255.255")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001::/20")), is(false));
    }

    @Test
    public void isMaintainedInRirSpace_invalid_resource() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(""));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: 12345")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 0")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inet6num: 0")), is(false));
        assertThat(resourceData.isEmpty(), is(true));
    }

    @Test
    public void isMaintainedByRir_invalid_resource() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(""));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("12345")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("0")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("0")), is(false));
    }

    @Test
    public void isMaintainedInRirSpace_no_resource() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(""));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("mntner: DEV-MNT")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("person: Test Person\nnic-hdl: TP1-TEST")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("organisation: ORG-TOL1-TEST")), is(true));
        assertThat(resourceData.isEmpty(), is(true));
    }

    @Test
    public void isMaintainedByRir_no_resource() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(""));
        assertThat(resourceData.isMaintainedByRir(ObjectType.MNTNER, ciString("12345")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.PERSON, ciString("0")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.ORGANISATION, ciString("0")), is(true));
    }

    @Test
    public void isMaintainedInRirSpace_aut_num() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|EU|asn|7|1|19930901|allocated\n" +
                "ripencc|EU|asn|28|10|19930901|allocated\n"));

        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS6")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS7")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS8")), is(false));

        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS27")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS28")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS29")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS37")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS38")), is(false));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void isMaintainedByRir_aut_num() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|EU|asn|7|1|19930901|allocated\n" +
                "ripencc|EU|asn|28|10|19930901|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS6")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS7")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS8")), is(false));

        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS27")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS28")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS29")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS37")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS38")), is(false));
    }

    @Test
    public void isMaintainedInRirSpace_ipv4() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated\n" +
                "ripencc|EU|ipv4|2.16.0.0|524288|20100910|allocated\n"));

        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 1.0.0.0 - 1.255.255.255")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 2.0.0.0 - 2.0.0.0")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 2.0.0.0 - 2.0.0.1")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 2.0.0.1 - 2.0.0.1")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 2/12")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 2/19")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 2/11")), is(false));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void isMaintainedByRir_ipv4() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|FR|ipv4|2.0.0.0|1048576|20100712|allocated\n" +
                "ripencc|EU|ipv4|2.16.0.0|524288|20100910|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("1.0.0.0 - 1.255.255.255")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("2.0.0.0 - 2.0.0.0")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("2.0.0.0 - 2.0.0.1")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("2.0.0.1 - 2.0.0.1")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("2/12")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("2/19")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("2/11")), is(false));
    }

    // An 8K allocation in database, mapping to two 4K allocations in the resources file
    @Test
    public void isMaintainedByRir_ipv4_multiple_allocations() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(
                "ripencc|GB|ipv4|80.253.128.0|4096|20011213|allocated\n" +
                "ripencc|IR|ipv4|80.253.144.0|4096|20020528|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("80.253.128.0 - 80.253.159.255")), is(true));
    }

    // An 8K allocation in database, mapping to two 2K allocations and one 4K allocation in the resources file
    @Test
    public void isMaintainedByRir_ipv4_multiple_sized_allocations() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(
                "ripencc|GB|ipv4|80.253.128.0|2048|20011213|allocated\n" +
                "ripencc|GB|ipv4|80.253.136.0|2048|20011213|allocated\n" +
                "ripencc|IR|ipv4|80.253.144.0|4096|20020528|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("80.253.128.0 - 80.253.159.255")), is(true));
    }

    // An 8K allocation in database, mapping to two 4K allocation in the resources file, with an additional 2K sub-allocation
    @Test
    public void isMaintainedByRir_ipv4_multiple_sized_encompassing_allocations() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(
                "ripencc|GB|ipv4|80.253.128.0|4096|20011213|allocated\n" +
                "ripencc|GB|ipv4|80.253.136.0|2048|20011213|allocated\n" +
                "ripencc|IR|ipv4|80.253.144.0|4096|20020528|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("80.253.128.0 - 80.253.159.255")), is(true));
    }

    // An 8K allocation in database, but the 2K allocations in the resources file contains a gap
    @Test
    public void isMaintainedByRir_ipv4_multiple_allocations_with_gap() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner(
                "ripencc|GB|ipv4|80.253.128.0|2048|20011213|allocated\n" +
                "ripencc|IR|ipv4|80.253.136.0|2048|20020528|allocated\n" +
                "ripencc|IR|ipv4|80.253.144.0|2048|20020528|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("80.253.128.0 - 80.253.159.255")), is(false));
    }

    @Test
    public void isMaintainedInRirSpace_ipv6() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|DE|ipv6|2001:608::|32|19990812|allocated\n" +
                "ripencc|NL|ipv6|2001:610::|32|19990819|allocated\n"));

        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inet6num: 2001::")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inet6num: 2002:608::")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inet6num: 2001:608::")), is(true));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inet6num: 2001:608:abcd::")), is(true));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void isMaintainedByRir_ipv6() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|DE|ipv6|2001:608::|32|19990812|allocated\n" +
                "ripencc|NL|ipv6|2001:610::|32|19990819|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001::")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2002:608::")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001:608::/32")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001:608:abcd::")), is(false));
        assertThat(resourceData.isEmpty(), is(false));
    }

    @Test
    public void isMaintainedByRir_ipv6_multiple_allocations() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|DE|ipv6|2001:2002:2003:2004::|64|19990812|allocated\n" +
                "ripencc|DE|ipv6|2001:2002:2003:2005::|64|19990812|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001:2002:2003:2004::/63")), is(true));
    }

    @Test
    public void isMaintainedByRir_ipv6_multiple_sized_encompassing_allocations() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|DE|ipv6|2001:2002:2003:2004::|64|19990812|allocated\n" +
                "ripencc|DE|ipv6|2001:2002:2003:2004:001::|65|19990812|allocated\n" +
                "ripencc|DE|ipv6|2001:2002:2003:2005::|64|19990812|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001:2002:2003:2004::/63")), is(true));
    }

    @Test
    public void isMaintainedByRir_ipv6_multiple_allocations_with_gap() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("" +
                "ripencc|DE|ipv6|2001:2002:2003:2004:001::|65|19990812|allocated\n" +
                "ripencc|DE|ipv6|2001:2002:2003:2005::|64|19990812|allocated\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001:2002:2003:2004::/64")), is(false));
    }

    @Test
    public void available_resources() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "ARIN-GRS", new Scanner("" +
                "arin||ipv4|162.218.216.0|337920||available|\n" +
                "arin||ipv6|2620:106:c100::|40||available|\n" +
                "arin||asn|62681|807||available|\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS62681")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("162.218.216.0 - 162.223.255.255")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2620:0106:c100::/40")), is(true));
    }

    @Test
    public void reserved_resource() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "ARIN-GRS", new Scanner("" +
                "arin||ipv4|23.128.0.0|4194304||reserved|\n" +
                "arin||ipv6|2620:1d1::|32||reserved|\n" +
                "arin||asn|2733|1||reserved|\n"));

        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS2733")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("23.128.0.0/10")), is(true));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2620:1d1::/32")), is(true));
    }

    @Test
    public void as_always_parsed_uppercase() {
        final AuthoritativeResource resourceData = AuthoritativeResource.loadFromScanner(logger, "TEST-GRS", new Scanner("" +
                "test|EU|asn|7|1|19930901|allocated\n" +
                "test|EU|asn|28|2|19930901|allocated\n"));
        final List<String> asnums = Lists.newArrayList(Iterables.transform(resourceData.getAutNums(), new Function<CIString, String>() {
            @Override
            public String apply(CIString input) {
                return input.toString();
            }
        }));
        assertThat(asnums, hasSize(3));
        assertThat(asnums, containsInAnyOrder("AS7", "AS28", "AS29"));
    }

    @Test
    public void isMaintainedInRirSpace_unknown_data() {
        final AuthoritativeResource resourceData = AuthoritativeResource.unknown();
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("aut-num: AS6")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inetnum: 1.0.0.0 - 1.255.255.255")), is(false));
        assertThat(resourceData.isMaintainedInRirSpace(RpslObject.parse("inet6num: 2001::")), is(false));
        assertThat(resourceData.isEmpty(), is(true));
    }

    @Test
    public void isMaintainedByRir_unknown_data() {
        final AuthoritativeResource resourceData = AuthoritativeResource.unknown();
        assertThat(resourceData.isMaintainedByRir(ObjectType.AUT_NUM, ciString("AS6")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INETNUM, ciString("1.0.0.0 - 1.255.255.255")), is(false));
        assertThat(resourceData.isMaintainedByRir(ObjectType.INET6NUM, ciString("2001::")), is(false));
        assertThat(resourceData.isEmpty(), is(true));
    }

    @Test
    public void getResources_unknown() {
        assertThat(AuthoritativeResource.unknown().getResourceTypes(),
                containsInAnyOrder(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM));
    }

    @Test
    public void getResources_scanner() {
        assertThat(AuthoritativeResource.loadFromScanner(logger, "RIPE-GRS", new Scanner("")).getResourceTypes(),
                containsInAnyOrder(ObjectType.AUT_NUM, ObjectType.INETNUM, ObjectType.INET6NUM));
    }

    private Scanner getScanner(final String resourceName) throws IOException {
        final Resource resource = new ClassPathResource(String.format("grs/%s.gz", resourceName));
        return new Scanner(new GZIPInputStream(resource.getInputStream()));
    }
}
