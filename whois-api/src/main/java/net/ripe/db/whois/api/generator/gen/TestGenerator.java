package net.ripe.db.whois.api.generator.gen;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import nl.grol.whois.data.model.CommentedValue;
import nl.grol.whois.data.model.Inetnum;
import nl.grol.whois.data.model.Mntner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;


public class TestGenerator {

    public static void main(String[] args) throws Exception {

        TestGenerator testGenerator = new TestGenerator();

        //testGenerator.constructMntnerFromOkXmlResponse();

        testGenerator.constructMntnerFromErrorXmlResponse();

        //testGenerator.constructNewToXmlRequest();

        //testGenerator.buildError();
    }

    public void constructMntnerFromOkXmlResponse() throws Exception {
        List errorMessages = Lists.newArrayList();
        Mntner.Builder builder = Mntner.Builder.fromResponse(fromXml(XML_MNTNER_RESP));
        builder.optionalAddAbuseMailbox("mgrol@ripe.net");
        Mntner mntner = builder.build();
        System.err.println(mntner.toString());
        System.err.println(toXml(mntner.toRequest()));
    }

    public void constructMntnerFromErrorXmlResponse() throws Exception {
        Mntner.Builder builder = Mntner.Builder.fromResponse(fromXml(XML_ERROR_RESP));
        builder.optionalAddAbuseMailbox("mgrol@ripe.net");
        Mntner mntner = builder.build();
        System.err.println(mntner.toString());
        System.err.println(toXml(mntner.toRequest()));
    }

    public void constructNewToXmlRequest() throws Exception {
        Inetnum builder = new Inetnum.Builder()
                .mandatorySetInetnum("10.20.30.0 - 10.20.30.255")
                .mandatorySetNetname("my net")
                .mandatoryAddDescr("my descr")
                .mandatoryAddDescr("my descr 2")
                .mandatoryAddCountry("nl")
                .mandatoryAddAdminCRef("GROL-RIPE")
                .mandatoryAddTechCRef("GROL-RIPE")
                .mandatorySetStatus("ASSIGNED", "my first comment")
                .mandatoryAddMntByRef("GROL-MNT")
                .mandatoryAddChanged("mgrol@ripe.net 20150423")
                .mandatorySetSource("RIPE")
                .optionalSetOrgRef("MY-ORG")
                .build();
        System.err.print(builder.toString());

        System.err.println(toXml(builder.toRequest()));
    }

    public void buildError() {

        // next line will break: due to missing mandatory fields
        try {
            new Inetnum.Builder().mandatorySetInetnum("10.20.30.0 - 10.20.30.255").build();
        } catch (Exception exc) {
            System.err.print("Error building inetnum:" + exc.toString());
        }

    }


    private WhoisResources fromXml(final String xmlString) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(WhoisResources.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        InputStream inputStream = new ByteArrayInputStream(xmlString.getBytes(StandardCharsets.UTF_8));
        WhoisResources whoisResources = (WhoisResources) unmarshaller.unmarshal(inputStream);
        return whoisResources;
    }

