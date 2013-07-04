package net.ripe.db.whois.api.httpserver;

import net.ripe.db.whois.api.AbstractRestClientTest;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class RdapServerTest extends AbstractRestClientTest {
    private static final Audience AUDIENCE = Audience.PUBLIC;

    @Test
    public void rdapServerTest() throws Exception {
        final URL url = new URL("http://localhost:" + getPort(AUDIENCE) + "/index.html");

        final HttpURLConnection httpConnect = (HttpURLConnection)url.openConnection();
        httpConnect.connect();

        Thread.sleep(150000);

        assertEquals(httpConnect.getResponseCode(), 200);
        httpConnect.disconnect();
    }
}
