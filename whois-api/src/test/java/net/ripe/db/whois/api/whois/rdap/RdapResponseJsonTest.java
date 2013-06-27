package net.ripe.db.whois.api.whois.rdap;

import com.Ostermiller.util.LineEnds;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.StreamingMarshal;
import net.ripe.db.whois.api.whois.rdap.domain.*;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RdapResponseJsonTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdapResponseJsonTest.class);

    private static DatatypeFactory dataTypeFactory;
    static {
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
        } catch (Exception ex) {
            LOGGER.error("Failed to init dataTypeFactory");
        }
    }

    @Test
    public void entity_vcard_serialization_test() throws Exception {
        VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String date = sdf.format(new Date(1372214924859L));

        builder.setVersion()
                .setFn("Joe User")
                .setN(builder.createNEntryValueType("User", "Joe", "", "", builder.createNEntryValueHonorifics("ing. jr", "M.Sc.")))
                .setBday("--02-03")
                .setAnniversary(date)
                .setGender("M")
                .setKind("individual")
                .addLang(VcardObjectHelper.createHashMap(Maps.immutableEntry("pref", "1")), "fr")
                .addLang(VcardObjectHelper.createHashMap(Maps.immutableEntry("pref", "2")), "en")
                .setOrg("Example")
                .setTitle("Research Scientist")
                .setRole("Project Lead")
                .addAdr(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", "work")), builder.createAdrEntryValueType("",
                        "Suite 1234",
                        "4321 Rue Somewhere",
                        "Quebec",
                        "QC",
                        "G1V 2M2",
                        "Canada"))
                .addAdr(VcardObjectHelper.createHashMap(Maps.immutableEntry("pref", "1")), null)
                .addTel(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", new String[]{"work", "voice"})), "tel:+1-555-555-1234;ext=102")
                .addTel(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", new String[]{"work", "cell", "voice", "video", "text"})), "tel:+1-555-555-4321")
                .setEmail(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", "work")), "joe.user@example.com")
                .setGeo(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", "work")), "geo:46.772673,-71.282945")
                .setKey(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", "work")), "http://www.example.com/joe.user/joe.asc")
                .setTz("-05:00")
                .setUrl(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", "work")), "http://example.org");

        List<Object> objs = VcardObjectHelper.toObjects(builder.build());
        StringOutputStream serializer = streamObject(objs);
        String result = convertEOLToUnix(serializer);

        assertEquals("" +
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
                "}, \"text\", \"http://example.org\" ] ] ]", result);
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

        Remarks remarks1 = new Remarks();
        remarks1.getDescription().add("She sells sea shells down by the sea shore.");
        remarks1.getDescription().add("Originally written by Terry Sullivan.");
        nameserver.getRemarks().add(remarks1);


        Links link = new Links();
        link.setHref("http://example.net/nameserver/xxxx");
        link.setValue("http://example.net/nameserver/xxxx");
        link.setRel("self");
        nameserver.getLinks().add(link);

        nameserver.setPort43("whois.example.net");

        Events event1 = new Events();
        event1.setEventAction("registration");

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(1372214924859L);

        event1.setEventDate(dataTypeFactory.newXMLGregorianCalendar(gc));

        nameserver.getEvents().add(event1);

        Events event2 = new Events();
        event2.setEventAction("last changed");
        event2.setEventDate(dataTypeFactory.newXMLGregorianCalendar(gc));

        event2.setEventActor("joe@example.com");
        nameserver.getEvents().add(event2);

        StringOutputStream serializer = streamObject(nameserver);
        String result = convertEOLToUnix(serializer);

        assertEquals("" +
                "{\n" +
                "  \"handle\" : \"handle\",\n" +
                "  \"ldhName\" : \"ns1.xn--fo-5ja.example\",\n" +
                "  \"unicodeName\" : \"foo.example\",\n" +
                "  \"status\" : [ \"active\" ],\n" +
                "  \"ipAddresses\" : {\n" +
                "    \"ipv4\" : [ \"192.0.2.1\", \"192.0.2.2\" ],\n" +
                "    \"ipv6\" : [ \"2001:db8::123\" ]\n" +
                "  },\n" +
                "  \"remarks\" : [ {\n" +
                "    \"description\" : [ \"She sells sea shells down by the sea shore.\", \"Originally written by Terry Sullivan.\" ]\n" +
                "  } ],\n" +
                "  \"links\" : [ {\n" +
                "    \"value\" : \"http://example.net/nameserver/xxxx\",\n" +
                "    \"rel\" : \"self\",\n" +
                "    \"href\" : \"http://example.net/nameserver/xxxx\"\n" +
                "  } ],\n" +
                "  \"port43\" : \"whois.example.net\",\n" +
                "  \"events\" : [ {\n" +
                "    \"eventAction\" : \"registration\",\n" +
                "    \"eventDate\" : \"2013-06-26T02:48:44Z\"\n" +
                "  }, {\n" +
                "    \"eventAction\" : \"last changed\",\n" +
                "    \"eventDate\" : \"2013-06-26T02:48:44Z\",\n" +
                "    \"eventActor\" : \"joe@example.com\"\n" +
                "  } ]\n" +
                "}", result);
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

        Remarks remarks1 = new Remarks();
        remarks1.getDescription().add("She sells sea shells down by the sea shore.");
        remarks1.getDescription().add( "Originally written by Terry Sullivan.");
        ip.getRemarks().add(remarks1);


        Links link = new Links();
        link.setHref("http://example.net/ip/2001:db8::/48");
        link.setValue("http://example.net/ip/2001:db8::/48");
        link.setRel("self");
        ip.getLinks().add(link);

        Links uplink = new Links();
        uplink.setHref("http://example.net/ip/2001:C00::/23");
        uplink.setValue("http://example.net/ip/2001:db8::/48");
        uplink.setRel("up");
        ip.getLinks().add(uplink);

        Events event1 = new Events();
        event1.setEventAction("registration");

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(1372214924859L);

        event1.setEventDate(dataTypeFactory.newXMLGregorianCalendar(gc));

        ip.getEvents().add(event1);

        Events event2 = new Events();
        event2.setEventAction("last changed");
        event2.setEventDate(dataTypeFactory.newXMLGregorianCalendar(gc));
        event2.setEventActor("joe@example.com");
        ip.getEvents().add(event2);

        Entity entity = new Entity();
        entity.setHandle("XXXX");
        entity.getRoles().add("registrant");

        entity.getRemarks().add(remarks1);

        entity.getEvents().add(event1);
        entity.getEvents().add(event2);

        ip.getEntities().add(entity);

        Links entityLink = new Links();
        entityLink.setHref("http://example.net/entity/xxxx");
        entityLink.setValue("http://example.net/entity/xxxx");
        entityLink.setRel("self");
        entity.getLinks().add(entityLink);

        VcardObjectHelper.VcardBuilder builder = new VcardObjectHelper.VcardBuilder();

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

        entity.setVcardArray(VcardObjectHelper.toObjects(builder.build()));

        StringOutputStream serializer = streamObject(ip);
        String result = convertEOLToUnix(serializer);

        assertEquals("" +
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
                "}", result);
    }



    @Test
    public void notices_serialization_test() throws Exception {

        Notices notices = new Notices();
        notices.setTitle("Beverage policy");
        notices.getDescription().add("Beverages with caffeine for keeping horses awake.");
        notices.getDescription().add("Very effective.");

        Links link = new Links();
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

        StringOutputStream serializer = streamObject(notices);
        String result = convertEOLToUnix(serializer);

        assertEquals("" +
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
                "}", result);
    }


    private StringOutputStream streamObject(Object o) {
        StreamingMarshal streamingMarshal = new RdapStreamingMarshalJson();
        StringOutputStream serializer = new StringOutputStream();
        streamingMarshal.open(serializer);
        streamingMarshal.start("");
        streamingMarshal.writeObject(o);
        streamingMarshal.close();
        return serializer;
    }

    private String convertEOLToUnix(StringOutputStream serializer) throws IOException {
        StringOutputStream resultStream = new StringOutputStream();
        LineEnds.convert(new StringInputStream(serializer.toString()), resultStream, LineEnds.STYLE_UNIX);
        return resultStream.toString();
    }

}
