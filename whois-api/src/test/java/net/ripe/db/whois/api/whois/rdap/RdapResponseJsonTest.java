package net.ripe.db.whois.api.whois.rdap;

import com.Ostermiller.util.LineEnds;
import net.ripe.db.whois.api.whois.StreamingMarshal;
import net.ripe.db.whois.api.whois.rdap.domain.Domain;
import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Event;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Nameserver;
import net.ripe.db.whois.api.whois.rdap.domain.Notice;
import net.ripe.db.whois.api.whois.rdap.domain.Remark;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.google.common.collect.Maps.immutableEntry;
import static net.ripe.db.whois.api.whois.rdap.VcardObjectHelper.createMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class RdapResponseJsonTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapResponseJsonTest.class);

    private DatatypeFactory dataTypeFactory;

    @Before
    public void setup() {
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
        } catch (Exception ex) {
            LOGGER.error("Failed to init dataTypeFactory");
        }
    }

    @Test
    public void entity_vcard_serialization_test() throws Exception {
        final VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();
        final String date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(1372214924859L));

        builder.setVersion()
                .setFn("Joe User")
                .setN(builder.createNEntryValueType("User", "Joe", "", "", builder.createNEntryValueHonorifics("ing. jr", "M.Sc.")))
                .setBday("--02-03")
                .setAnniversary(date)
                .setGender("M")
                .setKind("individual")
                .addLang(createMap(immutableEntry("pref", "1")), "fr")
                .addLang(createMap(immutableEntry("pref", "2")), "en")
                .setOrg("Example")
                .setTitle("Research Scientist")
                .setRole("Project Lead")
                .addAdr(createMap(immutableEntry("type", "work")), builder.createAdrEntryValueType("",
                        "Suite 1234",
                        "4321 Rue Somewhere",
                        "Quebec",
                        "QC",
                        "G1V 2M2",
                        "Canada"))
                .addAdr(createMap(immutableEntry("pref", "1")), null)
                .addTel(createMap(immutableEntry("type", new String[]{"work", "voice"})), "tel:+1-555-555-1234;ext=102")
                .addTel(createMap(immutableEntry("type", new String[]{"work", "cell", "voice", "video", "text"})), "tel:+1-555-555-4321")
                .setEmail(createMap(immutableEntry("type", "work")), "joe.user@example.com")
                .setGeo(createMap(immutableEntry("type", "work")), "geo:46.772673,-71.282945")
                .setKey(createMap(immutableEntry("type", "work")), "http://www.example.com/joe.user/joe.asc")
                .setTz("-05:00")
                .setUrl(createMap(immutableEntry("type", "work")), "http://example.org");

        final List<Object> objects = builder.build();
        final String result = convertEOLToUnix(streamObject(objects));

        String expectedString = "" +
                "[ \"vcard\", [ [ \"version\", {\n" +
                "}, \"text\", \"4.0\" ], [ \"fn\", {\n" +
                "}, \"text\", \"Joe User\" ], [ \"n\", {\n" +
                "}, \"text\", [ \"User\", \"Joe\", \"\", \"\", [ \"ing. jr\", \"M.Sc.\" ] ] ], [ \"bday\", {\n" +
                "}, \"date-and-or-time\", \"--02-03\" ], [ \"anniversary\", {\n" +
                "}, \"date-and-or-time\", \"2013-06-26T12:48:44+1000\" ], [ \"gender\", {\n" +
                "}, \"text\", \"M\" ], [ \"kind\", {\n" +
                "}, \"text\", \"individual\" ], [ \"lang\", {\n" +
                "  \"pref\" : \"1\"\n" +
                "}, \"language-tag\", \"fr\" ], [ \"lang\", {\n" +
                "  \"pref\" : \"2\"\n" +
                "}, \"language-tag\", \"en\" ], [ \"org\", {\n" +
                "}, \"text\", \"Example\" ], [ \"title\", {\n" +
                "}, \"text\", \"Research Scientist\" ], [ \"role\", {\n" +
                "}, \"text\", \"Project Lead\" ], [ \"adr\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"text\", [ \"\", \"Suite 1234\", \"4321 Rue Somewhere\", \"Quebec\", \"QC\", \"G1V 2M2\", \"Canada\" ] ], [ \"adr\", {\n" +
                "  \"pref\" : \"1\"\n" +
                "}, \"text\", [ \"\", \"\", \"\", \"\", \"\", \"\", \"\" ] ], [ \"tel\", {\n" +
                "  \"type\" : [ \"work\", \"voice\" ]\n" +
                "}, \"uri\", \"tel:+1-555-555-1234;ext=102\" ], [ \"tel\", {\n" +
                "  \"type\" : [ \"work\", \"cell\", \"voice\", \"video\", \"text\" ]\n" +
                "}, \"uri\", \"tel:+1-555-555-4321\" ], [ \"email\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"text\", \"joe.user@example.com\" ], [ \"geo\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"uri\", \"geo:46.772673,-71.282945\" ], [ \"key\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"text\", \"http://www.example.com/joe.user/joe.asc\" ], [ \"tz\", {\n" +
                "}, \"utc-offset\", \"-05:00\" ], [ \"key\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"text\", \"http://example.org\" ] ] ]";

        assertThat(expectedString,sameJSONAs(result));
    }

    @Test
    public void nameserver_serialization_test() throws Exception {
        final Nameserver nameserver = new Nameserver();
        nameserver.setHandle("handle");
        nameserver.setLdhName("ns1.xn--fo-5ja.example");
        nameserver.setUnicodeName("foo.example");
        nameserver.getStatus().add("active");

        final Nameserver.IpAddresses ipAddresses = new Nameserver.IpAddresses();
        ipAddresses.getIpv4().add("192.0.2.1");
        ipAddresses.getIpv4().add("192.0.2.2");
        ipAddresses.getIpv6().add("2001:db8::123");
        nameserver.setIpAddresses(ipAddresses);

        final List<String> remarkList = new ArrayList<>();
        final Remark remarks1 = new Remark();
        remarkList.add("She sells sea shells down by the sea shore.");
        remarkList.add("Originally written by Terry Sullivan.");

        remarks1.getDescription().addAll(remarkList);
        nameserver.getRemarks().add(remarks1);


        final Link link = new Link();
        link.setHref("http://example.net/nameserver/xxxx");
        link.setValue("http://example.net/nameserver/xxxx");
        link.setRel("self");
        nameserver.getLinks().add(link);

        nameserver.setPort43("whois.example.net");

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction("registration");

        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(1372214924859L);

        registrationEvent.setEventDate(dataTypeFactory.newXMLGregorianCalendar(calendar));

        nameserver.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction("last changed");
        lastChangedEvent.setEventDate(dataTypeFactory.newXMLGregorianCalendar(calendar));

        lastChangedEvent.setEventActor("joe@example.com");
        nameserver.getEvents().add(lastChangedEvent);

        final String result = convertEOLToUnix(streamObject(nameserver));

        String expectedString = "" +
                "{\n" +
                "  \"ldhName\" : \"ns1.xn--fo-5ja.example\",\n" +
                "  \"unicodeName\" : \"foo.example\",\n" +
                "  \"ipAddresses\" : {\n" +
                "    \"ipv4\" : [ \"192.0.2.1\", \"192.0.2.2\" ],\n" +
                "    \"ipv6\" : [ \"2001:db8::123\" ]\n" +
                "  },\n" +
                "  \"port43\" : \"whois.example.net\",\n" +
                "  \"status\" : [ \"active\" ],\n" +
                "  \"remarks\" : [ {\n" +
                "    \"description\" : [ \"She sells sea shells down by the sea shore.\", \"Originally written by Terry Sullivan.\" ]\n" +
                "  } ],\n" +
                "  \"links\" : [ {\n" +
                "    \"value\" : \"http://example.net/nameserver/xxxx\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"href\" : \"http://example.net/nameserver/xxxx\"\n" +
                "  } ],\n" +
                "  \"events\" : [ {\n" +
                "    \"eventAction\" : \"registration\",\n" +
                "    \"eventDate\" : \"2013-06-26T02:48:44Z\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"2013-06-26T02:48:44Z\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ],\n" +
                "  \"handle\" : \"handle\"\n" +
                "}";

        assertThat(expectedString,sameJSONAs(result));

    }

    @Test
    public void domain_serialization_test() throws Exception {
        final Domain domain = new Domain();

        domain.setHandle("XXXX");
        domain.setLdhName("192.in-addr.arpa");


        final Nameserver nameserver1 = new Nameserver();
        nameserver1.setLdhName("ns1.rir.example");
        domain.getNameServers().add(nameserver1);

        final Nameserver nameserver2 = new Nameserver();
        nameserver2.setLdhName("ns2.rir.example");
        domain.getNameServers().add(nameserver2);

        final List<String> remarkList = new ArrayList<>();
        final Remark remark = new Remark();
        remarkList.add("She sells sea shells down by the sea shore.");
        remarkList.add("Originally written by Terry Sullivan.");

        remark.getDescription().addAll(remarkList);
        domain.getRemarks().add(remark);

        final Link link = new Link();
        link.setHref("http://example.net/domain/XXXXX");
        link.setValue("http://example.net/domain/XXXX");
        link.setRel("self");
        domain.getLinks().add(link);

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction("registration");

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(1372214924859L);

        registrationEvent.setEventDate(dataTypeFactory.newXMLGregorianCalendar(calendar));

        domain.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction("last changed");
        lastChangedEvent.setEventDate(dataTypeFactory.newXMLGregorianCalendar(calendar));
        lastChangedEvent.setEventActor("joe@example.com");
        domain.getEvents().add(lastChangedEvent);

        final Entity entity = new Entity();
        entity.setHandle("XXXX");
        entity.getRoles().add("registrant");

        entity.getRemarks().add(remark);

        entity.getEvents().add(registrationEvent);
        entity.getEvents().add(lastChangedEvent);

        domain.getEntities().add(entity);

        final Link entityLink = new Link();
        entityLink.setHref("http://example.net/entity/xxxx");
        entityLink.setValue("http://example.net/entity/xxxx");
        entityLink.setRel("self");
        entity.getLinks().add(entityLink);

        final VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();

        builder.setVersion()
                .setFn("Joe User")
                .setKind("individual")
                .setOrg("Example")
                .setTitle("Research Scientist")
                .setRole("Project Lead")
                .addAdr(builder.createAdrEntryValueType("",
                        "Suite 1234",
                        "4321 Rue Somewhere",
                        "Quebec",
                        "QC",
                        "G1V 2M2",
                        "Canada"))
                .addTel("tel:+1-555-555-1234;ext=102")
                .setEmail("joe.user@example.com");

        entity.setVcardArray(builder.build());

        final Domain.SecureDNS secureDNS = new Domain.SecureDNS();

        secureDNS.setDelegationSigned(new Boolean(true));

        final Domain.SecureDNS.DsData dsData = new Domain.SecureDNS.DsData();
        dsData.setKeyTag(BigInteger.valueOf(12345L));
        dsData.setAlgorithm(BigInteger.valueOf(3));
        dsData.setDigestType(BigInteger.valueOf(1));
        dsData.setDigest("49FD46E6C4B45C55D4AC");

        secureDNS.getDsData().add(dsData);

        domain.setSecureDNS(secureDNS);

        final String result = convertEOLToUnix(streamObject(domain));

        String expectedString = "" +
                "{\n" +
                " \"handle\" : \"XXXX\",\n" +
                " \"ldhName\" : \"192.in-addr.arpa\",\n" +
                " \"nameServers\" :\n" +
                " [\n" +
                "   { \"ldhName\" : \"ns1.rir.example\" },\n" +
                "   { \"ldhName\" : \"ns2.rir.example\" }\n" +
                " ],\n" +
                " \"secureDNS\":\n" +
                " {\n" +
                "   \"delegationSigned\": true,\n" +
                "   \"dsData\":\n" +
                "   [\n" +
                "     {\n" +
                "       \"keyTag\": 12345,\n" +
                "       \"algorithm\": 3,\n" +
                "       \"digestType\": 1,\n" +
                "       \"digest\": \"49FD46E6C4B45C55D4AC\"\n" +
                "     }\n" +
                "   ]\n" +
                " },\n" +
                " \"remarks\" :\n" +
                " [\n" +
                "   {\n" +
                "     \"description\" :\n" +
                "     [\n" +
                "       \"She sells sea shells down by the sea shore.\",\n" +
                "       \"Originally written by Terry Sullivan.\"\n" +
                "     ]\n" +
                "   }\n" +
                " ],\n" +
                " \"links\" :\n" +
                " [\n" +
                "   {\n" +
                "     \"value\": \"http://example.net/domain/XXXX\",\n" +
                "     \"rel\" : \"self\",\n" +
                "     \"href\" : \"http://example.net/domain/XXXXX\"\n" +
                "   }\n" +
                " ],\n" +
                " \"events\" :\n" +
                " [\n" +
                "   {\n" +
                "     \"eventAction\" : \"registration\",\n" +
                "     \"eventDate\" : \"2013-06-26T02:48:44Z\"\n" +
                "   },\n" +
                "   {\n" +
                "     \"eventAction\" : \"last changed\",\n" +
                "     \"eventDate\" : \"2013-06-26T02:48:44Z\",\n" +
                "     \"eventActor\" : \"joe@example.com\"\n" +
                "   }\n" +
                " ],\n" +
                " \"entities\" :\n" +
                " [\n" +
                "   {\n" +
                "     \"handle\" : \"XXXX\",\n" +
                "    \"vcardArray\" : [ \"vcard\", [ [ \"version\", {\n" +
                "    }, \"text\", \"4.0\" ], [ \"fn\", {\n" +
                "    }, \"text\", \"Joe User\" ], [ \"kind\", {\n" +
                "    }, \"text\", \"individual\" ], [ \"org\", {\n" +
                "    }, \"text\", \"Example\" ], [ \"title\", {\n" +
                "    }, \"text\", \"Research Scientist\" ], [ \"role\", {\n" +
                "    }, \"text\", \"Project Lead\" ], [ \"adr\", {\n" +
                "    }, \"text\", [ \"\", \"Suite 1234\", \"4321 Rue Somewhere\", \"Quebec\", \"QC\", \"G1V 2M2\", \"Canada\" ] ], [ \"tel\", {\n" +
                "    }, \"uri\", \"tel:+1-555-555-1234;ext=102\" ], [ \"email\", {\n" +
                "    }, \"text\", \"joe.user@example.com\" ] ] ],\n" +
                "     \"roles\" : [ \"registrant\" ],\n" +
                "     \"remarks\" :\n" +
                "     [\n" +
                "       {\n" +
                "         \"description\" :\n" +
                "         [\n" +
                "           \"She sells sea shells down by the sea shore.\",\n" +
                "           \"Originally written by Terry Sullivan.\"\n" +
                "         ]\n" +
                "       }\n" +
                "     ],\n" +
                "     \"links\" :\n" +
                "     [\n" +
                "       {\n" +
                "         \"value\": \"http://example.net/entity/xxxx\",\n" +
                "         \"rel\" : \"self\",\n" +
                "         \"href\" : \"http://example.net/entity/xxxx\"\n" +
                "       }\n" +
                "     ],\n" +
                "     \"events\" :\n" +
                "     [\n" +
                "       {\n" +
                "         \"eventAction\" : \"registration\",\n" +
                "         \"eventDate\" : \"2013-06-26T02:48:44Z\"\n" +
                "       },\n" +
                "       {\n" +
                "         \"eventAction\" : \"last changed\",\n" +
                "         \"eventDate\" : \"2013-06-26T02:48:44Z\",\n" +
                "         \"eventActor\" : \"joe@example.com\"\n" +
                "       }\n" +
                "     ]\n" +
                "   }\n" +
                " ]\n" +
                "}\n";

        assertThat(expectedString, sameJSONAs(result));
    }

    @Test
    public void ip_serialization_test() throws Exception {
        final Ip ip = new Ip();
        ip.setHandle("XXXX-RIR");
        ip.setParentHandle("YYYY-RIR");
        ip.setStartAddress("2001:db8::0");
        ip.setEndAddress("2001:db8::0:FFFF:FFFF:FFFF:FFFF:FFFF");
        ip.setIpVersion("v6");
        ip.setName("NET-RTR-1");
        ip.setType("DIRECT ALLOCATION");
        ip.setCountry("AU");
        ip.getStatus().add("allocated");

        final List<String> remarkList = new ArrayList<>();
        final Remark remark = new Remark();
        remarkList.add("She sells sea shells down by the sea shore.");
        remarkList.add("Originally written by Terry Sullivan.");

        remark.getDescription().addAll(remarkList);
        ip.getRemarks().add(remark);


        final Link link = new Link();
        link.setHref("http://example.net/ip/2001:db8::/48");
        link.setValue("http://example.net/ip/2001:db8::/48");
        link.setRel("self");
        ip.getLinks().add(link);

        final Link uplink = new Link();
        uplink.setHref("http://example.net/ip/2001:C00::/23");
        uplink.setValue("http://example.net/ip/2001:db8::/48");
        uplink.setRel("up");
        ip.getLinks().add(uplink);

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction("registration");

        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(1372214924859L);

        registrationEvent.setEventDate(dataTypeFactory.newXMLGregorianCalendar(calendar));

        ip.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction("last changed");
        lastChangedEvent.setEventDate(dataTypeFactory.newXMLGregorianCalendar(calendar));
        lastChangedEvent.setEventActor("joe@example.com");
        ip.getEvents().add(lastChangedEvent);

        final Entity entity = new Entity();
        entity.setHandle("XXXX");
        entity.getRoles().add("registrant");

        entity.getRemarks().add(remark);

        entity.getEvents().add(registrationEvent);
        entity.getEvents().add(lastChangedEvent);

        ip.getEntities().add(entity);

        final Link entityLink = new Link();
        entityLink.setHref("http://example.net/entity/xxxx");
        entityLink.setValue("http://example.net/entity/xxxx");
        entityLink.setRel("self");
        entity.getLinks().add(entityLink);

        final VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();

        builder.setVersion()
                .setFn("Joe User")
                .setKind("individual")
                .setOrg("Example")
                .setTitle("Research Scientist")
                .setRole("Project Lead")
                .addAdr(builder.createAdrEntryValueType("",
                        "Suite 1234",
                        "4321 Rue Somewhere",
                        "Quebec",
                        "QC",
                        "G1V 2M2",
                        "Canada"))
                .addTel("tel:+1-555-555-1234;ext=102")
                .setEmail("joe.user@example.com");

        entity.setVcardArray(builder.build());

        final String result = convertEOLToUnix(streamObject(ip));

        String expectedString = "" +
                "{\n" +
                "  \"handle\" : \"XXXX-RIR\",\n" +
                "  \"startAddress\" : \"2001:db8::0\",\n" +
                "  \"endAddress\" : \"2001:db8::0:FFFF:FFFF:FFFF:FFFF:FFFF\",\n" +
                "  \"ipVersion\" : \"v6\",\n" +
                "  \"name\" : \"NET-RTR-1\",\n" +
                "  \"type\" : \"DIRECT ALLOCATION\",\n" +
                "  \"country\" : \"AU\",\n" +
                "  \"parentHandle\" : \"YYYY-RIR\",\n" +
                "  \"status\" : [ \"allocated\" ],\n" +
                "  \"entities\" : [ {\n" +
                "    \"handle\" : \"XXXX\",\n" +
                "    \"vcardArray\" : [ \"vcard\", [ [ \"version\", {\n" +
                "    }, \"text\", \"4.0\" ], [ \"fn\", {\n" +
                "    }, \"text\", \"Joe User\" ], [ \"kind\", {\n" +
                "    }, \"text\", \"individual\" ], [ \"org\", {\n" +
                "    }, \"text\", \"Example\" ], [ \"title\", {\n" +
                "    }, \"text\", \"Research Scientist\" ], [ \"role\", {\n" +
                "    }, \"text\", \"Project Lead\" ], [ \"adr\", {\n" +
                "    }, \"text\", [ \"\", \"Suite 1234\", \"4321 Rue Somewhere\", \"Quebec\", \"QC\", \"G1V 2M2\", \"Canada\" ] ], [ \"tel\", {\n" +
                "    }, \"uri\", \"tel:+1-555-555-1234;ext=102\" ], [ \"email\", {\n" +
                "    }, \"text\", \"joe.user@example.com\" ] ] ],\n" +
                "    \"roles\" : [ \"registrant\" ],\n" +
                "    \"remarks\" : [ {\n" +
                "      \"description\" : [ \"She sells sea shells down by the sea shore.\", \"Originally written by Terry Sullivan.\" ]\n" +
                "    } ],\n" +
                "    \"links\" : [ {\n" +
                "      \"value\" : \"http://example.net/entity/xxxx\",\n" +
                "      \"rel\" : \"self\",\n" +
                "      \"href\" : \"http://example.net/entity/xxxx\"\n" +
                "    } ],\n" +
                "    \"events\" : [ {\n" +
                "      \"eventAction\" : \"registration\",\n" +
                "      \"eventDate\" : \"2013-06-26T02:48:44Z\"\n" +
                "    }, {\n" +
                "      \"eventAction\" : \"last changed\",\n" +
                "      \"eventDate\" : \"2013-06-26T02:48:44Z\",\n" +
                "      \"eventActor\" : \"joe@example.com\"\n" +
                "    } ]\n" +
                "  } ],\n" +
                "  \"remarks\" : [ {\n" +
                "    \"description\" : [ \"She sells sea shells down by the sea shore.\", \"Originally written by Terry Sullivan.\" ]\n" +
                "  } ],\n" +
                "  \"links\" : [ {\n" +
                "    \"value\" : \"http://example.net/ip/2001:db8::/48\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"href\" : \"http://example.net/ip/2001:db8::/48\"\n" +
                "  }, {\n" +
                "    \"value\" : \"http://example.net/ip/2001:db8::/48\",\n" +
                "    \"rel\" : \"up\",\n" +
                "    \"href\" : \"http://example.net/ip/2001:C00::/23\"\n" +
                "  } ],\n" +
                "  \"events\" : [ {\n" +
                "    \"eventAction\" : \"registration\",\n" +
                "    \"eventDate\" : \"2013-06-26T02:48:44Z\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"2013-06-26T02:48:44Z\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ]\n" +
                "}";

        assertThat(expectedString,sameJSONAs(result));
    }


    @Test
    public void notices_serialization_test() throws Exception {
        final Notice notices = new Notice();
        notices.setTitle("Beverage policy");
        notices.getDescription().add("Beverages with caffeine for keeping horses awake.");
        notices.getDescription().add("Very effective.");

        final Link link = new Link();
        link.setValue("http://example.com/context_uri");
        link.setRel("self");
        link.setHref("http://example.com/target_uri_href");
        link.getHreflang().add("en");
        link.getHreflang().add("ch");
        link.getTitle().add("title1");
        link.getTitle().add("title2");
        link.setMedia("screen");
        link.setType("application/json");
        notices.setLinks(link);

        String result = convertEOLToUnix(streamObject(notices));

        String expectedString = "" +
                "{\n" +
                "  \"title\" : \"Beverage policy\",\n" +
                "  \"description\" : [ \"Beverages with caffeine for keeping horses awake.\", \"Very effective.\" ],\n" +
                "  \"links\" : {\n" +
                "    \"value\" : \"http://example.com/context_uri\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"href\" : \"http://example.com/target_uri_href\",\n" +
                "    \"hreflang\" : [ \"en\", \"ch\" ],\n" +
                "    \"title\" : [ \"title1\", \"title2\" ],\n" +
                "    \"media\" : \"screen\",\n" +
                "    \"type\" : \"application/json\"\n" +
                "  }\n" +
                "}";

        assertThat(expectedString,sameJSONAs(result));
    }


    private StringOutputStream streamObject(Object o) {
        final StreamingMarshal streamingMarshal = new RdapStreamingMarshalJson();
        final StringOutputStream serializer = new StringOutputStream();

        streamingMarshal.open(serializer);
        streamingMarshal.start("");
        streamingMarshal.writeObject(o);
        streamingMarshal.close();

        return serializer;
    }

    private String convertEOLToUnix(StringOutputStream serializer) throws IOException {
        final StringOutputStream resultStream = new StringOutputStream();
        LineEnds.convert(new StringInputStream(serializer.toString()), resultStream, LineEnds.STYLE_UNIX);

        return resultStream.toString();
    }
}
