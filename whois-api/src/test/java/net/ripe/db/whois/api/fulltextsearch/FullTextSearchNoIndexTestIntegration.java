package net.ripe.db.whois.api.fulltextsearch;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;

import org.junit.jupiter.api.BeforeAll;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.ws.rs.NotFoundException;

import static org.junit.jupiter.api.Assertions.fail;

@Tag("IntegrationTest")
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
