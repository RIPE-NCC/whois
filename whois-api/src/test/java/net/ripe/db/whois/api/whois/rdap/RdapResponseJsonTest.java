package net.ripe.db.whois.api.whois.rdap;

import com.Ostermiller.util.LineEnds;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.api.whois.StreamingMarshal;
import net.ripe.db.whois.api.whois.rdap.domain.Nameserver;
import net.ripe.db.whois.api.whois.rdap.domain.vcard.*;
import org.codehaus.plexus.util.StringInputStream;
import org.codehaus.plexus.util.StringOutputStream;
import org.junit.Test;

import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class RdapResponseJsonTest {

    @Test
    public void vcard_serialization_test() throws Exception {

        List<Object> vcardArray = new ArrayList<Object>();
        vcardArray.add("vcard");

        Version version = new Version();
        version.setEntryType("text");
        version.setEntryValue("4.0");
        vcardArray.add(VcardObjectHelper.toObjects(version));

        Fn fn = new Fn();
        fn.setEntryType("text");
        fn.setEntryValue("Joe User");
        vcardArray.add(VcardObjectHelper.toObjects(fn));

        Kind kind = new Kind();
        kind.setEntryType("text");
        kind.setEntryValue("individual");
        vcardArray.add(VcardObjectHelper.toObjects(kind));

        Lang lang1 = new Lang();
        lang1.setKeyValues(Maps.newHashMap());
        lang1.getKeyValues().put("pref","1");
        lang1.setEntryType("language-tag");
        lang1.setEntryValue("fr");
        vcardArray.add(VcardObjectHelper.toObjects(lang1));

        Lang lang2 = new Lang();
        lang2.setKeyValues(Maps.newHashMap());
        lang2.getKeyValues().put("pref","2");
        lang2.setEntryType("language-tag");
        lang2.setEntryValue("en");
        vcardArray.add(VcardObjectHelper.toObjects(lang2));

        Org org = new Org();
        org.setEntryType("text");
        org.setEntryValue("Example");
        vcardArray.add(VcardObjectHelper.toObjects(org));

        Title title = new Title();
        title.setEntryType("text");
        title.setEntryValue("Research Scientist");
        vcardArray.add(VcardObjectHelper.toObjects(title));

        Role role = new Role();
        role.setEntryType("text");
        role.setEntryValue("Project Lead");
        vcardArray.add(VcardObjectHelper.toObjects(role));

        Adr adr = new Adr();
        adr.setKeyValues(Maps.newHashMap());
        adr.getKeyValues().put("type","work");
        adr.setEntryType("text");
        adr.getEntryValue().addAll(Lists.<String>newArrayList("", "Suite 1234", "4321 Rue Somewhere", "Quebec", "QC", "G1V 2M2", "Canada"));
        vcardArray.add(VcardObjectHelper.toObjects(adr));

        Tel tel = new Tel();
        tel.setKeyValues(Maps.newHashMap());
        tel.getKeyValues().put("type",new String[]{"work", "voice"});
        tel.setEntryType("uri");
        tel.setEntryValue("tel:+1-555-555-1234;ext=102");
        vcardArray.add(VcardObjectHelper.toObjects(tel));

        Email email = new Email();
        email.setKeyValues(Maps.newHashMap());
        email.getKeyValues().put("type","work");
        email.setEntryType("text");
        email.setEntryValue("joe.user@example.com");
        vcardArray.add(VcardObjectHelper.toObjects(email));

        StringOutputStream serializer = streamObject(vcardArray);
        String result = convertEOLToUnix(serializer);

        assertEquals("" +
                "[ \"vcard\", [ \"version\", {\n" +
                "}, \"text\", \"4.0\" ], [ \"fn\", {\n" +
                "}, \"text\", \"Joe User\" ], [ \"kind\", {\n" +
                "}, \"text\", \"individual\" ], [ \"lang\", {\n" +
                "  \"pref\" : \"1\"\n" +
                "}, \"language-tag\", \"fr\" ], [ \"lang\", {\n" +
                "  \"pref\" : \"2\"\n" +
                "}, \"language-tag\", \"en\" ], [ \"org\", {\n" +
                "}, \"text\", \"Example\" ], [ \"title\", {\n" +
                "}, \"text\", \"Research Scientist\" ], [ \"role\", {\n" +
                "}, \"text\", \"Project Lead\" ], [ \"adr\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"text\", [ \"\", \"Suite 1234\", \"4321 Rue Somewhere\", \"Quebec\", \"QC\", \"G1V 2M2\", \"Canada\" ] ], [ \"tel\", {\n" +
                "  \"type\" : [ \"work\", \"voice\" ]\n" +
                "}, \"uri\", \"tel:+1-555-555-1234;ext=102\" ], [ \"email\", {\n" +
                "  \"type\" : \"work\"\n" +
                "}, \"text\", \"joe.user@example.com\" ] ]", result);
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
        remarks1.getDescription().add( "Originally written by Terry Sullivan.");
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
