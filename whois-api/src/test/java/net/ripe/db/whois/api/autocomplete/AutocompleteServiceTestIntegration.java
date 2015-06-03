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
public class AutocompleteServiceTestIntegration extends AbstractIntegrationTest {
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
    public void single_maintainer_found() {
        assertThat(query("AA1-MNT", "mntner"), containsString("num found = 1"));
    }

    @Test
    public void no_maintainers_found() {
        assertThat(query("invalid", "mntner"), containsString("num found = 0"));
    }

    // helper methods

    private String query(final String queryString, final String objectType) {
        return RestTest.target(getPort(), String.format("whois/autocomplete?q=%s&t=%s", queryString, objectType))
                .request()
                .get(String.class);
    }

    private void rebuildIndex() {
        freeTextIndex.rebuild();
    }
}
