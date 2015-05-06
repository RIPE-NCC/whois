package net.ripe.db.whois.api.autocomplete;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AutocompleteTestIntegration extends AbstractIntegrationTest {
    @Autowired FreeTextIndex freeTextIndex;

    @Autowired AutocompleteService autocompleteService;

    @BeforeClass
    public static void setProperty() {
        // We only enable freetext indexing here, so it doesn't slow down the rest of the test suite
        System.setProperty("dir.freetext.index", "var${jvmId:}/idx");
    }

    @AfterClass
    public static void clearProperty() {
        System.clearProperty("dir.freetext.index");
    }

    @Before
    public void setUp() throws Exception {
        databaseHelper.addObject("mntner: AA1-MNT");
        databaseHelper.addObject("mntner: AB1-MNT");
        databaseHelper.addObject("mntner: AC1-MNT");
        rebuildIndex();
    }

    @Test
    public void autocomplete() {
        assertThat(query("test"), containsString("testing"));
    }

    // helper methods

    private String query(final String queryString) {
        return RestTest.target(getPort(), String.format("autocomplete?%s", queryString))
                .request()
                .get(String.class);
    }

    private void rebuildIndex() {
        freeTextIndex.rebuild();
    }
}
