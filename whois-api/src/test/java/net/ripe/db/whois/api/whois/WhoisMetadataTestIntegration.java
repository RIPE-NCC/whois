package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.WebTarget;
import java.net.HttpURLConnection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class WhoisMetadataTestIntegration extends AbstractRestClientTest {

    private static final Audience AUDIENCE = Audience.PUBLIC;

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

    @Ignore("TODO: default is JSON response")
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
        assertThat("has service", response, containsString("<service name=\"getSupportedDataSources\"/>"));
        assertThat("has source", response, containsString("<source name=\"RIPE\" id=\"ripe\"/>"));
        assertThat("has grs-source", response, containsString("<source name=\"TEST-GRS\" id=\"test-grs\" grs-id=\"test-grs\"/>"));
        assertThat("has sources", response, containsString("<sources>"));
        assertThat("has grs-sources", response, containsString("<grs-sources>"));
    }

    @Test
    public void getSourcesJson() throws Exception {
        final String response = doGetRequest("whois/metadata/sources.json", HttpURLConnection.HTTP_OK);
        assertThat(response, containsString("\"name\" : \"getSupportedDataSources\""));
        assertThat(response, containsString("\"name\" : \"RIPE\""));
        assertThat(response, containsString("\"name\" : \"TEST\""));
        assertThat(response, containsString("\"name\" : \"TEST-GRS\""));
        assertThat(response, not(containsString("whois-resources")));
    }

    @Override
    protected WebTarget createResource(final Audience audience, final String path) {
        return client.target(String.format("http://localhost:%d/%s", getPort(audience), path));
    }

    private String doGetRequest(final String url, final int httpStatus) {
        try {
            final String response = createResource(AUDIENCE, url).request().get(String.class);
            assertThat(httpStatus, is(HttpURLConnection.HTTP_OK));
            return response;
        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(httpStatus));
            return e.getResponse().readEntity(String.class);
        }
    }
}
