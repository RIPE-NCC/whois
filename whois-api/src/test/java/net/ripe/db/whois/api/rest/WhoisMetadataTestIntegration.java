package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ClientErrorException;
import java.net.HttpURLConnection;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class WhoisMetadataTestIntegration extends AbstractIntegrationTest {

    @Test
    public void getTemplateXml() throws Exception {
        final String s = doGetRequest("whois/metadata/templates/peering-set.xml", HttpURLConnection.HTTP_OK);
        assertThat("has service", s, containsString("<service name=\"getObjectTemplate\"/>"));
        assertThat("has key", s, containsString("<attribute name=\"peering-set\" requirement=\"MANDATORY\" cardinality=\"SINGLE\" keys=\"PRIMARY_KEY LOOKUP_KEY\"/>"));
        assertThat("has org", s, containsString("<attribute name=\"org\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>"));
        assertThat("has source", s, containsString("<source id=\"ripe\"/>"));
        assertThat("has template", s, containsString("<template type=\"peering-set\">"));
    }

    @Test
    public void getTemplateJson() throws Exception {
        final String s = doGetRequest("whois/metadata/templates/peering-set.json", HttpURLConnection.HTTP_OK);
        assertThat(s, containsString("\"name\" : \"getObjectTemplate\""));
        assertThat(s, containsString("/metadata/templates/peering-set"));
        assertThat(s, containsString("\"type\" : \"peering-set\""));
        assertThat(s, containsString("\"name\" : \"peering-set\""));
        assertThat(s, containsString("\"requirement\" : \"MANDATORY\""));
        assertThat(s, containsString("\"cardinality\" : \"SINGLE\""));
        assertThat(s, containsString("\"keys\" : [ \"PRIMARY_KEY\", \"LOOKUP_KEY\" ]"));
        assertThat(s, not(containsString("template-resources")));
    }

    @Test
    public void getTemplateDefaultsToXml() throws Exception {
        final String s1 = doGetRequest("whois/metadata/templates/peering-set.xml", HttpURLConnection.HTTP_OK);
        final String s2 = doGetRequest("whois/metadata/templates/peering-set", HttpURLConnection.HTTP_OK);
        assertThat(s1, is(s2));
    }

    @Test
    public void wrongTypeReturns404() throws Exception {
        doGetRequest("whois/metadata/templates/jedi-master", HttpURLConnection.HTTP_NOT_FOUND);
        doGetRequest("whois/metadata/templates/jedi-master.xml", HttpURLConnection.HTTP_NOT_FOUND);
        doGetRequest("whois/metadata/templates/jedi-master.json", HttpURLConnection.HTTP_NOT_FOUND);
        doGetRequest("whois/metadata/templates/peering-set.jedi", HttpURLConnection.HTTP_NOT_FOUND);
        doGetRequest("whois/metadata/templates/jedi.jedi", HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void getSourcesXml() throws Exception {
        final String response = doGetRequest("whois/metadata/sources.xml", HttpURLConnection.HTTP_OK);
        assertThat(response, endsWith("<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\"><link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/metadata/sources\"/><service name=\"getSupportedDataSources\"/><sources><source id=\"test\"/><source id=\"test-grs\"/></sources></whois-resources>"));
    }

    @Test
    public void getSourcesJson() throws Exception {
        final String response = doGetRequest("whois/metadata/sources.json", HttpURLConnection.HTTP_OK);
        assertThat(response, is("{\n" +
                "  \"link\" : {\n" +
                "    \"type\" : \"locator\",\n" +
                "    \"href\" : \"http://rest.db.ripe.net/metadata/sources\"\n" +
                "  },\n" +
                "  \"service\" : {\n" +
                "    \"name\" : \"getSupportedDataSources\"\n" +
                "  },\n" +
                "  \"sources\" : {\n" +
                "    \"source\" : [ {\n" +
                "      \"id\" : \"test\"\n" +
                "    }, {\n" +
                "      \"id\" : \"test-grs\"\n" +
                "    } ]\n" +
                "  }\n" +
                "}"));
    }

    private String doGetRequest(final String url, final int httpStatus) {
        try {
            final String response = RestTest.target(getPort(), url).request().get(String.class);
            assertThat(httpStatus, is(HttpURLConnection.HTTP_OK));
            return response;
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(httpStatus));
            return e.getResponse().readEntity(String.class);
        }
    }
}
