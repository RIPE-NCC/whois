package net.ripe.db.whois.api.fulltextsearch;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.experimental.categories.Category;
import org.junit.jupiter.api.Test;

import javax.ws.rs.NotFoundException;

import static org.junit.Assert.fail;

@Category(IntegrationTest.class)
public class FullTextSearchNoIndexTestIntegration extends AbstractIntegrationTest {

    @BeforeAll
    public static void clearProperty() {
        System.setProperty("dir.fulltext.index", "");
    }

    @Test
    public void search() {
        try {
            query("q=test");
            fail();
        } catch (NotFoundException e) {
            // expected
        }
    }

    private String query(final String queryString) {
        return RestTest.target(getPort(), "search?" + queryString)
                .request()
                .get(String.class);
    }
}
