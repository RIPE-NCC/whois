package net.ripe.db.whois.api;

import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class ApiDocsTestIntegration extends AbstractRestClientTest {
    @Test
    public void checkInternalIndex() throws Exception {
        final String index = createStaticResource(Audience.INTERNAL, "/").get(String.class);

        assertThat(index, containsString("<html"));
        assertThat(index, containsString("<title>RIPE WHOIS API</title>"));
        assertThat(index, containsString(">/acl/bans/{prefix}<"));
        assertThat(index, containsString(">/logs/current<"));
        assertThat(index, containsString(">/metadata/sources<"));
        assertThat(index, containsString(">/delete/{source}/{objectType}/{key}<"));
    }

    @Test
    public void checkExternalIndex() throws Exception {
        final String index = createStaticResource(Audience.PUBLIC, "/").get(String.class);

        assertThat(index, containsString("<html"));
        assertThat(index, containsString("<title>RIPE WHOIS API</title>"));
        assertThat(index, not(containsString(">/acl/bans/{prefix}<")));
        assertThat(index, not(containsString(">/logs/current<")));
        assertThat(index, containsString(">/metadata/sources<"));
        assertThat(index, containsString(">/delete/{source}/{objectType}/{key}<"));
    }
}
