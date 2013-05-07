package net.ripe.db.whois.api;

import com.jayway.awaitility.Awaitility;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.concurrent.Callable;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class ApiDocsTestIntegration extends AbstractRestClientTest {
    @Test
    public void checkInternalIndex() throws Exception {
        final String index = get(Audience.INTERNAL, "");

        assertThat(index, containsString("<html"));
        assertThat(index, containsString("<title>RIPE WHOIS API</title>"));
        assertThat(index, containsString(">/acl/bans/{prefix}<"));
        assertThat(index, containsString(">/logs/current<"));
        assertThat(index, containsString(">/metadata/sources<"));
        assertThat(index, containsString(">/delete/{source}/{objectType}/{key}<"));
    }

    @Test
    public void checkExternalIndex() throws Exception {
        final String index = get(Audience.PUBLIC, "");

        assertThat(index, containsString("<html"));
        assertThat(index, containsString("<title>RIPE WHOIS API</title>"));
        assertThat(index, not(containsString(">/acl/bans/{prefix}<")));
        assertThat(index, not(containsString(">/logs/current<")));
        assertThat(index, containsString(">/metadata/sources<"));
        assertThat(index, containsString(">/delete/{source}/{objectType}/{key}<"));
    }

    private String get(final Audience audience, final String path) {
        final StringBuffer result = new StringBuffer();
        Awaitility.await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    final String index = createStaticResource(audience, path).get(String.class);
                    result.append(index);
                    return true;
                } catch (Exception e) {
                    return false;
                }
            }
        }, is(true));
        return result.toString();
    }
}
