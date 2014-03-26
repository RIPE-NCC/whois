package net.ripe.db.whois.common.rpsl;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AttributeSyntaxTest {

    @Test
    public void adminc() {
        verifySuccess(ObjectType.AS_SET, AttributeType.ADMIN_C, "HIA1-AFRINIC");
        verifySuccess(ObjectType.AS_SET, AttributeType.ADMIN_C, "HIA1-ARIN");
    }

    @Test
    public void alias() {
        verifySuccess(ObjectType.INET_RTR, AttributeType.ALIAS, "Moscow-BNS003-Gig0-1-707.free.net");
        verifySuccess(ObjectType.INET_RTR, AttributeType.ALIAS, "RIPE-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test-test.net");
        verifySuccess(ObjectType.INET_RTR, AttributeType.ALIAS, "RiPE.net");

        verifyFailure(ObjectType.INET_RTR, AttributeType.ALIAS, "RIPE-test_.net");
    }

    @Test
    public void asBlock() {
        verifyFailure(ObjectType.AS_BLOCK, AttributeType.AS_BLOCK, "AS7-8");
        verifyFailure(ObjectType.AS_BLOCK, AttributeType.AS_BLOCK, "AS7 - AS6");
        verifyFailure(ObjectType.AS_BLOCK, AttributeType.AS_BLOCK, "AS7 - ");

        verifyFailure(ObjectType.AS_BLOCK, AttributeType.AS_BLOCK, "AS7");
        verifySuccess(ObjectType.AS_BLOCK, AttributeType.AS_BLOCK, "AS4-AS5");
        verifySuccess(ObjectType.AS_BLOCK, AttributeType.AS_BLOCK, "AS7 - AS7");
        verifySuccess(ObjectType.AS_BLOCK, AttributeType.AS_BLOCK, "AS1877 - AS1901");
    }

    @Test
    public void asSet() {
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, "AS");
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, "AS1");
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, "AS TESTNET");
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, "AS3320:");
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, ":AS-TEST-PLOT-TEST-FROM-AS6724");
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, "AS-SET-TEST,AS5320:AS-AUTH-PLOT-TEST-FROM-AS4724");
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, "AS40696:AS51358");
        verifyFailure(ObjectType.AS_SET, AttributeType.AS_SET, "AS4034");

        verifySuccess(ObjectType.AS_SET, AttributeType.AS_SET, "AS-TESTNET");
        verifySuccess(ObjectType.AS_SET, AttributeType.AS_SET, "as-test-software");
        verifySuccess(ObjectType.AS_SET, AttributeType.AS_SET, "AS-SET-TEST");
        verifySuccess(ObjectType.AS_SET, AttributeType.AS_SET, "AS5320:AS-TEST-PLOT-TEST-FROM-AS4724");
        verifySuccess(ObjectType.AS_SET, AttributeType.AS_SET, "AS-SET-TEST-1");
        verifySuccess(ObjectType.AS_SET, AttributeType.AS_SET, "AS1:AS-FOO:AS-BAR");
        verifySuccess(ObjectType.AS_SET, AttributeType.AS_SET, "AS1:AS-EXPORT:AS2");
    }

    @Test
    public void aggrBndry() {
        verifySuccess(ObjectType.ROUTE, AttributeType.AGGR_BNDRY, "AS8764 or AS8689 or AS31006 or AS34037");
        verifySuccess(ObjectType.ROUTE, AttributeType.AGGR_BNDRY, "AS34403");
        verifySuccess(ObjectType.ROUTE, AttributeType.AGGR_BNDRY, "AS8764 AND AS34175");
        verifySuccess(ObjectType.ROUTE, AttributeType.AGGR_BNDRY, "AS8764 EXcePT AS34175");

        verifyFailure(ObjectType.ROUTE, AttributeType.AGGR_BNDRY, "AS8764 AND OR AS34175");
        verifyFailure(ObjectType.ROUTE, AttributeType.AGGR_BNDRY, "oR AS8764");
    }

    @Test
    public void aggrMtd() {
        verifySuccess(ObjectType.ROUTE, AttributeType.AGGR_MTD, "outbound AS-ANY");
        verifySuccess(ObjectType.ROUTE, AttributeType.AGGR_MTD, " inBound");
        verifySuccess(ObjectType.ROUTE, AttributeType.AGGR_MTD, "outbound AS-ANY or AS34175 except AS-SET-TEST");

        verifyFailure(ObjectType.ROUTE, AttributeType.AGGR_MTD, "inbound AS8764");
        verifyFailure(ObjectType.ROUTE, AttributeType.AGGR_MTD, "outbound except AS-SET-TEST");
    }

    @Test
    public void assignmentSize() throws Exception {
        verifyFailure(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "");
        verifyFailure(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "A");
        verifyFailure(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "A1");
        verifyFailure(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "1A");
        verifyFailure(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "-1");

        verifySuccess(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "0");
        verifySuccess(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "00");
        verifySuccess(ObjectType.INET6NUM, AttributeType.ASSIGNMENT_SIZE, "1234");
    }

    @Test
    public void autNum() {
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AAS3255");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS-3255");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS 3255");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "3255");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS4294967296");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS42:RS43");

        verifySuccess(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS0");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS3255");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.AUT_NUM, "AS4294967295");
    }

    @Test
    public void auth() throws Exception {
        verifySuccess(ObjectType.MNTNER, AttributeType.AUTH, "AUTO-666");

        verifySuccess(ObjectType.MNTNER, AttributeType.AUTH, "x509-1");
        verifySuccess(ObjectType.MNTNER, AttributeType.AUTH, "X509-12345678901234567890");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "X509-01");
        verifySuccess(ObjectType.MNTNER, AttributeType.AUTH, "Md5-Pw $1$a$bcdefghijklmnopqrstuvw");
        verifySuccess(ObjectType.MNTNER, AttributeType.AUTH, "mD5-pW $1$abc012./$./01234567890123456789");
        verifySuccess(ObjectType.MNTNER, AttributeType.AUTH, "pgpkey-01234567");
        verifySuccess(ObjectType.MNTNER, AttributeType.AUTH, "SSO test2@ripe.net");

        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "x509-ab./");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "x509-ab./");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "pgpkey-ghij./12");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "SSO tes,,,.....");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "SSO ");

        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "md5-pw bcdefghijklmnopqrstuvwx");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "md5-pw $1$abcdefghi$bcdefghijklmnopqrstuvwx");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "x509-");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "x509-678901234567890123456789012345678901234567890a");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "md5-pw $1$a$bcdefgijklmnopqrstuvw");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "md5-pw $1$abc012./$./012345678901234567890");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "pgpkey-0123456");
        verifyFailure(ObjectType.MNTNER, AttributeType.AUTH, "pgpkey-012345678");
    }

    @Test
    public void changed() throws Exception {
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "a@a");
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "a.a.a");

        verifySuccess(ObjectType.PERSON, AttributeType.CHANGED, "foo@provider.com");
        verifySuccess(ObjectType.PERSON, AttributeType.CHANGED, "a@a.a");
        verifySuccess(ObjectType.PERSON, AttributeType.CHANGED, "'anthingcan1242go!@(&)^!(&@^21here\"@0.2345678901234567890123456789012345678901");
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "0@2.45678901234567890123456789012345678901234567890123456789012345678901234567890123456789");

        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "a@a 20010101");
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "a.a.a 20010101");
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "a@a.a 2001010");
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "foo@provider.com 01234567");

        verifySuccess(ObjectType.PERSON, AttributeType.CHANGED, "foo@provider.com 20010101");
        verifySuccess(ObjectType.PERSON, AttributeType.CHANGED, "a@a.a 20010101");
        verifySuccess(ObjectType.PERSON, AttributeType.CHANGED, "'anthingcan1242go!@(&)^!(&@^21here\"@0.2345678901234567890123456789012345678901 20010101");
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "0@2.45678901234567890123456789012345678901234567890123456789012345678901234567890 20010101");
        verifyFailure(ObjectType.PERSON, AttributeType.CHANGED, "a@a.a 2001010101");
    }

    @Test
    public void components() throws Exception {
        verifySuccess(ObjectType.ROUTE, AttributeType.COMPONENTS, "{217.113.0.0/19^19-24 }");
        verifySuccess(ObjectType.ROUTE, AttributeType.COMPONENTS, "atomic");

        verifyFailure(ObjectType.ROUTE, AttributeType.COMPONENTS, "atomic OR {217.113.0.0/19^19-24 }");
    }

    @Test
    public void country() throws Exception {
        verifyFailure(ObjectType.INETNUM, AttributeType.COUNTRY, "a");
        verifyFailure(ObjectType.INETNUM, AttributeType.COUNTRY, "aaa,");

        verifySuccess(ObjectType.INETNUM, AttributeType.COUNTRY, "nl");
        verifySuccess(ObjectType.INETNUM, AttributeType.COUNTRY, "Nl");
        verifySuccess(ObjectType.INETNUM, AttributeType.COUNTRY, "en");
        verifySuccess(ObjectType.INETNUM, AttributeType.COUNTRY, "EN");
    }

    @Test
    public void descr() {
        verifySuccess(ObjectType.ROUTE, AttributeType.DESCR, "");

        StringBuilder builder = new StringBuilder();
        for (char i = 0; i < Character.MAX_VALUE; i++) {
            builder.append(i);
        }

        verifySuccess(ObjectType.ROUTE, AttributeType.DESCR, builder.toString());
    }

    @Test
    public void domain() {
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "-core.swip.net");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "icm-$-london-1.icp.net");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "");
        verifySuccess(ObjectType.DOMAIN, AttributeType.DOMAIN, "1.0.0.0.2.0.0.0.3.0.0.0.4.0.0.0.5.0.0.0.6.0.0.0.7.0.0.0.8.0.0.0.9.0.0.0.0.0.0.0.1.0.0.0.2.0.0.0.3.0.0.0.4.0.0.0.5.0.0.0.e164.arpa.");

        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "Amsterdam.ripe.net");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "Amsterdam.in-addr.arpa");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "01-03.0.0.193.in-addr.arpa");

        verifySuccess(ObjectType.DOMAIN, AttributeType.DOMAIN, "36.116.62.in-addr.arpa");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz.abcdefghijklmnopqrstuvwxyz.abcdefghijklmnopq.e164.arpa");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "alpha.e164.arpa.");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DOMAIN, "23024.194.196.in-addr.arpa");
        verifySuccess(ObjectType.DOMAIN, AttributeType.DOMAIN, "4.3.2.1.6.7.9.8.6.4.e164.arpa.");
        verifySuccess(ObjectType.DOMAIN, AttributeType.DOMAIN, "2.0.0.1.2.0.0.2.2.0.0.3.2.0.0.4.2.0.0.5.2.0.0.6.2.0.0.7.2.0.0.8.ip6.arpa");

        verifySuccess(ObjectType.DOMAIN, AttributeType.DS_RDATA, "26954 8 4 502BDAEFD8944CC0B47A100425FE1D3CE44235E5B6EA3E7C884D28C581D868E5DCEDD21EDF8CB45CDAE3F05B947BDED5");
        verifySuccess(ObjectType.DOMAIN, AttributeType.DS_RDATA, "1 1 1 1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890");
        verifyFailure(ObjectType.DOMAIN, AttributeType.DS_RDATA, "1 1 1 1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF1234567890ABCDEF12345678901");
    }

    @Test
    public void defaultAttr() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.DEFAULT, "to AS9004\n    action pref=100;\n    networks ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.DEFAULT, "to AS13237");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.DEFAULT, "to invalid");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.DEFAULT, "to AS4294967296");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.DEFAULT, "INVALID");
    }

    @Test
    public void email() throws Exception {
        verifyFailure(ObjectType.PERSON, AttributeType.E_MAIL, "a@a");
        verifyFailure(ObjectType.PERSON, AttributeType.E_MAIL, "a.a.a");

        verifySuccess(ObjectType.PERSON, AttributeType.E_MAIL, "foo@provider.com");
        verifySuccess(ObjectType.PERSON, AttributeType.E_MAIL, "a@a.a");
        verifySuccess(ObjectType.PERSON, AttributeType.E_MAIL, "'anthingcan1242go!@(&)^!(&@^21here\"@0.2345678901234567890123456789012345678901");
        verifyFailure(ObjectType.PERSON, AttributeType.E_MAIL, "0@2.45678901234567890123456789012345678901234567890123456789012345678901234567890");
    }

    @Test
    public void encryption() throws Exception {
        verifyFailure(ObjectType.IRT, AttributeType.ENCRYPTION, "PGPKEY-");
        verifyFailure(ObjectType.IRT, AttributeType.ENCRYPTION, "A6D57ECE");
        verifyFailure(ObjectType.IRT, AttributeType.ENCRYPTION, "X509-");
        verifyFailure(ObjectType.IRT, AttributeType.ENCRYPTION, "2606");
        verifyFailure(ObjectType.IRT, AttributeType.ENCRYPTION, "AUTO-");
        verifyFailure(ObjectType.IRT, AttributeType.ENCRYPTION, "123");

        verifySuccess(ObjectType.IRT, AttributeType.ENCRYPTION, "PGPKEY-A6D57ECE");
        verifySuccess(ObjectType.IRT, AttributeType.ENCRYPTION, "X509-2606");
        verifySuccess(ObjectType.IRT, AttributeType.ENCRYPTION, "AUTO-123");
    }

    @Test
    public void export() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "TO AS0 ANNOUNCE ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "TO AS0 ANNOUNCE AS1");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "TO AS0 ANNOUNCE AS-T1");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS-FOO announce ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "TO AS4294967295 ANNOUNCE ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS31133 announce as-infolada");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS8968 action pref=100; announce {151.96.0.0/16}{151.96.0.0/17}");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS8968 action pref=100; announce any");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS31133 announce AS-i1");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS35805 announce as-set-wanex");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS12840 announce AS-TEST");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to prng-as5408-ibgp announce ANY;");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS20485 action pref=100; announce AS-ORTEL");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS42688 at 195.250.64.11 action med = 0; announce AS8226:AS-ALL {0.0.0.0/0}");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS8308:AS-Cust-transit announce ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS8308:AS-Peerings announce <AS-NASK:AS-Customers$>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS22302:AS-PEERS announce AS22302 AS22302:AS-CUSTOMERS");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS5617 to AS8246 to AS15833 announce AS12324 AS12346 AS12346:AS-transit");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS8881 action aspath.prepend (AS12355); announce AS-TEST-V4 AND NOT fltr-bogons");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS34087 213.167.114.122 at 213.167.114.121 action pref=100; to AS34087 announce {0.0.0.0/0}");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS6777 action community .= { 6777:6777, 6777:8473}; announce AS-NIANET");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS47254 TNS announce ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS9075 ANNOUNCE ANY AND NOT RS-IBXBOGONS AND NOT {0.0.255.0/0^25-32}");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS8308:AS-Peerings announce <[AS64512-AS65534]>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "to AS8308:AS-Peerings announce <[AS64512 - AS65534]>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT, "protocol BGP4 into OSPF to AS1 announce AS2");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT, "TO AS4294967296 ANNOUNCE ANY");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT, "TO ASA ANNOUNCE ANY");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT, "INVALID");
    }

    @Test
    public void export_via() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "afi ipv4.unicast AS99070 to AS123456 announce AS-SU-LOCAL");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "afi ipv6.unicast AS88262 at 2001:67c:20d0:fffe:ffff:ffff:ffff:fffa to AS123986 action pref=100; announce AS-SU-LOCAL");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "                     AS12956  \t84.16.8.225 at 84.16.8.226\t to AS986 AS123 to AS234 announce AS-TEST AND NOT {0.0.0.0/0}");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "afi any AS9070 62.44.108.66 at 62.44.108.65 to AS456789 announce AS-SU-LOCAL");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "afi ipv4.unicast AS99070 to AS123456 action pref=100; pref=10; announce AS-SU-LOCAL");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "ipv4.unicast AS99070 to AS123456 announce AS-SU-LOCAL");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "afi ipv4.unicast AS99070 to AS123456 to AS123457 announce AS-SU-LOCAL");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "ipv4.unicast AS99070 to AS123456 to AS123457 announce AS-SU-LOCAL");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "afi ipv4.unicast AS99070 to AS123456 action pref=100; action pref=10; announce AS-SU-LOCAL");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.EXPORT_VIA, "INVALID");
    }

    @Test
    public void exportComps() throws Exception {
        verifySuccess(ObjectType.ROUTE, AttributeType.EXPORT_COMPS, "{193.130.196.0/24, 193.130.197.0/24}");
        verifySuccess(ObjectType.ROUTE, AttributeType.EXPORT_COMPS, "{ 41.190.128.0/19^19-24 }");
        verifySuccess(ObjectType.ROUTE, AttributeType.EXPORT_COMPS, "{194.55.167.0/24}");
        verifySuccess(ObjectType.ROUTE6, AttributeType.EXPORT_COMPS, "{ 2001:610:140::/48 }");
        verifySuccess(ObjectType.ROUTE6, AttributeType.EXPORT_COMPS, "{ 2001:610:140::/48 ,  2620:104:4007::/48 }");

        verifyFailure(ObjectType.ROUTE, AttributeType.EXPORT_COMPS, "{193.130.196.0/24}, {193.130.197.0/24}");
        verifyFailure(ObjectType.ROUTE6, AttributeType.EXPORT_COMPS, "{ 2001:610:140::/48 }, { 2620:104:4007::/48 }");
    }

    @Test
    public void filterSet() {
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "fltr");
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "Fltr1");
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "fltr HEPNET");
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "AS20773:");
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER_SET, ":FLTR-AUTH-PLOT-TEST-FROM-RS4724");

        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "fltr-HEPNET");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "FLTR-HEPNET");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "AS20773:fltr-HOSTEUROPE");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "AS20773:FLTR-HOSTEUROPE");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER_SET, "FLTR-RIPE:FLTR-TEST:FLTR-IPV6");
    }

    @Test
    public void filter() throws Exception {
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "AS1");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "as1 As2 AS3");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "as1 aNd As2");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "ANY AND NoT (AS1:FLTR-GLOBAL-UPSTREAMS AS2:RS-BOGUS)");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "as1 aNd As2");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "AS1 AND <[AS2-AS9]>");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "{10.0.0.0/8^+, 192.168.0.0/16^+}");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "{}");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "community(8856:10034)");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "<AS1>");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "<^AS1>");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "<AS2$>");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "<^AS1 AS2 AS3$>");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "<^AS1 .* AS2$>");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "AS226 AND NOT {128.9.0.0/16}");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.FILTER, "NOT {128.9.0.0/16, 128.8.0.0/16}");

        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER, "{ 192.168/16^+");
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER, "{ 999/8^+ }");
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER, "invalid");
        verifyFailure(ObjectType.FILTER_SET, AttributeType.FILTER, "{ 192.168.0/16^+, 10/8^+ }");
    }

    @Test
    public void geoLoc() throws Exception {
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "90.90 90.90");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "abc 90 90");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "abc 90 def 180");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "10.10 200");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "-90");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "-90,");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "a b");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "-180 180");
        verifyFailure(ObjectType.INETNUM, AttributeType.GEOLOC, "123.321 -123.123");

        verifySuccess(ObjectType.INETNUM, AttributeType.GEOLOC, "90 90");
        verifySuccess(ObjectType.INETNUM, AttributeType.GEOLOC, "+90 +90");
        verifySuccess(ObjectType.INETNUM, AttributeType.GEOLOC, "1.2 3.4");
        verifySuccess(ObjectType.INETNUM, AttributeType.GEOLOC, "-10 -10");
        verifySuccess(ObjectType.INETNUM, AttributeType.GEOLOC, "-90 90");
        verifySuccess(ObjectType.INETNUM, AttributeType.GEOLOC, "12 3");
        verifySuccess(ObjectType.INETNUM, AttributeType.GEOLOC, "24 -2");
    }

    @Test
    public void holes() {
        verifyFailure(ObjectType.ROUTE, AttributeType.HOLES, "100.100.100");
        verifyFailure(ObjectType.ROUTE, AttributeType.HOLES, "300.300.300.300");
        verifyFailure(ObjectType.ROUTE, AttributeType.HOLES, "0/100");
        verifyFailure(ObjectType.ROUTE, AttributeType.HOLES, "::0/0");
        verifyFailure(ObjectType.ROUTE, AttributeType.HOLES, "");
        verifyFailure(ObjectType.ROUTE, AttributeType.HOLES, ",");

        verifySuccess(ObjectType.ROUTE, AttributeType.HOLES, "192.168.1.10");
        verifySuccess(ObjectType.ROUTE, AttributeType.HOLES, "192.168.1.10,192.168.1.11");
        verifySuccess(ObjectType.ROUTE, AttributeType.HOLES, "0/0");

        verifyFailure(ObjectType.ROUTE6, AttributeType.HOLES, "");
        verifyFailure(ObjectType.ROUTE6, AttributeType.HOLES, "100.100.100");
        verifyFailure(ObjectType.ROUTE6, AttributeType.HOLES, "0/0");
        verifySuccess(ObjectType.ROUTE6, AttributeType.HOLES, "::0/0");
        verifySuccess(ObjectType.ROUTE6, AttributeType.HOLES, "2a00:c00::/48,2a00:c01::/48");
    }

    @Test
    public void ifaddr() {
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "193.63.94.2 masklen 32");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "0.0.0.0 masklen 32");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "193.63.94.2     masklen 0");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "193.63.94.2     masklen      0");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 action community.append(12456:20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "146.188.49.14 masklen 30 action community.append(12456:20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 action community.append(20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 action community.append     (12356:20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 action               community.append(12356:20);");

        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "2001:0658:0212::/48 masklEn 128");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "255.255.255.256 masklen 32");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "2001::");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 community.append(12456:20);");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 action community.append(12346:20)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 action");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3.4 masklen 30 action community.append();");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "1.2.3. masklen 30 action community.append(12346:20);");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "masklen action community.append(12356:20);");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "193.63.94.2 masklen 33");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "193.63.94.2 masklen -1");
        verifyFailure(ObjectType.INET_RTR, AttributeType.IFADDR, "193.63.94.2");
    }

    @Test
    public void importAttr() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, " from AS1717 accept ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS5388\n  action pref=50;\n   accept AS5388");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS2860:AS-TEST AND AS15525 accept <AS-TEST>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS34002 action pref=10; accept <^AS34002+$>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS6777 accept ANY AND NOT <[AS2-AS9]>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS-FOO accept PeerAS");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS-FOO accept peeras");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS2 action pref = 10; med = 0; community.append(10250, 3561:10); accept { 128.9.0.0/16 }\n");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS2 action pref = 10; med = 0; community.delete(100, NO_EXPORT, 3561:10); accept { 128.9.0.0/16 }\n");

        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS22302:AS-PEERS   accept (PeerAS)");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS22302:AS-PEERS   accept (PeerAS OR AS22302:AS-PEERS:PeerAS) AND NOT (FLTR-INOC-BOGUS OR FLTR-INOC-RFC)");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS22302:AS-CUSTOMERS   accept (PeerAS OR AS22302:AS-CUSTOMERS:PeerAS OR AS22302:RS-INOC:PeerAS) AND NOT (FLTR-INOC-BOGUS OR FLTR-INOC-RFC)");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS22302:AS-TRANSIT   accept PeerAS AND NOT (FLTR-INOC-BOGUS OR FLTR-INOC-RFC OR FLTR-INOC-MAXPRE)");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "protocol STATIC into BGP4 from AS1 action aspath.prepend(AS1, AS1); accept AS1:RS-STATIC-ROUTES");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS2 7.7.7.2 at 7.7.7.1 action pref = 1; dpa = 5; from AS2 action pref = 2; accept AS4");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT, " from invalid");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT, "from AS4294967296 accept any");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT, "INVALID");
    }

    @Test
    public void import_via() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "afi ipv6.unicast AS150607 2001:7f8:5b:3::1 at 2001:7f8:5b:3::2 from AS103357 action next_hop = 10.0.0.1; AS123 from AS-TOOL action pref=100; accept AS-EPIX");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "  afi ipv6.unicast AS15685 2001:7f8:14::6:1 at 2001:7f8:14::31:1 from AS-TEST action cost=50; accept ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "afi ipv6.unicast AS16777 from AS262144 accept <^[AS9002 AS31133 AS24940]>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "afi ipv6.unicast AS16777 from AS262144 accept <^[AS1002-AS1005]>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "afi ipv6.unicast AS16777 from AS262144 accept ANY ;");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "afi ipv6.unicast AS16777 from AS262144 action next_hop = 10.0.0.1; next_hop = 10.0.0.10; accept ANY");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "ipv6.unicast AS16777 from AS262144 accept ANY ;");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "afi ipv6.unicast AS16777 from AS262144");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "afi ipv6.unicast AS16777 from AS262144 action next_hop = 10.0.0.1; action next_hop = 10.0.0.10; accept ANY");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, " afi ipv6.unicast  AS3248  AS39560");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, " afi wonttell  AS3248 accept AS39560");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.IMPORT_VIA, "INVALID");
    }


    @Test
    public void inet_rtr() {
        verifyFailure(ObjectType.INET_RTR, AttributeType.INET_RTR, "-core.test.net");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INET_RTR, "icm-$-test-1.foo.net");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INET_RTR, "");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INET_RTR, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");

        verifySuccess(ObjectType.INET_RTR, AttributeType.INET_RTR, "icm-test-1.foo.net");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INET_RTR, "RIPE_NCC.icp.net");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INET_RTR, "Amsterdam.ripe.net");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INET_RTR, "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz.abcdefghijklmnopqrstuvwxyz.abcdefghijklmnopqrstuvwxyz");
    }

    @Test
    public void inetnum() {
        verifyFailure(ObjectType.INETNUM, AttributeType.INETNUM, "");
        verifyFailure(ObjectType.INETNUM, AttributeType.INETNUM, "100.100.100");
        verifyFailure(ObjectType.INETNUM, AttributeType.INETNUM, "300.300.300.300");
        verifyFailure(ObjectType.INETNUM, AttributeType.INETNUM, "0/100");
        verifyFailure(ObjectType.INETNUM, AttributeType.INETNUM, "::0/0");

        verifySuccess(ObjectType.INETNUM, AttributeType.INETNUM, "192.168.1.10");
        verifySuccess(ObjectType.INETNUM, AttributeType.INETNUM, "0/0");
    }

    @Test
    public void inet6num() {
        verifyFailure(ObjectType.INET6NUM, AttributeType.INET6NUM, "195.10.40.0/29");

        verifySuccess(ObjectType.INET6NUM, AttributeType.INET6NUM, "::0");
        verifySuccess(ObjectType.INET6NUM, AttributeType.INET6NUM, "::0/0");
        verifySuccess(ObjectType.INET6NUM, AttributeType.INET6NUM, "2001:1578:0200::/40");
        verifySuccess(ObjectType.INET6NUM, AttributeType.INET6NUM, "2a01:e0::/32");
        verifySuccess(ObjectType.INET6NUM, AttributeType.INET6NUM, "2a01:00e0::/32");
        verifySuccess(ObjectType.INET6NUM, AttributeType.INET6NUM, "2A01:E0::/32");
    }


    @Test
    public void inject() {
        verifySuccess(ObjectType.ROUTE, AttributeType.INJECT, "upon static");
        verifySuccess(ObjectType.ROUTE, AttributeType.INJECT, "upon HAVE-COMPONENTS {128.8.0.0/16, 128.9.0.0/16}");

        verifyFailure(ObjectType.ROUTE, AttributeType.INJECT, "AT AS42312");
    }

    @Test
    public void interfaceAttr() {
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001:1578:200:FFfF::2 masKlen 128");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "84.233.170.54 masklEn 32");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "255.255.255.255 masklEn 32");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "0.0.0.0 masklEn 32");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001:: masklEn 32");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001::1 masklen 30 action community.append(12356:20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001:67c:2e8:13:a8a9:745d:3ed8:94b0 masklen 30 action community.append(12456:20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "193.63.94.2     masklen 0");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "193.63.94.2     masklen      0");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 action community.append(12356:20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 action community.append(20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 action community.append     (12456:20);");
        verifySuccess(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 action               community.append(12456:20);");

        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001:0658:0212::/48 masklEn 128");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001::1 masklen 30 action community.append(12456:20)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001:1578:200:FFFF::2 masklen 129");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "84.233.170.54 masklen 33");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "255.255.255.256 masklEn 32");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "255.255.255.256 masklen 32");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001::");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 community.append(12456:20);");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 action community.append(12456:20)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 action");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3.4 masklen 30 action community.append();");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "1.2.3. masklen 30 action community.append(12456:20);");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "masklen action community.append(12456:20);");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "193.63.94.2 masklen 33");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "193.63.94.2 masklen -1");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "193.63.94.2");
        verifyFailure(ObjectType.INET_RTR, AttributeType.INTERFACE, "2001:0658:0212::/48 masklEn 128");
    }

    @Test
    public void keyCert() throws Exception {
        verifyFailure(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "PGPKEY-");
        verifyFailure(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "A6D57ECE");
        verifyFailure(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "X509-");
        verifyFailure(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "2606");
        verifyFailure(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "AUTO-");
        verifyFailure(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "123");

        verifySuccess(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "PGPKEY-A6D57ECE");
        verifySuccess(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "X509-2606");
        verifySuccess(ObjectType.KEY_CERT, AttributeType.KEY_CERT, "AUTO-123");
    }

    @Test
    public void language() throws Exception {
        verifyFailure(ObjectType.INETNUM, AttributeType.LANGUAGE, "a");
        verifyFailure(ObjectType.INETNUM, AttributeType.LANGUAGE, "aaa,");

        verifySuccess(ObjectType.INETNUM, AttributeType.LANGUAGE, "nl");
        verifySuccess(ObjectType.INETNUM, AttributeType.LANGUAGE, "Nl");
        verifySuccess(ObjectType.INETNUM, AttributeType.LANGUAGE, "en");
        verifySuccess(ObjectType.INETNUM, AttributeType.LANGUAGE, "EN");
    }

    @Test
    public void localAs() {
        verifyFailure(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AS");
        verifyFailure(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AAS3255");
        verifyFailure(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AS-3255");
        verifyFailure(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AS 3255");
        verifyFailure(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "3255");
        verifyFailure(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AS4294967296");

        verifySuccess(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AS0");
        verifySuccess(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AS3255");
        verifySuccess(ObjectType.INET_RTR, AttributeType.LOCAL_AS, "AS4294967295");
    }

    @Test
    public void memberOf() {
        verifyFailure(ObjectType.INET_RTR, AttributeType.MEMBER_OF, "AS-TESTNET");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS1");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS TESTNET");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS3320:");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, ":AS-AUTH-PLOT-FOO-FROM-AS2724");

        verifyFailure(ObjectType.ROUTE, AttributeType.MEMBER_OF, "AS-TESTNET");
        verifyFailure(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "AS-TESTNET");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS-TESTNET");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "as-foo-software");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS-SET-TEST");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS3320:AS-AUTH-PLOT-FOO-FROM-AS4724");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS-SET-FOO-1");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "AS-SET-TEST,AS3320:AS-AUTH-PLOT-FOO-FROM-AS4724");

        verifyFailure(ObjectType.ROUTE, AttributeType.MEMBER_OF, "RS");
        verifyFailure(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "RS");
        verifyFailure(ObjectType.ROUTE, AttributeType.MEMBER_OF, "RS1");
        verifyFailure(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "RS1");
        verifyFailure(ObjectType.ROUTE, AttributeType.MEMBER_OF, "RS TESTNET");
        verifyFailure(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "RS TESTNET");
        verifyFailure(ObjectType.ROUTE, AttributeType.MEMBER_OF, "AS20773:");
        verifyFailure(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "AS20773:");
        verifyFailure(ObjectType.ROUTE, AttributeType.MEMBER_OF, ":RS-AUTH-PLOT-FOO-FROM-RS2724");
        verifyFailure(ObjectType.ROUTE6, AttributeType.MEMBER_OF, ":RS-AUTH-PLOT-FOO-FROM-RS2724");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.MEMBER_OF, "RS-TESTNET");
        verifySuccess(ObjectType.ROUTE, AttributeType.MEMBER_OF, "RS-TESTNET");
        verifySuccess(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "RS-TESTNET");
        verifySuccess(ObjectType.ROUTE, AttributeType.MEMBER_OF, "AS20773:RS-HOSTEUROPE");
        verifySuccess(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "AS20773:RS-HOSTEUROPE");
        verifySuccess(ObjectType.ROUTE, AttributeType.MEMBER_OF, "AS702:RS-DE,AS702:RS-DE-PI");
        verifySuccess(ObjectType.ROUTE6, AttributeType.MEMBER_OF, "AS702:RS-DE,AS702:RS-DE-PI");
        verifyFailure(ObjectType.ROUTE, AttributeType.MEMBER_OF, "rtrs-as15469-edge");

        verifySuccess(ObjectType.INET_RTR, AttributeType.MEMBER_OF, "rtrs-as15469-edge");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MEMBER_OF, "rtrs-gasp2");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MEMBER_OF, "AS702:RS-DE,AS702:RS-DE-PI");
    }

    @Test
    public void members() throws Exception {
        verifySuccess(ObjectType.AS_SET, AttributeType.MEMBERS, "AS-TEST_TRANSIT");
        verifySuccess(ObjectType.AS_SET, AttributeType.MEMBERS, "AS2602");
        verifySuccess(ObjectType.AS_SET, AttributeType.MEMBERS, "AS2602, AS42909, AS51966");

        verifyFailure(ObjectType.AS_SET, AttributeType.MEMBERS, "AS2602, AS42909, AS51966,");
        verifyFailure(ObjectType.AS_SET, AttributeType.MEMBERS, ",AS2602, AS42909, AS51966");
        verifyFailure(ObjectType.AS_SET, AttributeType.MEMBERS, "AS2602, , AS42909, AS51966");
        verifyFailure(ObjectType.AS_SET, AttributeType.MEMBERS, "AS2602,,AS42909,AS51966");
        verifyFailure(ObjectType.AS_SET, AttributeType.MEMBERS, ",");
        verifyFailure(ObjectType.AS_SET, AttributeType.MEMBERS, "");

        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "195.66.224.0/23");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "212.7.192.0/19^19-24");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "RS-SIX-BLOG^16-24");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "172.184.0.0/13, 172.184.0.0/23^24");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "AS13646:RS-TEST");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "AS13646");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "AS-COFFEE,AS3333");
        verifyFailure(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "2a00:10C0::/32");
        verifyFailure(ObjectType.ROUTE_SET, AttributeType.MEMBERS, "FLTR-TESTNET");

        verifySuccess(ObjectType.RTR_SET, AttributeType.MEMBERS, "rmws-foo-bar.nu.bogus.net");
        verifySuccess(ObjectType.RTR_SET, AttributeType.MEMBERS, "ams-bar.foobar.net");
        verifySuccess(ObjectType.RTR_SET, AttributeType.MEMBERS, "213.232.64.1");

        verifyFailure(ObjectType.RTR_SET, AttributeType.MEMBERS, "2a00:10C0::/32");
    }

    @Test
    public void mp_members() throws Exception {
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MP_MEMBERS, "195.66.224.0/23");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MP_MEMBERS, "212.7.192.0/19^19-24");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MP_MEMBERS, "RS-SIX-BLOG^16-24");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MP_MEMBERS, "AS13646:RS-TEST");

        verifySuccess(ObjectType.ROUTE_SET, AttributeType.MP_MEMBERS, "172.184.0.0/13, 172.184.0.0/23^24");

        verifySuccess(ObjectType.RTR_SET, AttributeType.MP_MEMBERS, "rmws-foo-bar.nu.bogus.net");
        verifySuccess(ObjectType.RTR_SET, AttributeType.MP_MEMBERS, "ams-foo.foobar.net");
        verifySuccess(ObjectType.RTR_SET, AttributeType.MP_MEMBERS, "213.232.64.1");
        verifySuccess(ObjectType.RTR_SET, AttributeType.MP_MEMBERS, "2001:1578::/32, 2002:1578::/32, 213.232.64.1/24");

        verifySuccess(ObjectType.RTR_SET, AttributeType.MP_MEMBERS, "2a00:10C0::/32");
    }

    @Test
    public void mntBy() throws Exception {
        verifyFailure(ObjectType.IRT, AttributeType.MNT_BY, "A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_BY, "A1, A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_BY, "A12345678901234567890123456789012345678901234567890123456789012345678901234567890");

        verifySuccess(ObjectType.IRT, AttributeType.MNT_BY, "ABCDEfghijKLMNOprstuVWXYz0123456789_______________----_________________________0");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_BY, "A1");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_BY, "A1, A2");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_BY, "FOO-BAR-ZOT");
    }

    @Test
    public void mntDomains() throws Exception {
        verifyFailure(ObjectType.IRT, AttributeType.MNT_DOMAINS, "A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_DOMAINS, "A1, A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_DOMAINS, "A12345678901234567890123456789012345678901234567890123456789012345678901234567890");

        verifySuccess(ObjectType.IRT, AttributeType.MNT_DOMAINS, "ABCDEfghijKLMNOprstuVWXYz0123456789_______________----_________________________0");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_DOMAINS, "A1");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_DOMAINS, "A1, A2");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_DOMAINS, "FOO-BAR-ZOT");
    }

    @Test
    public void mntIrt() {
        verifyFailure(ObjectType.IRT, AttributeType.MNT_IRT, "irt");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_IRT, "irtAA");

        verifySuccess(ObjectType.IRT, AttributeType.MNT_IRT, "irt-1");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_IRT, "irt-1,irt-2");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_IRT, "irt-1-1");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_IRT, "irt-A_A");
    }

    @Test
    public void mntLower() throws Exception {
        verifyFailure(ObjectType.IRT, AttributeType.MNT_LOWER, "A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_LOWER, "A1, A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_LOWER, "A12345678901234567890123456789012345678901234567890123456789012345678901234567890");

        verifySuccess(ObjectType.IRT, AttributeType.MNT_LOWER, "ABCDEfghijKLMNOprstuVWXYz0123456789_______________----_________________________0");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_LOWER, "A1");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_LOWER, "A1, A2");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_LOWER, "FOO-BAR-ZOT");
    }

    @Test
    public void mntRef() throws Exception {
        verifyFailure(ObjectType.IRT, AttributeType.MNT_REF, "A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_REF, "A1, A");
        verifyFailure(ObjectType.IRT, AttributeType.MNT_REF, "A12345678901234567890123456789012345678901234567890123456789012345678901234567890");

        verifySuccess(ObjectType.IRT, AttributeType.MNT_REF, "ABCDEfghijKLMNOprstuVWXYz0123456789_______________----_________________________0");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_REF, "A1");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_REF, "A1, A2");
        verifySuccess(ObjectType.IRT, AttributeType.MNT_REF, "FOO-BAR-ZOT");
    }

    @Test
    public void mpDefault() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_DEFAULT, "afi ipv6.unicast to AS5541 networks AS31554");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_DEFAULT, "afi ipv6.unicast to AS12502 action pref=100; networks ANY");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.MP_DEFAULT, "to AS6939 action pref=1000000;");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.MP_DEFAULT, "INVALID");
    }

    @Test
    public void mpExport() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_EXPORT, "        afi ipv4.unicast to AS9070 62.44.108.66 at 62.44.108.65 announce AS-SU-LOCAL\n");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_EXPORT, "afi ipv6.unicast to AS8262 2001:67c:20d0:fffe:ffff:ffff:ffff:fffa at 2001:67c:20d0:fffe:ffff:ffff:ffff:fff9 announce AS-SU-LOCAL");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_EXPORT, "afi ipv6.unicast to   AS12956  \t84.16.8.225 at 84.16.8.226\tannounce AS-TEST AND NOT {0.0.0.0/0}");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_EXPORT, "afi ipv4.unicast to AS9070 62.44.108.66 at 62.44.108.65 announce AS-SU-LOCAL");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.MP_EXPORT, "invalid");
    }

    @Test
    public void mpFilter() throws Exception {
        verifySuccess(ObjectType.FILTER_SET, AttributeType.MP_FILTER, "{ 0.0.0.0/0 }");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.MP_FILTER, "{ 2001:1234::/64^+ }");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.MP_FILTER, "{ 2a00:10C0::/32^+ }");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.MP_FILTER, "{ 0.0.0.0/0, 192.168.0.0/16^+ }");
        verifySuccess(ObjectType.FILTER_SET, AttributeType.MP_FILTER, "{2001::/20^20-32} AND NOT {2001:DB8::/32^+}");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.MP_FILTER, "<[AS64512-AS65534]>");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.MP_FILTER, "<^AS7775535> OR <^AS8501 AS20965>");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.MP_FILTER, "<^AS7775535> OR <^AS8501 AS20965> AND community.contains(65533:7295)");

        verifyFailure(ObjectType.FILTER_SET, AttributeType.MP_FILTER, "INVALID");
    }

    @Test
    public void mpImport() throws Exception {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_IMPORT, "afi ipv6.unicast from AS50607 2001:7f8:5b:3::1 at 2001:7f8:5b:3::2 action pref=100; accept AS-EPIX");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_IMPORT, "  afi ipv6.unicast from AS15685 2001:7f8:14::6:1 at 2001:7f8:14::31:1 accept ANY");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_IMPORT, "afi ipv6.unicast from AS6777 accept <^[AS9002 AS31133 AS24940]>");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.MP_IMPORT, "afi ipv6.unicast from AS6777 accept <^[AS1002-AS1005]>");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.MP_IMPORT, " afi ipv6.unicast  AS3248  AS39560");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.MP_IMPORT, "INVALID");
    }

    @Test
    public void mpPeer() throws Exception {
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 2001::1A asno(AS2334)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 2001::1A asno(AS0)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 2001::1A asno(AS1)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "MPBGP 2001:1234::1 asno(AS12345)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "MPBGP 2001:: asno(AS12345)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "MPBGP 2001:: asno(AS1)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "MPBGP 2001:67c:2e8:13:a8a9:745d:3ed8:94b0 asno(AS12345)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 192.168.1.2 asno(AS2345)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 192.168.1.2 asno(AS2345)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 0.0.0.0 asno(PeERaS)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 192.168.1.2       asno(    PeERaS)");

        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "MPBGP 2001:: asno(AS01)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 2001::1A asno()");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "MPBGP 2001:: asno(AS)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "MPBGP 2001::/1 asno(AS12345)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "0.0.0.0 asno(PeERaS)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 192.168.1. asno(PeERaS)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "BGP4 255.255.255.256 asno(PeERaS)");
        verifyFailure(ObjectType.INET_RTR, AttributeType.MP_PEER, "INVALID");
    }

    @Test
    public void mpPeering() throws Exception {
        verifySuccess(ObjectType.PEERING_SET, AttributeType.MP_PEERING, "AS3043 2001:504:17:115::169 at 2001:504:17:115::227");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.MP_PEERING, "AS702:PRNG-AT-CONTINENTAL");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.MP_PEERING, "AS8262 146.188.51.222 At 146.188.51.221");

        verifyFailure(ObjectType.PEERING_SET, AttributeType.MP_PEERING, "AS8262 At 199.999");
        verifyFailure(ObjectType.PEERING_SET, AttributeType.MP_PEERING, "AS9998555555 146.188.67.26 at 146.188.0.13");
    }

    @Test
    public void mntRoutes() throws Exception {
        verifyFailure(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "");
        verifyFailure(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "A");
        verifyFailure(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "AS13213-MNT {193.150.8.0-193.150.8.255}");

        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "RP");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "ANY");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "RIPE-NCC-RPSL-MNT");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "AS8867-MNT  ANY");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "AS8867-MNT  any  ");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "MNTNER_NAME1");
        verifyFailure(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "SOME-MNT {0/0}");
        verifyFailure(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "SOME-MNT {128.9/16}");
        verifyFailure(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "EQIP-MNT {77.74.152.0/23*23}");
        verifyFailure(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "CB-EUROPE-FLOSK {194.9.240.0/33}");

        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "AS286-MNT {194.104.182.0/24^+}");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "AS286-MNT {194.104.182.0/24^-}");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "TEST-MNT { 193.151.47.0/24^24-24 }");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "TEST-MNT {77.74.152.0/23^23}");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "CB-EUROPE-FLOSK {194.9.240.0/24,194.9.241.0/24}");
        verifySuccess(ObjectType.ROUTE, AttributeType.MNT_ROUTES, "TEST-MNT {91.233.206.0/23, 91.233.206.0/24 , 91.233.207.0/24}");
        verifySuccess(ObjectType.ROUTE6, AttributeType.MNT_ROUTES, "QSC-NOC {2a00:c00::/48}");
    }

    @Test
    public void netname() throws Exception {
        verifyFailure(ObjectType.INETNUM, AttributeType.NETNAME, "");
        verifyFailure(ObjectType.INETNUM, AttributeType.NETNAME, "1NETNAME");
        verifyFailure(ObjectType.INETNUM, AttributeType.NETNAME, "NET.NAME");
        verifyFailure(ObjectType.INETNUM, AttributeType.NETNAME, "REALLY_REALLY_REALLY_REALLY_REALLY_REALLY_REALLY_REALLY_REALLY_REALLY_LONG_NETNAME");

        verifySuccess(ObjectType.INETNUM, AttributeType.NETNAME, "RIPE-NCC");
        verifySuccess(ObjectType.INETNUM, AttributeType.NETNAME, "ripe-ncc");
        verifySuccess(ObjectType.INETNUM, AttributeType.NETNAME, "FOO-DIALDOWN-01");
    }

    @Test
    public void nichandle() throws Exception {
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "aa-");
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "a-");
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "aaaaa-");
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "aaa-a");
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "aa100000-a12345678901234567890a");
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "aa100000-a1234567890123456789a");

        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "aa-aa");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "aa1-aa");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "Aa1-aA");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "DH3037-RIPE");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "ASAK");

        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "aa1-aa");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "aa999999-aa");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "TSFP");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "TsFP");

        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "aa01-aa");

        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "auto-1");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "auto-1aa");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "AuTo-1");
        verifySuccess(ObjectType.ROLE, AttributeType.NIC_HDL, "auto-12345678901234567890aaaa");
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "auto-01");
        verifyFailure(ObjectType.ROLE, AttributeType.NIC_HDL, "auto-1234567890123456789012345aa");
    }

    @Test
    public void nserver() {
        verifySuccess(ObjectType.DOMAIN, AttributeType.NSERVER, "RIPE.NL");
        verifySuccess(ObjectType.DOMAIN, AttributeType.NSERVER, "ns5.bogus.com");
        verifySuccess(ObjectType.DOMAIN, AttributeType.NSERVER, "144.102.5.in-addr.arpa 81.20.133.177");
        verifySuccess(ObjectType.DOMAIN, AttributeType.NSERVER, "144.102.5.in-addr.arpa 81.20.133.177");

        verifyFailure(ObjectType.DOMAIN, AttributeType.NSERVER, "ns1.64.67.217.in-addr.arpa 2001:db8::1 2001:db8::2");
        verifyFailure(ObjectType.DOMAIN, AttributeType.NSERVER, "144.102.5.in-addr.arpa 144.102.5.in-addr.arpa");
        verifyFailure(ObjectType.DOMAIN, AttributeType.NSERVER, "RIPE.NL.ip6.arpa 144.102.5.e164.arpa");
        verifyFailure(ObjectType.DOMAIN, AttributeType.NSERVER, "RIPE.NL.ip6.arpa, ns6.bogus.com");
    }

    @Test
    public void object_name() throws Exception {
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "A");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "A12345678901234567890123456789012345678901234567890123456789012345678901234567890");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "ANY");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "any");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "as-any");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "rs-any");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "peeras");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "and");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "or");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "not");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "atomic");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "from");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "to");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "at");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "action");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "accept");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "announce");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "except");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "refine");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "networks");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "into");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "inbound");
        verifyFailure(ObjectType.MNTNER, AttributeType.MNTNER, "outbound");
        verifySuccess(ObjectType.MNTNER, AttributeType.MNTNER, "ABCDEfghijKLMNOprstuVWXYz0123456789_______________----_________________________0");
        verifySuccess(ObjectType.MNTNER, AttributeType.MNTNER, "A1");
        verifySuccess(ObjectType.MNTNER, AttributeType.MNTNER, "FOO-BAR-ZOT");
    }

    @Test
    public void organisation() {
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "aaa-aa-aa");
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "aaa-");
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "aaa-aa");
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "aaa-aa-a");
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "org-aa100000-a1234567890123456a");

        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "org-aa-aa");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "org-aa1-aa");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "oRg-Aa1-aA");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "org-aa100000-a123456789012345a");

        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "org-aa1-aa");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "org-aa999999-aa");

        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "org-aa01-aa");

        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "auto-1");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "auto-1aa");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "AuTo-1");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG, "auto-12345678901234567890aaaa");
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "auto-01");
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG, "auto-1234567890123456789012345aa");
    }

    @Test
    public void origin() {
        verifyFailure(ObjectType.ROUTE, AttributeType.ORIGIN, "AS");
        verifyFailure(ObjectType.ROUTE, AttributeType.ORIGIN, "AAS3255");
        verifyFailure(ObjectType.ROUTE, AttributeType.ORIGIN, "AS-3255");
        verifyFailure(ObjectType.ROUTE, AttributeType.ORIGIN, "AS 3255");
        verifyFailure(ObjectType.ROUTE, AttributeType.ORIGIN, "3255");
        verifyFailure(ObjectType.ROUTE, AttributeType.ORIGIN, "AS4294967296");

        verifySuccess(ObjectType.ROUTE, AttributeType.ORIGIN, "AS0");
        verifySuccess(ObjectType.ROUTE, AttributeType.ORIGIN, "AS3255");
        verifySuccess(ObjectType.ROUTE, AttributeType.ORIGIN, "AS4294967295");
    }

    @Test
    public void orgName() {
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG_NAME, "\"MAD\" $NAME");

        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_NAME, "Internet Assigned Numbers Authority");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_NAME, "ABD [DEF]");
    }

    @Test
    public void orgType() {
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "");
        verifyFailure(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "RIRRIR");

        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "iana");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "RIR");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "NIR");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "LIR");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "WHITEPAGES");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "DIRECT_ASSIGNMENT");
        verifySuccess(ObjectType.ORGANISATION, AttributeType.ORG_TYPE, "OTHER");

        final AttributeSyntax.OrgTypeSyntax orgTypeSyntax = new AttributeSyntax.OrgTypeSyntax();
        final String description = orgTypeSyntax.getDescription(ObjectType.ORGANISATION);
        assertThat(description, is("" +
                "org-type can have one of these values:\n" +
                "\n" +
                "o 'IANA' for Internet Assigned Numbers Authority\n" +
                "o 'RIR' for Regional Internet Registries\n" +
                "o 'NIR' for National Internet Registries (there are no NIRs in the RIPE NCC service region)\n" +
                "o 'LIR' for Local Internet Registries\n" +
                "o 'WHITEPAGES' for special links to industry people\n" +
                "o 'DIRECT_ASSIGNMENT' for direct contract with RIPE NCC\n" +
                "o 'OTHER' for all other organisations.\n\n"));
    }

    @Test
    public void owner() {
        verifySuccess(ObjectType.KEY_CERT, AttributeType.OWNER, "");

        StringBuilder builder = new StringBuilder();
        for (char i = 0; i < Character.MAX_VALUE; i++) {
            builder.append(i);
        }

        verifySuccess(ObjectType.KEY_CERT, AttributeType.OWNER, builder.toString());
    }

    @Test
    public void peer() {
        verifySuccess(ObjectType.INET_RTR, AttributeType.PEER, "BGP4 192.168.1.2 asno(AS2345)");
        verifySuccess(ObjectType.INET_RTR, AttributeType.PEER, "BGP4 192.168.1.2 asno(PeerAS), flap_damp()");
        verifySuccess(ObjectType.INET_RTR, AttributeType.PEER, "BGP4 rtrs-ibgp-peers asno(AS3333), flap_damp()");
        verifySuccess(ObjectType.INET_RTR, AttributeType.PEER, "BGP4 prng-ebgp-peers asno(PeerAS)");

        verifyFailure(ObjectType.INET_RTR, AttributeType.PEER, "BGP4 192.168.1.2 asno(PeerAS_), flap_damp()");
        verifyFailure(ObjectType.INET_RTR, AttributeType.PEER, "invalid");
    }

    @Test
    public void peering() {
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING, "AS6939 206.126.115.17 at 206.126.115.227");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING, "AS1680");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING, "AS-AIX");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING, "AS29076 at foobar-nu.bogus.ru");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING, "AS1 at 9.9.9.1");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING, "AS123 except AS123");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING, "pRng-bar");

        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING, "except AS123");
        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING, "at except AS123");
        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING, "invalid");
    }

    @Test
    public void peeringSet() {
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "PRNG-AS15469");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "prng-node-bogus");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "AS8627:prng-TRANSIT-OUT");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "PRNG-RIPE:PRNG-ALLOCBNDR:PRNG-IPV6");
        verifySuccess(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "AS8627:prng-TRANSIT-OUT");

        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "prng");
        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "Prng1");
        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "prng TESTNET");
        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING_SET, "AS20773:");
        verifyFailure(ObjectType.PEERING_SET, AttributeType.PEERING_SET, ":PRNG-AUTH-PLOT-BOGUS-FROM-RS3724");
    }

    @Test
    public void person() {
        verifyFailure(ObjectType.PERSON, AttributeType.PERSON, "some [name]");
        verifyFailure(ObjectType.PERSON, AttributeType.PERSON, "Mad 'Dog'");
        verifyFailure(ObjectType.PERSON, AttributeType.PERSON, "1Big guy");

        verifySuccess(ObjectType.PERSON, AttributeType.PERSON, "Agoston Horvath");
        verifySuccess(ObjectType.PERSON, AttributeType.PERSON, "Andre Kampert");
        verifySuccess(ObjectType.PERSON, AttributeType.PERSON, "Angela Sjoholm");
        verifySuccess(ObjectType.PERSON, AttributeType.PERSON, "Denis Walker");
        verifySuccess(ObjectType.PERSON, AttributeType.PERSON, "Ed Shryane");
        verifySuccess(ObjectType.PERSON, AttributeType.PERSON, "Kaveh Ranjbar");

        verifySuccess(ObjectType.PERSON, AttributeType.PERSON, "Martin . Fowler");
    }

    @Test
    public void poem() {
        verifyFailure(ObjectType.POEM, AttributeType.POEM, "poem");
        verifyFailure(ObjectType.POEM, AttributeType.POEM, "poem-");
        verifyFailure(ObjectType.POEM, AttributeType.POEM, "poem poem");
        verifyFailure(ObjectType.POEM, AttributeType.POEM, "POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM-POEM");

        verifySuccess(ObjectType.POEM, AttributeType.POEM, "poem-poem");
        verifySuccess(ObjectType.POEM, AttributeType.POEM, "POEM-POEM");
        verifySuccess(ObjectType.POEM, AttributeType.POEM, "POEM-POEM-POEM");
        verifySuccess(ObjectType.POEM, AttributeType.POEM, "POEM-POEM-A1-B2-C3");
    }

    @Test
    public void poeticForm() {
        verifyFailure(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "form");
        verifyFailure(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "form-");
        verifyFailure(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "form form");
        verifyFailure(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM-FORM");

        verifySuccess(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "form-form");
        verifySuccess(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "FORM-FORM");
        verifySuccess(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "FORM-FORM-FORM");
        verifySuccess(ObjectType.POETIC_FORM, AttributeType.POETIC_FORM, "FORM-FORM-A1-B2-C3");
    }

    @Test
    public void remarks() {
        verifySuccess(ObjectType.PERSON, AttributeType.REMARKS, "");

        StringBuilder builder = new StringBuilder();
        for (char i = 0; i < Character.MAX_VALUE; i++) {
            builder.append(i);
        }

        verifySuccess(ObjectType.PERSON, AttributeType.REMARKS, builder.toString());
    }

    @Test
    public void role() {
        verifyFailure(ObjectType.ROLE, AttributeType.ROLE, "some [name]");
        verifyFailure(ObjectType.ROLE, AttributeType.ROLE, "Mad 'Dog'");
        verifyFailure(ObjectType.ROLE, AttributeType.ROLE, "1Big guy");

        verifySuccess(ObjectType.ROLE, AttributeType.ROLE, "Agoston Horvath");
        verifySuccess(ObjectType.ROLE, AttributeType.ROLE, "Andre Kampert");
        verifySuccess(ObjectType.ROLE, AttributeType.ROLE, "Angela Sjoholm");
        verifySuccess(ObjectType.ROLE, AttributeType.ROLE, "Denis Walker");
        verifySuccess(ObjectType.ROLE, AttributeType.ROLE, "Ed Shryane");
        verifySuccess(ObjectType.ROLE, AttributeType.ROLE, "Kaveh Ranjbar");

        verifySuccess(ObjectType.ROLE, AttributeType.ROLE, "Martin . Fowler");
    }

    @Test
    public void route() {
        verifyFailure(ObjectType.ROUTE, AttributeType.ROUTE, "");
        verifyFailure(ObjectType.ROUTE, AttributeType.ROUTE, "100.100.100");
        verifyFailure(ObjectType.ROUTE, AttributeType.ROUTE, "300.300.300.300");
        verifyFailure(ObjectType.ROUTE, AttributeType.ROUTE, "0/100");
        verifyFailure(ObjectType.ROUTE, AttributeType.ROUTE, "192.168.1.10");
        verifyFailure(ObjectType.ROUTE, AttributeType.ROUTE, "192.168.1.0-192.168.1.255");
        verifyFailure(ObjectType.ROUTE, AttributeType.ROUTE, "::0/0");

        verifySuccess(ObjectType.ROUTE, AttributeType.ROUTE, "0/0");
        verifySuccess(ObjectType.ROUTE, AttributeType.ROUTE, "195.10.40.0/29");
        verifySuccess(ObjectType.ROUTE, AttributeType.ROUTE, "195.10.42.46/32");
    }

    @Test
    public void route6() {
        verifyFailure(ObjectType.ROUTE6, AttributeType.ROUTE6, "::0");
        verifyFailure(ObjectType.ROUTE6, AttributeType.ROUTE6, "195.10.40.0/29");

        verifySuccess(ObjectType.ROUTE6, AttributeType.ROUTE6, "::0/0");
        verifySuccess(ObjectType.ROUTE6, AttributeType.ROUTE6, "2001:1578:0200::/40");
        verifySuccess(ObjectType.ROUTE6, AttributeType.ROUTE6, "2a01:e0::/32");
        verifySuccess(ObjectType.ROUTE6, AttributeType.ROUTE6, "2a01:00e0::/32");
        verifySuccess(ObjectType.ROUTE6, AttributeType.ROUTE6, "2A01:E0::/32");
    }

    @Test
    public void routeSet() {
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.ROUTE_SET, "rS-ROUTES-AS12731-AS50343");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.ROUTE_SET, "As58119:RS-CH");
        verifySuccess(ObjectType.ROUTE_SET, AttributeType.ROUTE_SET, "Rs-TEST:rs-PUG");

        verifyFailure(ObjectType.ROUTE_SET, AttributeType.ROUTE_SET, "RTRS-CH");
        verifyFailure(ObjectType.ROUTE_SET, AttributeType.ROUTE_SET, "RS1093");
        verifyFailure(ObjectType.ROUTE_SET, AttributeType.ROUTE_SET, "As58119");
    }

    @Test
    public void rtrSet() {
        verifyFailure(ObjectType.RTR_SET, AttributeType.RTR_SET, "rtrs");
        verifyFailure(ObjectType.RTR_SET, AttributeType.RTR_SET, "Rtrs1");
        verifyFailure(ObjectType.RTR_SET, AttributeType.RTR_SET, "rtrs TESTNET");
        verifyFailure(ObjectType.RTR_SET, AttributeType.RTR_SET, "AS20773:");
        verifyFailure(ObjectType.RTR_SET, AttributeType.RTR_SET, ":RTRS-AUTH-PLOT-BOGUS-FROM-RS3724");

        verifySuccess(ObjectType.RTR_SET, AttributeType.RTR_SET, "rtrs-TESTNET");
        verifySuccess(ObjectType.RTR_SET, AttributeType.RTR_SET, "RTRS-TESTNET");
        verifySuccess(ObjectType.RTR_SET, AttributeType.RTR_SET, "AS20773:rtrs-HOSTEUROPE");
        verifySuccess(ObjectType.RTR_SET, AttributeType.RTR_SET, "AS20773:RTRS-HOSTEUROPE");
        verifySuccess(ObjectType.RTR_SET, AttributeType.RTR_SET, "RTRS-RIPE:RTRS-ALLOCBNDR:RTRS-IPV6");
    }

    @Test
    public void phone() {
        verifySuccess(ObjectType.PERSON, AttributeType.PHONE, "+3161021077");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "06-4826083");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "020-720375");

        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "112");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "06-11");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "(541) 754-3010");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "(089) / 636-48018");
        verifySuccess(ObjectType.PERSON, AttributeType.PHONE, "+31610210776");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "06-48260830");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "020-6720375");
        verifyFailure(ObjectType.PERSON, AttributeType.PHONE, "1-855-698-3248");

        verifySuccess(ObjectType.PERSON, AttributeType.PHONE, "+31610210776 ext.123");
        verifySuccess(ObjectType.PERSON, AttributeType.PHONE, "+31610210776 EXT.123");
    }

    @Test
    public void pingable() {
        verifyFailure(ObjectType.ROUTE, AttributeType.PINGABLE, "");
        verifyFailure(ObjectType.ROUTE, AttributeType.PINGABLE, "100.100.100");
        verifyFailure(ObjectType.ROUTE, AttributeType.PINGABLE, "300.300.300.300");
        verifyFailure(ObjectType.ROUTE, AttributeType.PINGABLE, "0/100");
        verifyFailure(ObjectType.ROUTE, AttributeType.PINGABLE, "::0/0");

        verifyFailure(ObjectType.ROUTE, AttributeType.PINGABLE, "192.168.1.10,192.168.1.11");
        verifyFailure(ObjectType.ROUTE, AttributeType.PINGABLE, "0/0");

        verifyFailure(ObjectType.ROUTE6, AttributeType.PINGABLE, "");
        verifyFailure(ObjectType.ROUTE6, AttributeType.PINGABLE, "100.100.100");
        verifyFailure(ObjectType.ROUTE6, AttributeType.PINGABLE, "0/0");
        verifyFailure(ObjectType.ROUTE6, AttributeType.PINGABLE, "2a00:c00::/48,2a00:c01::/48");
        verifyFailure(ObjectType.ROUTE6, AttributeType.PINGABLE, "::0/0");
        
        verifySuccess(ObjectType.ROUTE, AttributeType.PINGABLE, "192.168.1.10");
        verifySuccess(ObjectType.ROUTE6, AttributeType.PINGABLE, "2a00:c00::");
    }

    @Test
    public void statusIp4Ip6() {
        verifyFailure(ObjectType.INETNUM, AttributeType.STATUS, "SOME_INVALID_STATUS");

        verifySuccess(ObjectType.INETNUM, AttributeType.STATUS, "SUB-ALLOCATED PA");
        verifyFailure(ObjectType.INETNUM, AttributeType.STATUS, "ALLOCATED-BY-LIR");
        verifyFailure(ObjectType.INETNUM, AttributeType.STATUS, "ASSIGNED");
        verifySuccess(ObjectType.INETNUM, AttributeType.STATUS, "ALLOCATED PI");
        verifySuccess(ObjectType.INETNUM, AttributeType.STATUS, "LIR-PARTITIONED PI");
        verifySuccess(ObjectType.INETNUM, AttributeType.STATUS, "ASSIGNED ANYCAST");

        verifyFailure(ObjectType.INETNUM, AttributeType.STATUS, "AGGREGATED-BY-LIR");
        verifySuccess(ObjectType.INET6NUM, AttributeType.STATUS, "AGGREGATED-BY-LIR");

        verifySuccess(ObjectType.INETNUM, AttributeType.STATUS, "ALLOCATED PI");
        verifyFailure(ObjectType.INET6NUM, AttributeType.STATUS, "ALLOCATED PI");
    }

    @Test
    public void statusAutnum() {
        verifySuccess(ObjectType.AUT_NUM, AttributeType.AUTNUM_STATUS, "OTHER");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.AUTNUM_STATUS, "AssIgNed");
        verifySuccess(ObjectType.AUT_NUM, AttributeType.AUTNUM_STATUS, "legacy");

        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUTNUM_STATUS, "ALLOCATED PI");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUTNUM_STATUS, "33546565465");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUTNUM_STATUS, "PGPKEY-");
        verifyFailure(ObjectType.AUT_NUM, AttributeType.AUTNUM_STATUS, "whatever");
    }

    @Test
    public void signature() throws Exception {
        verifyFailure(ObjectType.IRT, AttributeType.SIGNATURE, "PGPKEY-");
        verifyFailure(ObjectType.IRT, AttributeType.SIGNATURE, "A6D57ECE");
        verifyFailure(ObjectType.IRT, AttributeType.SIGNATURE, "X509-");
        verifyFailure(ObjectType.IRT, AttributeType.SIGNATURE, "2606");
        verifyFailure(ObjectType.IRT, AttributeType.SIGNATURE, "AUTO-");
        verifyFailure(ObjectType.IRT, AttributeType.SIGNATURE, "123");

        verifySuccess(ObjectType.IRT, AttributeType.SIGNATURE, "PGPKEY-A6D57ECE");
        verifySuccess(ObjectType.IRT, AttributeType.SIGNATURE, "X509-2606");
        verifySuccess(ObjectType.IRT, AttributeType.SIGNATURE, "AUTO-123");
    }

    @Test
    public void text() {
        verifySuccess(ObjectType.POEM, AttributeType.TEXT, "");

        StringBuilder builder = new StringBuilder();
        for (char i = 0; i < Character.MAX_VALUE; i++) {
            builder.append(i);
        }

        verifySuccess(ObjectType.POEM, AttributeType.TEXT, builder.toString());
    }

    @Test
    public void techc() {
        verifySuccess(ObjectType.AS_SET, AttributeType.TECH_C, "HIA1-AFRINIC");
        verifySuccess(ObjectType.AS_SET, AttributeType.TECH_C, "HIA1-ARIN");
    }

    private void verifySuccess(final ObjectType objectType, final AttributeType attributeType, final String value) {
        verify(objectType, attributeType, value, false);
    }

    private void verifyFailure(final ObjectType objectType, final AttributeType attributeType, final String value) {
        verify(objectType, attributeType, value, true);
    }

    private void verify(final ObjectType objectType, final AttributeType attributeType, final String value, final boolean errors) {
        final ObjectMessages objectMessages = new ObjectMessages();
        new RpslAttribute(attributeType, value).validateSyntax(objectType, objectMessages);
        assertThat("Errors in " + attributeType.getName() + ": " + value + ": " + objectMessages, objectMessages.hasErrors(), is(errors));
    }
}
