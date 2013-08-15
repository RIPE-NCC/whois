package net.ripe.db.whois.api;

import com.google.common.collect.Sets;
import net.ripe.db.whois.api.httpserver.Audience;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class ApiDocsTestIntegration extends AbstractRestClientTest {
    @Test
    public void checkInternalIndex() throws Exception {
        final String index = createStaticResource(Audience.INTERNAL, "api-doc").get(String.class);

        assertThat(index, containsString("<html"));
        assertThat(index, containsString("<title>RIPE WHOIS API</title>"));
        assertThat(index, containsString(">/acl/bans/{prefix}<"));
        assertThat(index, containsString(">/logs/current<"));
        assertThat(index, containsString(">/metadata/sources<"));
        assertThat(index, containsString(">/delete/{objectType}/{key}<"));
    }

    @Test
    public void checkExternalIndex() throws Exception {
        final String index = createStaticResource(Audience.PUBLIC, "api-doc").get(String.class);

        assertThat(index, containsString("<html"));
        assertThat(index, containsString("<title>RIPE WHOIS API</title>"));
        assertThat(index, not(containsString(">/acl/bans/{prefix}<")));
        assertThat(index, not(containsString(">/logs/current<")));
        assertThat(index, containsString(">/metadata/sources<"));
        assertThat(index, containsString(">/delete/{objectType}/{key}<"));
    }

    @Test
    public void checkStaticLinksToGeneratedPages() throws Exception {
        final String index = createStaticResource(Audience.PUBLIC, "api-doc").get(String.class);

        Pattern pattern = Pattern.compile("(?i)<a href=\"(path.+)\">");
        final Matcher matcher = pattern.matcher(index);

        Set<String> urls = Sets.newHashSet();
        while (matcher.find()) {
            urls.add(matcher.group(1));
        }

        for (String url : urls) {
            final String subdoc = createStaticResource(Audience.PUBLIC, "api-doc/" + url).get(String.class);
            assertThat(subdoc, containsString("<a href=\"" + url + "\">"));
        }
    }
}