    private String toXml(WhoisResources whoisResources) throws Exception {
        JAXBContext jc = JAXBContext.newInstance(WhoisResources.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        InputStream stream = new ByteArrayInputStream(XML_MNTNER_RESP.getBytes(StandardCharsets.UTF_8));
        StringWriter writer = new StringWriter();
        marshaller.marshal(whoisResources, writer);
        return writer.toString();
    }

    private static String JSON_MNTNER_RESPONSE = "" +
            "{\"objects\":{\"object\":[ {\n" +
            "  \"type\" : \"mntner\",\n" +
            "  \"link\" : {\n" +
            "    \"type\" : \"locator\",\n" +
            "    \"href\" : \"http://rest-prepdev.db.ripe.net/ripe/mntner/AARDVARK-MNT\"\n" +
            "  },\n" +
            "  \"source\" : {\n" +
            "    \"id\" : \"ripe\"\n" +
            "  },\n" +
            "  \"primary-key\" : {\n" +
            "    \"attribute\" : [ {\n" +
            "      \"name\" : \"mntner\",\n" +
            "      \"value\" : \"AARDVARK-MNT\"\n" +
            "    } ]\n" +
            "  },\n" +
            "  \"attributes\" : {\n" +
            "    \"attribute\" : [ {\n" +
            "      \"name\" : \"mntner\",\n" +
            "      \"value\" : \"AARDVARK-MNT\"\n" +
            "    }, {\n" +
            "      \"name\" : \"auth\",\n" +
            "      \"value\" : \"MD5-PW\",\n" +
            "      \"comment\" : \"Filtered\"\n" +
            "    }, {\n" +
            "      \"name\" : \"descr\",\n" +
            "      \"value\" : \"Mntner for denis' objects\"\n" +
            "    }, {\n" +
            "      \"link\" : {\n" +
            "        \"type\" : \"locator\",\n" +
            "        \"href\" : \"http://rest-prepdev.db.ripe.net/ripe/person/DW6465-RIPE\"\n" +
            "      },\n" +
            "      \"name\" : \"admin-c\",\n" +
            "      \"value\" : \"DW6465-RIPE\",\n" +
            "      \"referenced-type\" : \"person\"\n" +
            "    }, {\n" +
            "      \"link\" : {\n" +
            "        \"type\" : \"locator\",\n" +
            "        \"href\" : \"http://rest-prepdev.db.ripe.net/ripe/person/DW-RIPE\"\n" +
            "      },\n" +
            "      \"name\" : \"tech-c\",\n" +
            "      \"value\" : \"DW-RIPE\",\n" +
            "      \"referenced-type\" : \"person\"\n" +
            "    }, {\n" +
            "      \"link\" : {\n" +
            "        \"type\" : \"locator\",\n" +
            "        \"href\" : \"http://rest-prepdev.db.ripe.net/ripe/key-cert/X509-1\"\n" +
            "      },\n" +
            "      \"name\" : \"auth\",\n" +
            "      \"value\" : \"X509-1\",\n" +
            "      \"referenced-type\" : \"key-cert\"\n" +
            "    }, {\n" +
            "      \"link\" : {\n" +
            "        \"type\" : \"locator\",\n" +
            "        \"href\" : \"http://rest-prepdev.db.ripe.net/ripe/key-cert/X509-1689\"\n" +
            "      },\n" +
            "      \"name\" : \"auth\",\n" +
            "      \"value\" : \"X509-1689\",\n" +
            "      \"referenced-type\" : \"key-cert\"\n" +
            "    }, {\n" +
            "      \"link\" : {\n" +
            "        \"type\" : \"locator\",\n" +
            "        \"href\" : \"http://rest-prepdev.db.ripe.net/ripe/mntner/AARDVARK-MNT\"\n" +
            "      },\n" +
            "      \"name\" : \"mnt-by\",\n" +
            "      \"value\" : \"AARDVARK-MNT\",\n" +
            "      \"referenced-type\" : \"mntner\"\n" +
            "    }, {\n" +
            "      \"link\" : {\n" +
            "        \"type\" : \"locator\",\n" +
            "        \"href\" : \"http://rest-prepdev.db.ripe.net/ripe/mntner/AARDVARK-MNT\"\n" +
            "      },\n" +
            "      \"name\" : \"referral-by\",\n" +
            "      \"value\" : \"AARDVARK-MNT\",\n" +
            "      \"referenced-type\" : \"mntner\"\n" +
            "    }, {\n" +
            "      \"name\" : \"created\",\n" +
            "      \"value\" : \"2004-02-25T16:52:00Z\"\n" +
            "    }, {\n" +
            "      \"name\" : \"last-modified\",\n" +
            "      \"value\" : \"2014-09-26T08:59:38Z\"\n" +
            "    }, {\n" +
            "      \"name\" : \"source\",\n" +
            "      \"value\" : \"RIPE\",\n" +
            "      \"comment\" : \"Filtered\"\n" +
            "    } ]\n" +
            "  }\n" +
            "} ]\n" +
            "},\n" +
            "\"terms-and-conditions\" : {\n" +
            "\"type\" : \"locator\",\n" +
            "\"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
            "}\n" +
            "}";

    private static final String XML_MNTNER_RESP = "" +
            "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
            "<objects>\n" +
            "<object type=\"mntner\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/mntner/AARDVARK-MNT\"/>\n" +
            "<source id=\"ripe\"/>\n" +
            "<primary-key>\n" +
            "<attribute name=\"mntner\" value=\"AARDVARK-MNT\"/>\n" +
            "</primary-key>\n" +
            "<attributes>\n" +
            "<attribute name=\"mntner\" value=\"AARDVARK-MNT\"/>\n" +
            "<attribute name=\"auth\" value=\"MD5-PW\" comment=\"Filtered\"/>\n" +
            "<attribute name=\"upd-to\" value=\"mgrol@ripe.net\" />\n" +
            "<attribute name=\"descr\" value=\"Mntner for denis' objects\"/>\n" +
            "<attribute name=\"descr\" value=\"Another description for denis' mntner\"/>\n" +
            "<attribute name=\"admin-c\" value=\"DW6465-RIPE\" referenced-type=\"person\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/person/DW6465-RIPE\"/>\n" +
            "</attribute>\n" +
            "<attribute name=\"tech-c\" value=\"DW-RIPE\" referenced-type=\"person\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/person/DW-RIPE\"/>\n" +
            "</attribute>\n" +
            "<attribute name=\"auth\" value=\"X509-1\" referenced-type=\"key-cert\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/key-cert/X509-1\"/>\n" +
            "</attribute>\n" +
            "<attribute name=\"auth\" value=\"X509-1689\" referenced-type=\"key-cert\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/key-cert/X509-1689\"/>\n" +
            "</attribute>\n" +
            "<attribute name=\"mnt-by\" value=\"AARDVARK-MNT\" referenced-type=\"mntner\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/mntner/AARDVARK-MNT\"/>\n" +
            "</attribute>\n" +
            "<attribute name=\"referral-by\" value=\"AARDVARK-MNT\" referenced-type=\"mntner\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/mntner/AARDVARK-MNT\"/>\n" +
            "</attribute>\n" +
            "<attribute name=\"changed\" value=\"mgrol@ripe.net 20040225\"/>\n" +
            "<attribute name=\"created\" value=\"2004-02-25T16:52:00Z\"/>\n" +
            "<attribute name=\"last-modified\" value=\"2014-09-26T08:59:38Z\"/>\n" +
            "<attribute name=\"source\" value=\"RIPE\" comment=\"Filtered\"/>\n" +
            "</attributes>\n" +
            "</object>\n" +
            "</objects>\n" +
            "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>\n" +
            "</whois-resources>";


    private static final String XML_ERROR_RESP = "" +
            "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
            "<link xlink:type=\"locator\" xlink:href=\"http://rest-prepdev.db.ripe.net/ripe/mntner/aardvark-mntt\"/>\n" +
            "<errormessages>\n" +
            "<errormessage severity=\"Error\" text=\"ERROR:101: no entries found No entries found in source %s. \">\n" +
            "<args value=\"RIPE\"/>\n" +
            "</errormessage>\n" +
            "</errormessages>\n" +
            "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>\n" +
            "</whois-resources>";

}
