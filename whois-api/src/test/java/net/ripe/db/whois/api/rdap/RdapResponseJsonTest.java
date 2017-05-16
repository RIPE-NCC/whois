package net.ripe.db.whois.api.rdap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.AnnotationIntrospectorPair;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rdap.domain.Action;
import net.ripe.db.whois.api.rdap.domain.Domain;
import net.ripe.db.whois.api.rdap.domain.Entity;
import net.ripe.db.whois.api.rdap.domain.Event;
import net.ripe.db.whois.api.rdap.domain.Ip;
import net.ripe.db.whois.api.rdap.domain.Link;
import net.ripe.db.whois.api.rdap.domain.Nameserver;
import net.ripe.db.whois.api.rdap.domain.Notice;
import net.ripe.db.whois.api.rdap.domain.Remark;
import net.ripe.db.whois.api.rdap.domain.Role;
import net.ripe.db.whois.api.rdap.domain.vcard.VCard;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static com.google.common.collect.Maps.immutableEntry;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class RdapResponseJsonTest {

    private static final String DATE_TIME = "2013-06-26T04:48:44Z";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse("2013-06-26T04:48:44");

    @Test
    public void entity() throws Exception {
        VCardBuilder builder = new VCardBuilder();
        VCard vcard = builder
                .addVersion()
                .addFn("Joe User")
                .addN(Lists.newArrayList("User", "Joe", "", Lists.newArrayList("ing. jr", "M.Sc.")))
                .addGender("M")
                .addLang(createMap(immutableEntry("pref", "1")), "fr")
                .build();

        Entity entity = new Entity();
        entity.setHandle("XXXX");
        entity.setVCardArray(vcard);

        assertThat(marshal(entity), equalTo("" +
                "{\n" +
                "  \"handle\" : \"XXXX\",\n" +
                "  \"vcardArray\" :" +
                " [ \"vcard\", [" +
                " [ \"version\", { }, \"text\", \"4.0\" ]," +
                " [ \"fn\", { }, \"text\", \"Joe User\" ]," +
                " [ \"n\", { }, \"text\", [ \"User\", \"Joe\", \"\", [ \"ing. jr\", \"M.Sc.\" ] ] ]," +
                " [ \"gender\", { }, \"text\", \"M\" ], [ \"lang\", {\n" +
                "    \"pref\" : \"1\"\n" +
                "  }, \"language-tag\", \"fr\" ] ] ],\n" +
                "  \"objectClassName\" : \"entity\"\n" +
                "}"));
    }

    @Test
    public void entity_vcard_serialization_test() throws Exception {
        final VCardBuilder builder = new VCardBuilder();

        builder.addVersion()
                .addFn("Joe User")
                .addN(createName("User", "Joe", "", "", createHonorifics("ing. jr", "M.Sc.")))
                .addBday("--02-03")
                .addAnniversary("20130101")
                .addGender("M")
                .addKind("individual")
                .addLang(createMap(immutableEntry("pref", "1")), "fr")
                .addLang(createMap(immutableEntry("pref", "2")), "en")
                .addOrg("Example")
                .addTitle("Research Scientist")
                .addRole("Project Lead")
                .addAdr(createMap(immutableEntry("type", "work")), createAddress("", "Suite 1234", "4321 Rue Somewhere", "Quebec", "QC", "G1V 2M2", "Canada"))
                .addAdr(createMap(immutableEntry("pref", "1")), createAddress("", "", "", "", "", "", ""))
                .addTel(createMap(immutableEntry("type", new String[]{"work", "voice"})), "tel:+1-555-555-1234;ext=102")
                .addTel(createMap(immutableEntry("type", new String[]{"work", "cell", "voice", "video", "text"})), "tel:+1-555-555-4321")
                .addEmail(createMap(immutableEntry("type", "work")), "joe.user@example.com")
                .addGeo(createMap(immutableEntry("type", "work")), "geo:46.772673,-71.282945")
                .addKey(createMap(immutableEntry("type", "work")), "http://www.example.com/joe.user/joe.asc")
                .addTz("-05:00")
                .addKey(createMap(immutableEntry("type", "work")), "http://example.org");

        assertThat(marshal(builder.build()), equalTo("" +
                "{\n  \"vcard\" : [ [ \"version\", {" +
                " }, \"text\", \"4.0\" ], [ \"fn\", {" +
                " }, \"text\", \"Joe User\" ], [ \"n\", {" +
                " }, \"text\", [ \"User\", \"Joe\", \"\", \"\", [ \"ing. jr\", \"M.Sc.\" ] ] ], [ \"bday\", {" +
                " }, \"date-and-or-time\", \"--02-03\" ], [ \"anniversary\", {" +
                " }, \"date-and-or-time\", \"20130101\" ], [ \"gender\", {" +
                " }, \"text\", \"M\" ], [ \"kind\", {" +
                " }, \"text\", \"individual\" ], [ \"lang\", {\n" +
                "    \"pref\" : \"1\"\n" +
                "  }, \"language-tag\", \"fr\" ], [ \"lang\", {\n" +
                "    \"pref\" : \"2\"\n" +
                "  }, \"language-tag\", \"en\" ], [ \"org\", {" +
                " }, \"text\", \"Example\" ], [ \"title\", {" +
                " }, \"text\", \"Research Scientist\" ], [ \"role\", {" +
                " }, \"text\", \"Project Lead\" ], [ \"adr\", {\n" +
                "    \"type\" : \"work\"\n" +
                "  }, \"text\", [ \"\", \"Suite 1234\", \"4321 Rue Somewhere\", \"Quebec\", \"QC\", \"G1V 2M2\", \"Canada\" ] ], [ \"adr\", {\n" +
                "    \"pref\" : \"1\"\n" +
                "  }, \"text\", [ \"\", \"\", \"\", \"\", \"\", \"\", \"\" ] ], [ \"tel\", {\n" +
                "    \"type\" : [ \"work\", \"voice\" ]\n" +
                "  }, \"uri\", \"tel:+1-555-555-1234;ext=102\" ], [ \"tel\", {\n" +
                "    \"type\" : [ \"work\", \"cell\", \"voice\", \"video\", \"text\" ]\n" +
                "  }, \"uri\", \"tel:+1-555-555-4321\" ], [ \"email\", {\n" +
                "    \"type\" : \"work\"\n" +
                "  }, \"text\", \"joe.user@example.com\" ], [ \"geo\", {\n" +
                "    \"type\" : \"work\"\n" +
                "  }, \"uri\", \"geo:46.772673,-71.282945\" ], [ \"key\", {\n" +
                "    \"type\" : \"work\"\n" +
                "  }, \"text\", \"http://www.example.com/joe.user/joe.asc\" ], [ \"tz\", {" +
                " }, \"utc-offset\", \"-05:00\" ], [ \"key\", {\n" +
                "    \"type\" : \"work\"\n" +
                "  }, \"text\", \"http://example.org\" ] ]\n}"));
    }

    private List createName(final String surname, final String given, final String prefix, final String suffix, final List honorifics) {
        return Lists.newArrayList(surname, given, prefix, suffix, honorifics);
    }

    private List createHonorifics(final String prefix, final String suffix) {
        return Lists.newArrayList(prefix, suffix);
    }

    private List createAddress(final String pobox, final String ext, final String street, final String locality, final String region, final String code, final String country) {
        return Lists.newArrayList(pobox, ext, street, locality, region, code, country);
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
        nameserver.getRemarks().add(new Remark(Lists.newArrayList("She sells sea shells down by the sea shore.", "Originally written by Terry Sullivan.")));
        nameserver.getLinks().add(new Link("http://example.net/nameserver/xxxx", "self", "http://example.net/nameserver/xxxx", null, null));
        nameserver.setPort43("whois.example.net");

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction(Action.REGISTRATION);
        registrationEvent.setEventDate(LOCAL_DATE_TIME);
        nameserver.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction(Action.LAST_CHANGED);
        lastChangedEvent.setEventDate(LOCAL_DATE_TIME);
        lastChangedEvent.setEventActor("joe@example.com");
        nameserver.getEvents().add(lastChangedEvent);

        assertThat(marshal(nameserver), equalTo("" +
                "{\n" +
                "  \"handle\" : \"handle\",\n" +
                "  \"ldhName\" : \"ns1.xn--fo-5ja.example\",\n" +
                "  \"unicodeName\" : \"foo.example\",\n" +
                "  \"ipAddresses\" : {\n" +
                "    \"ipv4\" : [ \"192.0.2.1\", \"192.0.2.2\" ],\n" +
                "    \"ipv6\" : [ \"2001:db8::123\" ]\n" +
                "  },\n" +
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
                "    \"eventDate\" : \"" + DATE_TIME + "\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"" + DATE_TIME + "\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ],\n" +
                "  \"port43\" : \"whois.example.net\",\n" +
                "  \"objectClassName\" : \"nameserver\"\n" +
                "}"));
    }

    @Test
    public void domain_serialization_test() throws Exception {
        final Domain domain = new Domain();

        domain.setHandle("XXXX");
        domain.setLdhName("192.in-addr.arpa");

        final Nameserver nameserver1 = new Nameserver();
        nameserver1.setLdhName("ns1.rir.example");
        domain.getNameservers().add(nameserver1);

        final Nameserver nameserver2 = new Nameserver();
        nameserver2.setLdhName("ns2.rir.example");
        domain.getNameservers().add(nameserver2);

        final Remark remark = new Remark(Lists.newArrayList("She sells sea shells down by the sea shore.", "Originally written by Terry Sullivan."));
        domain.getRemarks().add(remark);
        final Link link = new Link("http://example.net/domain/XXXX", "self", "http://example.net/domain/XXXXX", null, null);
        domain.getLinks().add(link);

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction(Action.REGISTRATION);
        registrationEvent.setEventDate(LOCAL_DATE_TIME);
        domain.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction(Action.LAST_CHANGED);
        lastChangedEvent.setEventDate(LOCAL_DATE_TIME);
        lastChangedEvent.setEventActor("joe@example.com");
        domain.getEvents().add(lastChangedEvent);

        final Entity entity = new Entity();
        entity.setHandle("XXXX");
        entity.getRoles().add(Role.REGISTRANT);

        entity.getRemarks().add(remark);

        entity.getEvents().add(registrationEvent);
        entity.getEvents().add(lastChangedEvent);

        domain.getEntitySearchResults().add(entity);

        final Link entityLink = new Link("http://example.net/entity/xxxx", "self", "http://example.net/entity/xxxx", null, null);
        entity.getLinks().add(entityLink);

        final VCardBuilder builder = new VCardBuilder();

        builder.addVersion()
                .addFn("Joe User")
                .addKind("individual")
                .addOrg("Example")
                .addTitle("Research Scientist")
                .addRole("Project Lead")
                .addAdr(createAddress("", "Suite 1234", "4321 Rue Somewhere", "Quebec", "QC", "G1V 2M2", "Canada"))
                .addTel("tel:+1-555-555-1234;ext=102")
                .addEmail("joe.user@example.com");

        entity.setVCardArray(builder.build());

        final Domain.SecureDNS secureDNS = new Domain.SecureDNS();

        secureDNS.setDelegationSigned(Boolean.TRUE);

        final Domain.SecureDNS.DsData dsData = new Domain.SecureDNS.DsData();
        dsData.setKeyTag(12345L);
        dsData.setAlgorithm((short) 3);
        dsData.setDigestType(1);
        dsData.setDigest("49FD46E6C4B45C55D4AC");

        secureDNS.getDsData().add(dsData);

        domain.setSecureDNS(secureDNS);

        assertThat(marshal(domain), equalTo("" +
                "{\n" +
                "  \"handle\" : \"XXXX\",\n" +
                "  \"ldhName\" : \"192.in-addr.arpa\",\n" +
                "  \"nameServers\" : [ {\n" +
                "    \"ldhName\" : \"ns1.rir.example\",\n" +
                "    \"objectClassName\" : \"nameserver\"\n" +
                "  }, {\n" +
                "    \"ldhName\" : \"ns2.rir.example\",\n" +
                "    \"objectClassName\" : \"nameserver\"\n" +
                "  } ],\n" +
                "  \"secureDNS\" : {\n" +
                "    \"delegationSigned\" : true,\n" +
                "    \"dsData\" : [ {\n" +
                "      \"keyTag\" : 12345,\n" +
                "      \"algorithm\" : 3,\n" +
                "      \"digest\" : \"49FD46E6C4B45C55D4AC\",\n" +
                "      \"digestType\" : 1\n" +
                "    } ]\n" +
                "  },\n" +
                "  \"entities\" : [ {\n" +
                "    \"handle\" : \"XXXX\",\n" +
                "    \"vcardArray\" : [ \"vcard\", [ [ \"version\", {" +        // TODO: vcardarray not formatted properly
                " }, \"text\", \"4.0\" ], [ \"fn\", {" +
                " }, \"text\", \"Joe User\" ], [ \"kind\", {" +
                " }, \"text\", \"individual\" ], [ \"org\", {" +
                " }, \"text\", \"Example\" ], [ \"title\", {" +
                " }, \"text\", \"Research Scientist\" ], [ \"role\", {" +
                " }, \"text\", \"Project Lead\" ], [ \"adr\", {" +
                " }, \"text\", [ \"\", \"Suite 1234\", \"4321 Rue Somewhere\", \"Quebec\", \"QC\", \"G1V 2M2\", \"Canada\" ] ], [ \"tel\", {" +
                " }, \"uri\", \"tel:+1-555-555-1234;ext=102\" ], [ \"email\", {" +
                " }, \"text\", \"joe.user@example.com\" ] ] ],\n" +
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
                "      \"eventDate\" : \"2013-06-26T04:48:44Z\"\n" +
                "    }, {\n" +
                "      \"eventAction\" : \"last changed\",\n" +
                "      \"eventDate\" : \"2013-06-26T04:48:44Z\",\n" +
                "      \"eventActor\" : \"joe@example.com\"\n" +
                "    } ],\n" +
                "    \"objectClassName\" : \"entity\"\n" +
                "  } ],\n" +
                "  \"remarks\" : [ {\n" +
                "    \"description\" : [ \"She sells sea shells down by the sea shore.\", \"Originally written by Terry Sullivan.\" ]\n" +
                "  } ],\n" +
                "  \"links\" : [ {\n" +
                "    \"value\" : \"http://example.net/domain/XXXX\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"href\" : \"http://example.net/domain/XXXXX\"\n" +
                "  } ],\n" +
                "  \"events\" : [ {\n" +
                "    \"eventAction\" : \"registration\",\n" +
                "    \"eventDate\" : \"2013-06-26T04:48:44Z\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"2013-06-26T04:48:44Z\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ],\n" +
                "  \"objectClassName\" : \"domain\"\n" +
                "}"));
    }

    @Test
    public void ip_serialization_test() throws Exception {
        Ip ip = new Ip();
        ip.setHandle("XXXX-RIR");
        ip.setParentHandle("YYYY-RIR");
        ip.setStartAddress("2001:db8::0");
        ip.setEndAddress("2001:db8::0:FFFF:FFFF:FFFF:FFFF:FFFF");
        ip.setIpVersion("v6");
        ip.setName("NET-RTR-1");
        ip.setType("DIRECT ALLOCATION");
        ip.setCountry("AU");
        ip.getStatus().add("allocated");

        final Remark remark = new Remark(Lists.newArrayList("She sells sea shells down by the sea shore.", "Originally written by Terry Sullivan."));
        ip.getRemarks().add(remark);

        final Link link = new Link("http://example.net/ip/2001:db8::/48", "self", "http://example.net/ip/2001:db8::/48", null, null);
        ip.getLinks().add(link);

        final Link uplink = new Link("http://example.net/ip/2001:db8::/48", "up", "http://example.net/ip/2001:C00::/23", null, null);
        ip.getLinks().add(uplink);

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction(Action.REGISTRATION);
        registrationEvent.setEventDate(LOCAL_DATE_TIME);
        ip.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction(Action.LAST_CHANGED);
        lastChangedEvent.setEventDate(LOCAL_DATE_TIME);
        lastChangedEvent.setEventActor("joe@example.com");
        ip.getEvents().add(lastChangedEvent);

        final Entity entity = new Entity();
        entity.setHandle("XXXX");

        final VCardBuilder builder = new VCardBuilder();
        builder.addVersion()
                .addFn("Joe User")
                .addKind("individual")
                .addOrg("Example")
                .addTitle("Research Scientist")
                .addRole("Project Lead")
                .addAdr(createAddress("", "Suite 1234", "4321 Rue Somewhere", "Quebec", "QC", "G1V 2M2", "Canada"))
                .addTel("tel:+1-555-555-1234;ext=102")
                .addEmail("joe.user@example.com");
        entity.setVCardArray(builder.build());
        entity.getRoles().add(Role.REGISTRANT);
        entity.getRemarks().add(remark);
        entity.getEvents().add(registrationEvent);
        entity.getEvents().add(lastChangedEvent);
        ip.getEntitySearchResults().add(entity);

        final Link entityLink = new Link("http://example.net/entity/xxxx", "self", "http://example.net/entity/xxxx", null, null);
        entity.getLinks().add(entityLink);

        assertThat(marshal(ip), equalTo("" +
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
                "    \"vcardArray\" : [ \"vcard\", [ " +
                "[ \"version\", { }, \"text\", \"4.0\" ], " +
                "[ \"fn\", { }, \"text\", \"Joe User\" ], " +
                "[ \"kind\", { }, \"text\", \"individual\" ], " +
                "[ \"org\", { }, \"text\", \"Example\" ], " +
                "[ \"title\", { }, \"text\", \"Research Scientist\" ], " +
                "[ \"role\", { }, \"text\", \"Project Lead\" ], " +
                "[ \"adr\", { }, \"text\", [ \"\", \"Suite 1234\", \"4321 Rue Somewhere\", \"Quebec\", \"QC\", \"G1V 2M2\", \"Canada\" ] ], " +
                "[ \"tel\", { }, \"uri\", \"tel:+1-555-555-1234;ext=102\" ], " +
                "[ \"email\", { }, \"text\", \"joe.user@example.com\" ] ] ],\n" +
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
                "      \"eventDate\" : \"" + DATE_TIME + "\"\n" +
                "    }, {\n" +
                "      \"eventAction\" : \"last changed\",\n" +
                "      \"eventDate\" : \"" + DATE_TIME + "\",\n" +
                "      \"eventActor\" : \"joe@example.com\"\n" +
                "    } ],\n" +
                "    \"objectClassName\" : \"entity\"\n" +
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
                "    \"eventDate\" : \"" + DATE_TIME + "\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"" + DATE_TIME + "\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ],\n" +
                "  \"objectClassName\" : \"ip network\"\n" +
                "}"));
    }

    @Test
    public void notices_serialization_test() throws Exception {
        final Notice notices = new Notice();
        notices.setTitle("Beverage policy");
        notices.getDescription().add("Beverages with caffeine for keeping horses awake.");
        notices.getDescription().add("Very effective.");

        final Link link = new Link("http://example.com/context_uri", "self", "http://example.com/target_uri_href", "screen", "application/json");
        link.getHreflang().add("en");
        link.getHreflang().add("ch");
        link.getTitle().add("title1");
        link.getTitle().add("title2");
        notices.getLinks().add(link);

        assertThat(marshal(notices), equalTo("" +
                "{\n" +
                "  \"title\" : \"Beverage policy\",\n" +
                "  \"description\" : [ \"Beverages with caffeine for keeping horses awake.\", \"Very effective.\" ],\n" +
                "  \"links\" : [ {\n" +
                "    \"value\" : \"http://example.com/context_uri\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"href\" : \"http://example.com/target_uri_href\",\n" +
                "    \"hreflang\" : [ \"en\", \"ch\" ],\n" +
                "    \"title\" : [ \"title1\", \"title2\" ],\n" +
                "    \"media\" : \"screen\",\n" +
                "    \"type\" : \"application/json\"\n" +
                "  } ]\n" +
                "}"));
    }

    // helper methods

    private String marshal(final Object o) throws IOException {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final JsonFactory jsonFactory = createJsonFactory();
        final JsonGenerator generator = jsonFactory.createGenerator(outputStream).useDefaultPrettyPrinter();
        generator.writeObject(o);
        generator.close();

        return outputStream.toString();
    }

    private JsonFactory createJsonFactory() {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setAnnotationIntrospector(
                new AnnotationIntrospectorPair(
                        new JacksonAnnotationIntrospector(),
                        new JaxbAnnotationIntrospector(TypeFactory.defaultInstance())));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true);

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        objectMapper.setDateFormat(df);

        return objectMapper.getFactory();
    }

    private <K, V> Map createMap(final Map.Entry<K, V>... entries) {
        final Map <K, V> ret = new HashMap<>();
        for (final Map.Entry<K, V>entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }
}
