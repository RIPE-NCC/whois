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

import javax.ws.rs.BadRequestException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

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
        databaseHelper.addObject("mntner: something-mnt");
        rebuildIndex();
    }

    @Test
    public void single_maintainer_found() {
        assertThat(query("AA1-MNT", "mntner", "mntner"), containsString("num found = 1"));
    }

    @Test
    public void match_start_of_word_dash_is_tokenised() {
        assertThat(query("AA1", "mntner", "mntner"), containsString("num found = 1"));
    }

    @Test
    public void match_start_of_word_first_syllable_only() {
        assertThat(query("some", "mntner", "mntner"), containsString("num found = 1"));
    }

    @Test
    public void match_start_of_word_first_syllable_only_case_insensitive() {
        assertThat(query("SoMe", "mntner", "mntner"), containsString("num found = 1"));
    }

    @Test
    public void no_maintainers_found() {
        assertThat(query("invalid", "mntner", "mntner"), containsString("num found = 0"));
    }

    @Test
    public void no_parameters() {
        try {
            RestTest.target(getPort(), "whois/autocomplete").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("query (q) parameter is required, and must be at least 3 characters long"));
        }
    }

    @Test
    public void attribute_type_parameter_missing() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?q=test&ot=mntner").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("attribute type (at) parameter is required"));
        }
    }

    @Test
    public void object_type_parameter_missing() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?q=test&at=mntner").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("object type (ot) parameter is required"));
        }
    }

    @Test
    public void invalid_attribute_type_parameter() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?q=test&at=invalid").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("invalid attribute type: invalid"));
        }
    }

    @Test
    public void invalid_object_type_parameter() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?q=test&ot=invalid").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("invalid object type: invalid"));
        }
    }

    // helper methods

    private String query(final String queryString, final String objectType, final String attributeType) {
        return RestTest.target(getPort(),
                String.format("whois/autocomplete?q=%s&ot=%s&at=%s", queryString, objectType, attributeType))
                .request()
                .get(String.class);
    }

    private void rebuildIndex() {
        freeTextIndex.rebuild();
    }
}
