package net.ripe.db.whois.api.whois;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.HttpURLConnection;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class WhoisMetadataTestIntegration extends AbstractIntegrationTest {

    @Test
    public void getTemplate() throws Exception {
        final String s = doGetRequest(getUrl("templates/peering-set.xml"), HttpURLConnection.HTTP_OK);
        assertThat("has service", s, containsString("service=\"getObjectTemplate\""));
        assertThat("has key", s, containsString("<attribute name=\"peering-set\" requirement=\"MANDATORY\" cardinality=\"SINGLE\" keys=\"PRIMARY_KEY LOOKUP_KEY\"/>"));
        assertThat("has org", s, containsString("<attribute name=\"org\" requirement=\"OPTIONAL\" cardinality=\"MULTIPLE\" keys=\"INVERSE_KEY\"/>"));
        assertThat("has source", s, containsString("<source id=\"ripe\"/>"));
        assertThat("has template", s, containsString("<template type=\"peering-set\">"));
    }

    @Test
    public void getTemplateDefaultsToXml() throws Exception {
        final String s1 = doGetRequest(getUrl("templates/peering-set.xml"), HttpURLConnection.HTTP_OK);
        final String s2 = doGetRequest(getUrl("templates/peering-set"), HttpURLConnection.HTTP_OK);
        assertThat(s1, is(s2));
    }

    @Test
    public void wrongTypeReturns404() throws Exception {
        doGetRequest(getUrl("templates/jedi-master"), HttpURLConnection.HTTP_NOT_FOUND);
        doGetRequest(getUrl("templates/jedi-master.xml"), HttpURLConnection.HTTP_NOT_FOUND);
        doGetRequest(getUrl("templates/jedi-master.json"), HttpURLConnection.HTTP_NOT_FOUND);

        doGetRequest(getUrl("templates/peering-set.jedi"), HttpURLConnection.HTTP_NOT_FOUND);
        doGetRequest(getUrl("templates/jedi.jedi"), HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void getSource() throws Exception {
        final String s = doGetRequest(getUrl("sources.xml"), HttpURLConnection.HTTP_OK);
        System.err.println(s);
        assertThat("has service", s, containsString("service=\"getSupportedDataSources\""));
        assertThat("has source", s, containsString("<source name=\"RIPE\" id=\"ripe\"/>"));
        assertThat("has grs-source", s, containsString("<source name=\"TEST-GRS\" id=\"test-grs\" grs-id=\"test-grs\"/>"));
        assertThat("has sources", s, containsString("<sources>"));
        assertThat("has grs-sources", s, containsString("<grs-sources>"));
    }

    private String getUrl(final String command) {
        return "http://localhost:" + getPort(Audience.PUBLIC) + "/whois/metadata/" + command;
    }

}
