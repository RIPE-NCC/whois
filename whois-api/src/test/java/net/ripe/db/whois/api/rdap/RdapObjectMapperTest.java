package net.ripe.db.whois.api.rdap;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.Action;
import net.ripe.db.whois.api.rdap.domain.Autnum;
import net.ripe.db.whois.api.rdap.domain.Domain;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.api.rdap.domain.Nameserver;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.api.rdap.domain.SearchResult;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.iptree.Ipv4Entry;
import net.ripe.db.whois.common.iptree.Ipv4Tree;
import net.ripe.db.whois.common.iptree.Ipv6Tree;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RdapObjectMapperTest {

    private static final LocalDateTime VERSION_TIMESTAMP = LocalDateTime.parse("2044-04-26T00:02:03.000");

    @Mock
    private NoticeFactory noticeFactory;
    @Mock
    private RpslObjectDao rpslObjectDao;
    @Mock
    private Ipv4Tree ipv4Tree;
    @Mock
    private Ipv6Tree ipv6Tree;

    private RdapObjectMapper mapper;

    @Before
    public void setup() {
        when(ipv4Tree.findFirstLessSpecific(any(Ipv4Resource.class))).thenReturn(Collections.singletonList(new Ipv4Entry(Ipv4Resource.parse("0/0"), 1)));
        when(rpslObjectDao.getById(1)).thenReturn(RpslObject.parse("inetnum: 0.0.0.0 - 255.255.255.255\nnetname: ROOT-NET\nsource: TEST"));

        this.mapper = new RdapObjectMapper(noticeFactory, rpslObjectDao, ipv4Tree, ipv6Tree, "whois.ripe.net");
    }

    @Test
    public void ip() {
        final Ip result = (Ip) map(
                RpslObject.parse(
                        "inetnum:        10.0.0.0 - 10.255.255.255\n" +
                        "netname:        RIPE-NCC\n" +
                        "descr:          some descr\n" +
                        "country:        NL\n" +
                        "admin-c:        TP1-TEST\n" +
                        "tech-c:         TP1-TEST\n" +
                        "status:         OTHER\n" +
                        "language:       EN\n" +
                        "mnt-by:         TST-MNT\n" +
                        "mnt-lower:      TST-MNT\n" +
                        "mnt-domains:    TST-MNT\n" +
                        "mnt-routes:     TST-MNT\n" +
                        "mnt-irt:        irt-IRT1\n" +
                        "notify:         notify@test.net\n" +
                        "org:            ORG-TOL1-TEST\n" +
                        "changed:        ripe@test.net 20120101\n" +
                        "source:         TEST"),
                RpslObject.parse(
                        "role:           Abuse Contact\n" +
                        "nic-hdl:        AB-TEST\n" +
                        "mnt-by:         TEST-MNT\n" +
                        "admin-c:        TP1-TEST\n" +
                        "tech-c:         TP2-TEST\n" +
                        "phone:          +31 12345678\n" +
                        "source:         TEST"
                ));

        assertThat(result.getHandle(), is("10.0.0.0 - 10.255.255.255"));
        assertThat(result.getStartAddress(), is("10.0.0.0/32"));
        assertThat(result.getEndAddress(), is("10.255.255.255/32"));
        assertThat(result.getIpVersion(), is("v4"));
        assertThat(result.getName(), is("RIPE-NCC"));
        assertThat(result.getType(), is("OTHER"));
        assertThat(result.getCountry(), is("NL"));
        assertThat(result.getLang(), is("EN"));
        assertThat(result.getParentHandle(), is("ROOT-NET"));
        assertThat(result.getPort43(), is("whois.ripe.net"));

        final List<Entity> entities = result.getEntitySearchResults();
        assertThat(entities, hasSize(1));
        assertThat(entities.get(0).getHandle(), is("AB-TEST"));
        assertThat(entities.get(0).getRoles(), hasSize(1));
        assertThat(entities.get(0).getRoles().get(0), is(Role.ABUSE));

        final List<Object> vCardArray = entities.get(0).getVCardArray();
        assertThat(vCardArray, hasSize(2));
        assertThat(vCardArray.get(0).toString(), is("vcard"));
        assertThat(Joiner.on("\n").join((List)vCardArray.get(1)), is("" +
                "[version, {}, text, 4.0]\n" +
                "[fn, {}, text, Abuse Contact]\n" +
                "[kind, {}, text, group]\n" +
                "[tel, {type=voice}, text, +31 12345678]"));

        final List<Entity> abuseEntities = entities.get(0).getEntitySearchResults();
        assertThat(abuseEntities, hasSize(3));
        assertThat(abuseEntities.get(0).getHandle(), is("TEST-MNT"));
        assertThat(abuseEntities.get(0).getRoles().get(0), is(Role.REGISTRANT));
        assertThat(abuseEntities.get(1).getHandle(), is("TP1-TEST"));
        assertThat(abuseEntities.get(1).getRoles().get(0), is(Role.ADMINISTRATIVE));
        assertThat(abuseEntities.get(2).getHandle(), is("TP2-TEST"));
        assertThat(abuseEntities.get(2).getRoles().get(0), is(Role.TECHNICAL));

        assertThat(result.getRemarks().get(0).getDescription().get(0), is("some descr"));
        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));

        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventAction(), is(Action.LAST_CHANGED));
        assertThat(result.getEvents().get(0).getEventDate(), is(VERSION_TIMESTAMP));
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
                "source:         TEST\n")));

        assertThat(result.getHandle(), is("AS102"));
        assertThat(result.getStartAutnum(), is(nullValue()));
        assertThat(result.getEndAutnum(), is(nullValue()));
        assertThat(result.getName(), is("End-User-2"));
        assertThat(result.getType(), is("DIRECT ALLOCATION"));
        assertThat(result.getStatus(), is(emptyIterable()));
        assertThat(result.getCountry(), is(nullValue()));

        final List<Entity> entities = result.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getHandle(), is("AP1-TEST"));
        assertThat(entities.get(0).getRoles(), containsInAnyOrder(Role.TECHNICAL, Role.ADMINISTRATIVE));
        assertThat(entities.get(0).getVCardArray(), is(nullValue()));
        assertThat(entities.get(1).getHandle(), is("UPD-MNT"));
        assertThat(entities.get(1).getRoles().get(0), is(Role.REGISTRANT));
        assertThat(entities.get(1).getVCardArray(), is(nullValue()));

        assertThat(result.getRemarks().get(0).getDescription().get(0), is("description"));

        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));
        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventAction(), is(Action.LAST_CHANGED));
        assertThat(result.getEvents().get(0).getEventDate(), is(VERSION_TIMESTAMP));

        assertThat(result.getPort43(), is("whois.ripe.net"));
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
                "nserver:         ns.foo.net.0.0.193.in-addr.arpa. 10.0.0.0/32\n" +
                "mnt-by:          RIPE-NCC-MNT\n" +
                "ds-rdata:        52314 5 1 93B5837D4E5C063A3728FAA72BA64068F89B39DF\n" +
                "changed:         test@ripe.net 20120505\n" +
                "source:          TEST")));

        assertThat(result.getHandle(), is("2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));
        assertThat(result.getLdhName(), is("2.1.2.1.5.5.5.2.0.2.1.e164.arpa"));
        assertThat(result.getUnicodeName(), is(nullValue()));

        assertThat(result.getNameservers(), hasSize(2));
        assertThat(result.getNameservers().get(0).getLdhName(), is("ns.1.net"));
        assertThat(result.getNameservers().get(1).getLdhName(), is("ns.foo.net.0.0.193.in-addr.arpa"));
        assertThat(result.getNameservers().get(1).getIpAddresses().getIpv4().get(0), is("10.0.0.0/32"));

        final Domain.SecureDNS secureDNS = result.getSecureDNS();
        assertThat(secureDNS.isDelegationSigned(), is(true));
        assertThat(secureDNS.getDsData().get(0).getAlgorithm(), is(5));
        assertThat(secureDNS.getDsData().get(0).getKeyTag(), is(52314L));
        assertThat(secureDNS.getDsData().get(0).getDigest(), is("93B5837D4E5C063A3728FAA72BA64068F89B39DF"));
        assertThat(secureDNS.getDsData().get(0).getDigestType(), is(1));

        final List<Entity> entities = result.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities.get(0).getHandle(), is("RIPE-NCC-MNT"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(0).getVCardArray(), is(nullValue()));
        assertThat(entities.get(1).getHandle(), is("TEST-PN"));
        assertThat(entities.get(1).getRoles(), containsInAnyOrder(Role.ADMINISTRATIVE, Role.TECHNICAL, Role.ZONE));
        assertThat(entities.get(1).getVCardArray(), is(nullValue()));

        assertThat(result.getStatus(), is(emptyIterable()));
        assertThat(result.getPublicIds(), is(nullValue()));

        assertThat(result.getRemarks().get(0).getDescription().get(0), is("enum domain"));
        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));
        assertThat(result.getPort43(), is("whois.ripe.net"));

        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventActor(), is(nullValue()));
        assertThat(result.getEvents().get(0).getEventAction(), is(Action.LAST_CHANGED));
        assertThat(result.getEvents().get(0).getEventDate(), is(VERSION_TIMESTAMP));
        assertThat(result.getEvents().get(0).getEventActor(), is(nullValue()));

        assertThat(result.getPort43(), is("whois.ripe.net"));
    }

    @Test
    public void domain_31_12_202_in_addr_arpa() {
        final Domain result = (Domain) map((RpslObject.parse("" +
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
                "source:   TEST\n")));

        assertThat(result.getHandle(), is("31.12.202.in-addr.arpa"));
        assertThat(result.getLdhName(), is("31.12.202.in-addr.arpa"));
        assertThat(result.getUnicodeName(), is(nullValue()));

        assertThat(result.getNameservers(), hasSize(3));
        assertThat(result.getNameservers(), containsInAnyOrder(
            new Nameserver(null, "ns1.test.com.au", null, new Nameserver.IpAddresses(Lists.newArrayList("10.0.0.1/32"), Lists.newArrayList("2001:10::1/128"))),
            new Nameserver(null, "ns2.test.com.au", null, new Nameserver.IpAddresses(Lists.newArrayList("10.0.0.2/32"), Lists.newArrayList("2001:10::2/128"))),
            new Nameserver(null, "ns3.test.com.au", null, null)
        ));

        final Domain.SecureDNS secureDNS = result.getSecureDNS();
        assertThat(secureDNS.isDelegationSigned(), is(true));
        assertThat(secureDNS.getDsData(), hasSize(3));
        assertThat(secureDNS.getDsData(), containsInAnyOrder(
            new Domain.SecureDNS.DsData(52151L, 1, "13ee60f7499a70e5aadaf05828e7fc59e8e70bc1", 1, null),
            new Domain.SecureDNS.DsData(17881L, 5, "2e58131e5fe28ec965a7b8e4efb52d0a028d7a78", 1, null),
            new Domain.SecureDNS.DsData(17881L, 5, "8c6265733a73e5588bfac516a4fcfbe1103a544b95f254cb67a21e474079547e", 2, null)
        ));

        final List<Entity> entities = result.getEntitySearchResults();
        assertThat(entities, hasSize(2));
        assertThat(entities, containsInAnyOrder(
            new Entity("OWNER-MNT", null, Lists.newArrayList(Role.REGISTRANT), null),
            new Entity("TP1-TEST", null, Lists.newArrayList(Role.TECHNICAL, Role.ADMINISTRATIVE, Role.ZONE), null)
        ));

        assertThat(result.getPort43(), is("whois.ripe.net"));
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
                "source:        TEST"));

        assertThat(result.getHandle(), is("FL1-TEST"));
        final List<Object> vCardArray = result.getVCardArray();
        assertThat(vCardArray, hasSize(2));
        assertThat(vCardArray.get(0).toString(), is("vcard"));
        assertThat(Joiner.on("\n").join((List) vCardArray.get(1)), is("" +
                "[version, {}, text, 4.0]\n" +
                "[fn, {}, text, First Last]\n" +
                "[kind, {}, text, individual]\n" +
                "[adr, {label=Singel 258}, text, null]\n" +
                "[tel, {type=voice}, text, +31 20 123456]\n" +
                "[tel, {type=fax}, text, +31 20 123457]\n" +
                "[email, {}, text, first@last.org]\n" +
                "[org, {}, text, ORG-TOL1-TEST]"));

        assertThat(result.getRoles(), is(emptyIterable()));
        assertThat(result.getPublicIds(), is(nullValue()));

        final List<Entity> entities = result.getEntitySearchResults();
        assertThat(entities, hasSize(1));
        assertThat(entities.get(0).getHandle(), is("TST-MNT"));
        assertThat(entities.get(0).getRoles(), contains(Role.REGISTRANT));
        assertThat(entities.get(0).getVCardArray(), is(nullValue()));

        assertThat(result.getRemarks(), is(emptyIterable()));
        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));

        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventAction(), is(Action.LAST_CHANGED));

        assertThat(result.getStatus(), is(emptyIterable()));
        assertThat(result.getPort43(), is("whois.ripe.net"));
    }

    @Test
    public void organisation() {
        final Entity result = (Entity) map(RpslObject.parse("" +
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
                "abuse-c:        ABU-TEST\n" +
                "mnt-by:         FRED-MNT\n" +
                "changed:        change@test.ripe\n" +
                "source:         TEST"));

        assertThat(result.getHandle(), is("ORG-AC1-TEST"));
        final List<Object> vCardArray = result.getVCardArray();
        assertThat(vCardArray, hasSize(2));
        assertThat(vCardArray.get(0).toString(), is("vcard"));
        assertThat(Joiner.on("\n").join((List) vCardArray.get(1)), is("" +
                "[version, {}, text, 4.0]\n" +
                "[fn, {}, text, Acme Carpets]\n" +
                "[kind, {}, text, org]\n" +
                "[adr, {label=Singel 258}, text, null]\n" +
                "[tel, {type=voice}, text, +31 1234567]\n" +
                "[tel, {type=fax}, text, +31 98765432]\n" +
                "[email, {}, text, bitbucket@ripe.net]\n" +
                "[geo, {}, uri, 52.375599 4.899902]"));

        assertThat(result.getRoles(), is(emptyIterable()));

        assertThat(result.getPublicIds(), is(nullValue()));
        assertThat(result.getEntitySearchResults(), hasSize(2));

        assertThat(result.getRemarks(), hasSize(1));
        assertThat(result.getRemarks().get(0).getDescription().get(0), is("Acme Carpet Organisation"));

        assertThat(result.getLinks(), hasSize(2));
        assertThat(result.getLinks().get(0).getRel(), is("self"));
        assertThat(result.getLinks().get(1).getRel(), is("copyright"));

        assertThat(result.getEvents(), hasSize(1));
        assertThat(result.getEvents().get(0).getEventAction(), is(Action.LAST_CHANGED));

        assertThat(result.getStatus(), is(emptyIterable()));
        assertThat(result.getPort43(), is("whois.ripe.net"));

        assertThat(result.getLang(), is("DK"));
    }

    @Test
    public void mapSearch_twoObjects() {
        final List<RpslObject> objects = Lists.newArrayList(
                RpslObject.parse("organisation: ORG-TOL-TEST\norg-name: Test Organisation\nstatus: OTHER\ndescr: comment 1\nsource: TEST"),
                RpslObject.parse("organisation: ORG-TST-TEST\norg-name: Test Company\nstatus: OTHER\ndescr: comment 2\nsource: TEST")
        );

        final SearchResult response = (SearchResult)mapSearch(objects, Lists.newArrayList(LocalDateTime.parse("1970-04-14T09:22:14.857"), LocalDateTime.parse("1996-02-05T03:52:05.938")));

        assertThat(response.getEntitySearchResults(), hasSize(2));

        final Entity first = response.getEntitySearchResults().get(0);
        assertThat(first.getHandle(), is("ORG-TOL-TEST"));
        assertThat(first.getEvents(), hasSize(1));
        assertThat(first.getEvents().get(0).getEventAction(), is(Action.LAST_CHANGED));
        assertThat(first.getRemarks(), hasSize(1));
        assertThat(first.getRemarks().get(0).getDescription().get(0), is("comment 1"));

        final Entity last = response.getEntitySearchResults().get(1);
        assertThat(last.getHandle(), is("ORG-TST-TEST"));
        assertThat(last.getEvents(), hasSize(1));
        assertThat(last.getEvents().get(0).getEventAction(), is(Action.LAST_CHANGED));
        assertThat(last.getRemarks(), hasSize(1));
        assertThat(last.getRemarks().get(0).getDescription().get(0), is("comment 2"));
    }

    // helper methods

    private Object map(final RpslObject rpslObject) {
        return map(rpslObject, null);
    }

    private Object map(final RpslObject rpslObject, final RpslObject abuseContact) {
        return mapper.map("http://localhost/", rpslObject, VERSION_TIMESTAMP, abuseContact);
    }

    private Object mapSearch(final List<RpslObject> objects, final Iterable<LocalDateTime> lastUpdateds) {
        return mapper.mapSearch("http://localhost", objects, lastUpdateds);
    }
}
