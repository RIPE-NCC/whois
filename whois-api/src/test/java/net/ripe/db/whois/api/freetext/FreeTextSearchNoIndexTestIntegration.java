package net.ripe.db.whois.api.freetext;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.InternalServerErrorException;

import static org.junit.Assert.fail;

public class FreeTextSearchNoIndexTestIntegration extends AbstractIntegrationTest {

    @BeforeClass
    public static void clearProperty() {
        System.setProperty("dir.freetext.index", "");
    }

    @Test
    public void search() throws Exception {
        try {
            query("q=test");
            fail();
        } catch (InternalServerErrorException e) {
            // expected
        }
    }

    private String query(final String queryString) {
        return RestTest.target(getPort(), "search?" + queryString)
                .request()
                .get(String.class);
    }
}
