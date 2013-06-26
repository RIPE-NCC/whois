package net.ripe.db.whois.api.whois.rdap;

import com.Ostermiller.util.LineEnds;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.StreamingMarshal;
import net.ripe.db.whois.api.whois.rdap.domain.Nameserver;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RdapResponseJsonTest {

    @Test
    public void entity_vcard_serialization_test() throws Exception {
        VcardObjectHelper.EntityVcardBuilder builder = new VcardObjectHelper.EntityVcardBuilder();

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(1372214924859L);

        builder.setVersion()
                .setFn("Joe User")
                .setN(builder.createNEntryValueType("User", "Joe", "", "", Lists.<String>newArrayList("ing. jr", "M.Sc.")))
                .setBday("--02-03")
                .setAnniversary(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc))
                .setGender("M")
                .setKind("individual")
                .addLang(VcardObjectHelper.createHashMap(Maps.immutableEntry("pref", "1")), "fr")
                .addLang(VcardObjectHelper.createHashMap(Maps.immutableEntry("pref", "2")), "en")
                .setOrg(VcardObjectHelper.createHashMap(Maps.immutableEntry("type", "work")), "Example")
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
                "}, \"date-and-or-time\", \"2013-06-26T02:48:44Z\" ], [ \"gender\", {\n" +
                "}, \"text\", \"M\" ], [ \"kind\", {\n" +
                "}, \"text\", \"individual\" ], [ \"lang\", {\n" +
                "  \"pref\" : \"1\"\n" +
                "}, \"language-tag\", \"fr\" ], [ \"lang\", {\n" +
                "  \"pref\" : \"2\"\n" +
                "}, \"language-tag\", \"en\" ], [ \"org\", {\n" +
                "  \"type\" : \"work\"\n" +
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
                "}, \"uri\", \"http://www.example.com/joe.user/joe.asc\" ], [ \"tz\", {\n" +
                "}, \"utc-offset\", \"-05:00\" ], [ \"key\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"uri\", \"http://example.org\" ] ] ]", result);
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

        Nameserver.Remarks remarks1 = new Nameserver.Remarks();
        remarks1.getDescription().add("She sells sea shells down by the sea shore.");
        remarks1.getDescription().add("Originally written by Terry Sullivan.");
        nameserver.getRemarks().add(remarks1);


        Nameserver.Links link = new Nameserver.Links();
        link.setHref("http://example.net/nameserver/xxxx");
        link.setValue("http://example.net/nameserver/xxxx");
        link.setRel("self");
        nameserver.getLinks().add(link);

        nameserver.setPort43("whois.example.net");

        Nameserver.Events event1 = new Nameserver.Events();
        event1.setEventAction("registration");

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(1372214924859L);

        try {
            event1.setEventDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
        } catch (Exception ex) {

        }
        nameserver.getEvents().add(event1);

        Nameserver.Events event2 = new Nameserver.Events();
        event2.setEventAction("last changed");
        try {
            event2.setEventDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
        } catch (Exception ex) {

        }
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
