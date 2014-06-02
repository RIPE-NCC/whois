package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;


@Category(IntegrationTest.class)
public class VersionListServiceTestIntegration extends AbstractInternalTest {

    @Before
    public void setup() {
        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");
    }

    @Test
    public void versionsReturnSomethingAtAll() {
        final String result = RestTest.target(getPort(), "api/rnd/test/ROLE/absc/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(result, notNullValue());
    }
}