package net.ripe.db.whois.compare.rest;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class RestExecutorTest {

    @Test
    public void test_compactXmlNaive() throws Exception {
        String compact = RestExecutor.compactXmlNaive(getXml());
        assertThat(compact, not(containsString("\n")));
        assertThat(compact, not(containsString("\t")));
    }

    @Test
    public void test_compactJson() throws Exception {
        String compact = RestExecutor.compactJson(getJson());

        assertThat(compact, not(containsString("\n")));
        assertThat(compact, not(containsString("\t")));
    }

    private String getXml() throws Exception {
        return "" +
                "<?xml version=\"1.0\" ?>\n" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                "  <objects>\n" +
                "    <object xmlns=\"\" type=\"mntner\">\t \t \t \n" +
                "      <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/ripe/mntner/RIPE-DBM-STARTUP-MNT\"></link>\n" +
                "      <source id=\"ripe\"></source>\n" +
                "      <primary-key>\n" +
                "        <attribute name=\"mntner\" value=\"RIPE-DBM-STARTUP-MNT\"></attribute>\n" +
                "      </primary-key>\n" +
                "      <attributes>\n" +
                "        <attribute name=\"mntner\" value=\"RIPE-DBM-STARTUP-MNT\"></attribute>\n" +
                "        <attribute name=\"descr\" value=\"Mntner for creating new person objects.\"></attribute>\n" +
                "        <attribute name=\"admin-c\" value=\"DW-RIPE\" referenced-type=\"person\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/ripe/person/DW-RIPE\"></link>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"tech-c\" value=\"RD132-RIPE\" referenced-type=\"role\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/ripe/role/RD132-RIPE\"></link>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"org\" value=\"ORG-NCC1-RIPE\" referenced-type=\"organisation\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/ripe/organisation/ORG-NCC1-RIPE\"></link>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"auth\" value=\"MD5-PW\" comment=\"Filtered\"></attribute>\n" +
                "        <attribute name=\"auth\" value=\"PGPKEY-1290F9D2\" referenced-type=\"key-cert\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/ripe/key-cert/PGPKEY-1290F9D2\"></link>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"mnt-by\" value=\"RIPE-DBM-STARTUP-MNT\" referenced-type=\"mntner\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/ripe/mntner/RIPE-DBM-STARTUP-MNT\"></link>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"referral-by\" value=\"RIPE-DBM-STARTUP-MNT\" referenced-type=\"mntner\">\n" +
                "          <link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/ripe/mntner/RIPE-DBM-STARTUP-MNT\"></link>\n" +
                "        </attribute>\n" +
                "        <attribute name=\"source\" value=\"RIPE\" comment=\"Filtered\"></attribute>\n" +
                "      </attributes>\n" +
                "    </object>\n" +
                "  </objects>\n" +
                "  <terms-and-conditions xmlns=\"\" xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"></terms-and-conditions>\n" +
                "</whois-resources>";


    }

    private String getJson(){
        return "" +
                "{\n" +
                "   \"terms-and-conditions\": {\t\t\t\t \n" +
                "      \"type\": \"locator\",\n" +
                "      \"href\": \"http://server/db/support/db-terms-conditions.pdf\"\n" +
                "   },\n" +
                "   \"objects\": {\n" +
                "      \"object\": [\n" +
                "         {\n" +
                "            \"source\": {\n" +
                "               \"id\": \"ripe\"\n" +
                "            },\n" +
                "            \"link\": {\n" +
                "               \"type\": \"locator\",\n" +
                "               \"href\": \"http://server/ripe/person/DW-RIPE\"\n" +
                "            },\n" +
                "            \"primary-key\": {\n" +
                "               \"attribute\": [\n" +
                "                  {\n" +
                "                     \"name\": \"nic-hdl\",\n" +
                "                     \"value\": \"DW-RIPE\"\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            \"attributes\": {\n" +
                "               \"attribute\": [\n" +
                "                  {\n" +
                "                     \"name\": \"person\",\n" +
                "                     \"value\": \"Denis Walker\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"address\",\n" +
                "                     \"value\": \"RIPE Network Coordination Centre (NCC)\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"address\",\n" +
                "                     \"value\": \"P.O. Box 10096\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"address\",\n" +
                "                     \"value\": \"1001 EB Amsterdam\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"address\",\n" +
                "                     \"value\": \"The Netherlands\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"phone\",\n" +
                "                     \"value\": \"+31 20 535 4444\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"fax-no\",\n" +
                "                     \"value\": \"+31 20 535 4445\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"nic-hdl\",\n" +
                "                     \"value\": \"DW-RIPE\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"mnt-by\",\n" +
                "                     \"link\": {\n" +
                "                        \"type\": \"locator\",\n" +
                "                        \"href\": \"http://server/ripe/mntner/aardvark-mnt\"\n" +
                "                     },\n" +
                "                     \"value\": \"aardvark-mnt\",\n" +
                "                     \"referenced-type\": \"mntner\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"remarks\"\n" +
                "                  },\n" +
                "                  {\n" +
                "                     \"name\": \"source\",\n" +
                "                     \"value\": \"RIPE\",\n" +
                "                     \"comment\": \"Filtered\"\n" +
                "                  }\n" +
                "               ]\n" +
                "            },\n" +
                "            \"type\": \"person\"\n" +
                "         }\n" +
                "      ]\n" +
                "   }\n" +
                "}";
    }

}
