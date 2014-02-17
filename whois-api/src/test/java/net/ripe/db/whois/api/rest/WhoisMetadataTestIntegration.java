package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ClientErrorException;
import java.net.HttpURLConnection;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class WhoisMetadataTestIntegration extends AbstractIntegrationTest {

    @Test
    public void template_xml() throws Exception {
        final String response = request("whois/metadata/templates/peering-set.xml", HttpURLConnection.HTTP_OK);

        assertThat(response, is(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<template-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                "<link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/metadata/templates/peering-set\"/>" +
                "<service name=\"getObjectTemplate\"/>" +
                "<templates>" +
                "<template type=\"peering-set\">" +
                "<source id=\"ripe\"/>" +
                "<attributes>" +
                "<attribute name=\"peering-set\" requirement=\"MANDATORY\" cardinality=\"SINGLE\" keys=\"PRIMARY_KEY LOOKUP_KEY\"/>" +
                "<attribute name=\"descr\" requirement=\"MANDATORY\" cardinality=\"MULTIPLE\" keys=\"\"/>" +
                "<attribute name=\"peering\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"\"/>" +
                "<attribute name=\"mp-peering\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"\"/>" +
                "<attribute name=\"remarks\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"\"/>" +
                "<attribute name=\"org\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>" +
                "<attribute name=\"tech-c\" requirement=\"MANDATORY\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>" +
                "<attribute name=\"admin-c\" requirement=\"MANDATORY\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>" +
                "<attribute name=\"notify\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>" +
                "<attribute name=\"mnt-by\" requirement=\"MANDATORY\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>" +
                "<attribute name=\"mnt-lower\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>" +
                "<attribute name=\"changed\" requirement=\"MANDATORY\" cardinality=\"MULTIPLE\" keys=\"\"/>" +
                "<attribute name=\"source\" requirement=\"MANDATORY\" cardinality=\"SINGLE\" keys=\"\"/>" +
                "</attributes>" +
                "</template>" +
                "</templates>" +
                "</template-resources>"));
    }

    @Test
    public void template_json() throws Exception {
        final String response = request("whois/metadata/templates/peering-set.json", HttpURLConnection.HTTP_OK);

        assertThat(response, is("{\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest.db.ripe.net/metadata/templates/peering-set\"\n" +
                "  },\n" +
                "  \"service\" : {\n" +
                "    \"name\" : \"getObjectTemplate\"\n" +
                "  },\n" +
                "  \"templates\" : {\n" +
                "    \"template\" : [ {\n" +
                "      \"type\" : \"peering-set\",\n" +
                "      \"source\" : {\n" +
                "        \"id\" : \"ripe\"\n" +
                "      },\n" +
                "      \"attributes\" : {\n" +
                "        \"attribute\" : [ {\n" +
                "          \"name\" : \"peering-set\",\n" +
                "          \"requirement\" : \"MANDATORY\",\n" +
                "          \"cardinality\" : \"SINGLE\",\n" +
                "          \"keys\" : [ \"PRIMARY_KEY\", \"LOOKUP_KEY\" ]\n" +
                "        }, {\n" +
                "          \"name\" : \"descr\",\n" +
                "          \"requirement\" : \"MANDATORY\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ ]\n" +
                "        }, {\n" +
                "          \"name\" : \"peering\",\n" +
                "          \"requirement\" : \"OPTIONAL\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ ]\n" +
                "        }, {\n" +
                "          \"name\" : \"mp-peering\",\n" +
                "          \"requirement\" : \"OPTIONAL\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ ]\n" +
                "        }, {\n" +
                "          \"name\" : \"remarks\",\n" +
                "          \"requirement\" : \"OPTIONAL\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ ]\n" +
                "        }, {\n" +
                "          \"name\" : \"org\",\n" +
                "          \"requirement\" : \"OPTIONAL\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ \"INVERSE_KEY\" ]\n" +
                "        }, {\n" +
                "          \"name\" : \"tech-c\",\n" +
                "          \"requirement\" : \"MANDATORY\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ \"INVERSE_KEY\" ]\n" +
                "        }, {\n" +
                "          \"name\" : \"admin-c\",\n" +
                "          \"requirement\" : \"MANDATORY\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ \"INVERSE_KEY\" ]\n" +
                "        }, {\n" +
                "          \"name\" : \"notify\",\n" +
                "          \"requirement\" : \"OPTIONAL\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ \"INVERSE_KEY\" ]\n" +
                "        }, {\n" +
                "          \"name\" : \"mnt-by\",\n" +
                "          \"requirement\" : \"MANDATORY\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ \"INVERSE_KEY\" ]\n" +
                "        }, {\n" +
                "          \"name\" : \"mnt-lower\",\n" +
                "          \"requirement\" : \"OPTIONAL\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ \"INVERSE_KEY\" ]\n" +
                "        }, {\n" +
                "          \"name\" : \"changed\",\n" +
                "          \"requirement\" : \"MANDATORY\",\n" +
                "          \"cardinality\" : \"MULTIPLE\",\n" +
                "          \"keys\" : [ ]\n" +
                "        }, {\n" +
                "          \"name\" : \"source\",\n" +
                "          \"requirement\" : \"MANDATORY\",\n" +
                "          \"cardinality\" : \"SINGLE\",\n" +
                "          \"keys\" : [ ]\n" +
                "        } ]\n" +
                "      }\n" +
                "    } ]\n" +
                "  }\n" +
                "}"));
    }

    @Test
    public void template_defaults_to_xml() throws Exception {
        assertThat(request("whois/metadata/templates/peering-set.xml", HttpURLConnection.HTTP_OK),
                is(request("whois/metadata/templates/peering-set", HttpURLConnection.HTTP_OK)));
    }

    @Test
    public void wrong_type_returns_not_found_status() throws Exception {
        request("whois/metadata/templates/jedi-master", HttpURLConnection.HTTP_NOT_FOUND);
        request("whois/metadata/templates/jedi-master.xml", HttpURLConnection.HTTP_NOT_FOUND);
        request("whois/metadata/templates/jedi-master.json", HttpURLConnection.HTTP_NOT_FOUND);
        request("whois/metadata/templates/peering-set.jedi", HttpURLConnection.HTTP_NOT_FOUND);
        request("whois/metadata/templates/jedi.jedi", HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void sources_xml() throws Exception {
        final String response = request("whois/metadata/sources.xml", HttpURLConnection.HTTP_OK);

        assertThat(response, is(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                "<link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/metadata/sources\"/>" +
                "<service name=\"getSupportedDataSources\"/>" +
                "<sources>" +
                "<source id=\"ripe\"/>" +
                "<source id=\"test\"/>" +
                "<source id=\"test-grs\"/>" +
                "</sources>" +
                "</whois-resources>"));
    }

    @Test
    public void sources_json() throws Exception {
        final String response = request("whois/metadata/sources.json", HttpURLConnection.HTTP_OK);

        assertThat(response, is(
                "{\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest.db.ripe.net/metadata/sources\"\n" +
                "  },\n" +
                "  \"service\" : {\n" +
                "    \"name\" : \"getSupportedDataSources\"\n" +
                "  },\n" +
                "  \"sources\" : {\n" +
                "    \"source\" : [ {\n" +
                "      \"id\" : \"ripe\"\n" +
                "    }, {\n" +
                "      \"id\" : \"test\"\n" +
                "    }, {\n" +
                "      \"id\" : \"test-grs\"\n" +
                "    } ]\n" +
                "  }\n" +
                "}"));
    }

    // helper methods

    private String request(final String url, final int expectedHttpStatus) {
        try {
            final String response = RestTest.target(getPort(), url).request().get(String.class);
            assertThat(expectedHttpStatus, is(HttpURLConnection.HTTP_OK));
            return response;
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(expectedHttpStatus));
            return e.getResponse().readEntity(String.class);
        }
    }
}
