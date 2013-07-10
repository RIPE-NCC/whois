package net.ripe.db.whois.api.whois.rdap;

import net.ripe.db.whois.api.whois.rdap.domain.Entity;
import net.ripe.db.whois.api.whois.rdap.domain.Event;
import net.ripe.db.whois.api.whois.rdap.domain.Ip;
import net.ripe.db.whois.api.whois.rdap.domain.Link;
import net.ripe.db.whois.api.whois.rdap.domain.Nameserver;
import net.ripe.db.whois.api.whois.rdap.domain.Notice;
import net.ripe.db.whois.api.whois.rdap.domain.Remark;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.codehaus.plexus.util.StringOutputStream;
import org.joda.time.LocalDateTime;
import org.junit.Test;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.google.common.collect.Maps.immutableEntry;
import static net.ripe.db.whois.api.whois.rdap.VCardObjectHelper.createHashMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class RdapResponseJsonTest {

    private static final String DATE_TIME = "2013-06-26T04:48:44Z";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.parse("2013-06-26T04:48:44");

    @Test
    public void entity_vcard_serialization_test() throws Exception {
        VCardBuilder builder = new VCardBuilder();

        builder.addVersion()
                .addFn("Joe User")
                .addN(builder.createNEntryValueType("User", "Joe", "", "", builder.createNEntryValueHonorifics("ing. jr", "M.Sc.")))
                .addBday("--02-03")
                .addAnniversary("20130101")
                .addGender("M")
                .addKind("individual")
                .addLang(createHashMap(immutableEntry("pref", "1")), "fr")
                .addLang(createHashMap(immutableEntry("pref", "2")), "en")
                .addOrg("Example")
                .addTitle("Research Scientist")
                .addRole("Project Lead")
                .addAdr(createHashMap(immutableEntry("type", "work")), builder.createAdrEntryValueType("",
                        "Suite 1234",
                        "4321 Rue Somewhere",
                        "Quebec",
                        "QC",
                        "G1V 2M2",
                        "Canada"))
                .addAdr(createHashMap(immutableEntry("pref", "1")), null)
                .addTel(createHashMap(immutableEntry("type", new String[]{"work", "voice"})), "tel:+1-555-555-1234;ext=102")                      // TODO
                .addTel(createHashMap(immutableEntry("type", new String[]{"work", "cell", "voice", "video", "text"})), "tel:+1-555-555-4321")     // TODO
                .addEmail(createHashMap(immutableEntry("type", "work")), "joe.user@example.com")
                .addGeo(createHashMap(immutableEntry("type", "work")), "geo:46.772673,-71.282945")
                .addKey(createHashMap(immutableEntry("type", "work")), "http://www.example.com/joe.user/joe.asc")
                .addTz("-05:00")
                .addUrl(createHashMap(immutableEntry("type", "work")), "http://example.org");

        assertThat(marshal(builder.build()), equalTo("" +
                "[ \"vcard\", [ [ \"version\", {\n" +
                "}, \"text\", \"4.0\" ], [ \"fn\", {\n" +
                "}, \"text\", \"Joe User\" ], [ \"n\", {\n" +
                "}, \"text\", [ \"User\", \"Joe\", \"\", \"\", [ \"ing. jr\", \"M.Sc.\" ] ] ], [ \"bday\", {\n" +
                "}, \"date-and-or-time\", \"--02-03\" ], [ \"anniversary\", {\n" +
                "}, \"date-and-or-time\", \"20130101\" ], [ \"gender\", {\n" +
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
                "}, \"text\", \"http://example.org\" ] ] ]"));
    }

    @Test
    public void nameserver_serialization_test() throws Exception {
        Nameserver nameserver = new Nameserver();
        nameserver.setHandle("handle");
        nameserver.setLdhName("ns1.xn--fo-5ja.example");
        nameserver.setUnicodeName("foo.example");
        nameserver.getStatus().add("active");
        Nameserver.IpAddresses ipAddresses = new Nameserver.IpAddresses();
        ipAddresses.getIpv4().add("192.0.2.1");
        ipAddresses.getIpv4().add("192.0.2.2");
        ipAddresses.getIpv6().add("2001:db8::123");
        nameserver.setIpAddresses(ipAddresses);

        List<String> remarkList = new ArrayList<>();
        Remark remarks1 = new Remark();
        remarkList.add("She sells sea shells down by the sea shore.");
        remarkList.add("Originally written by Terry Sullivan.");

        remarks1.getDescription().addAll(remarkList);
        nameserver.getRemarks().add(remarks1);

        Link link = new Link();
        link.setHref("http://example.net/nameserver/xxxx");
        link.setValue("http://example.net/nameserver/xxxx");
        link.setRel("self");
        nameserver.getLinks().add(link);

        nameserver.setPort43("whois.example.net");

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction("registration");
        registrationEvent.setEventDate(LOCAL_DATE_TIME);
        nameserver.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction("last changed");
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
                "    \"eventDate\" : \"" + DATE_TIME + "\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"" + DATE_TIME + "\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ]\n" +
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

        List<String> remarkList = new ArrayList<>();
        Remark remark = new Remark();
        remarkList.add("She sells sea shells down by the sea shore.");
        remarkList.add("Originally written by Terry Sullivan.");
        remark.getDescription().addAll(remarkList);
        ip.getRemarks().add(remark);

        Link link = new Link();
        link.setHref("http://example.net/ip/2001:db8::/48");
        link.setValue("http://example.net/ip/2001:db8::/48");
        link.setRel("self");
        ip.getLinks().add(link);

        Link uplink = new Link();
        uplink.setHref("http://example.net/ip/2001:C00::/23");
        uplink.setValue("http://example.net/ip/2001:db8::/48");
        uplink.setRel("up");
        ip.getLinks().add(uplink);

        final Event registrationEvent = new Event();
        registrationEvent.setEventAction("registration");
        registrationEvent.setEventDate(LOCAL_DATE_TIME);
        ip.getEvents().add(registrationEvent);

        final Event lastChangedEvent = new Event();
        lastChangedEvent.setEventAction("last changed");
        lastChangedEvent.setEventDate(LOCAL_DATE_TIME);
        lastChangedEvent.setEventActor("joe@example.com");
        ip.getEvents().add(lastChangedEvent);

        Entity entity = new Entity();
        entity.setHandle("XXXX");

        VCardBuilder builder = new VCardBuilder();
        builder.addVersion()
                .addFn("Joe User")
                .addKind("individual")
                .addOrg("Example")
                .addTitle("Research Scientist")
                .addRole("Project Lead")
                .addAdr(builder.createAdrEntryValueType("",
                        "Suite 1234",
                        "4321 Rue Somewhere",
                        "Quebec",
                        "QC",
                        "G1V 2M2",
                        "Canada"))
                .addTel("tel:+1-555-555-1234;ext=102")
                .addEmail("joe.user@example.com");
        entity.setVcardArray(builder.build());
        entity.getRoles().add("registrant");
        entity.getRemarks().add(remark);
        entity.getEvents().add(registrationEvent);
        entity.getEvents().add(lastChangedEvent);
        ip.getEntities().add(entity);

        Link entityLink = new Link();
        entityLink.setHref("http://example.net/entity/xxxx");
        entityLink.setValue("http://example.net/entity/xxxx");
        entityLink.setRel("self");
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
                "      \"eventDate\" : \"" + DATE_TIME + "\"\n" +
                "    }, {\n" +
                "      \"eventAction\" : \"last changed\",\n" +
                "      \"eventDate\" : \"" + DATE_TIME + "\",\n" +
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
                "    \"eventDate\" : \"" + DATE_TIME + "\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"" + DATE_TIME + "\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ]\n" +
                "}"));
    }

    @Test
    public void notices_serialization_test() throws Exception {
        Notice notices = new Notice();
        notices.setTitle("Beverage policy");
        notices.getDescription().add("Beverages with caffeine for keeping horses awake.");
        notices.getDescription().add("Very effective.");
        Link link = new Link();
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

        assertThat(marshal(notices), equalTo("" +
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
                "}"));
    }

    // helper methods

    private String marshal(Object o) throws IOException {
        final StringOutputStream outputStream = new StringOutputStream();

        final JsonFactory jsonFactory = createJsonFactory();
        JsonGenerator generator = jsonFactory.createJsonGenerator(outputStream).useDefaultPrettyPrinter();
        generator.writeObject(o);
        generator.close();

        return outputStream.toString();
    }

    private JsonFactory createJsonFactory() {
        final ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setAnnotationIntrospector(
                new AnnotationIntrospector.Pair(
                        new JacksonAnnotationIntrospector(),
                        new JaxbAnnotationIntrospector()));

        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        objectMapper.configure(SerializationConfig.Feature.WRITE_EMPTY_JSON_ARRAYS, true);

        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        objectMapper.setDateFormat(df);

        return objectMapper.getJsonFactory();
    }
}
