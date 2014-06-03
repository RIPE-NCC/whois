package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;


@Category(IntegrationTest.class)
public class VersionListServiceTestIntegration extends AbstractInternalTest {

    @Before
    public void setup() {
        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");
    }

    @Test
    public void versionsReturnSomethingAtAll() {
        try {
            RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        } catch (ClientErrorException e) {
            final Response response = e.getResponse();
            assertThat(response.getStatus(), is(404));
            assertThat(response.readEntity(String.class), containsString("ERROR:101: no entries found"));
        }
    }
}