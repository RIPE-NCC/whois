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
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
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
        databaseHelper.addObject("mntner: random1-mnt");
        databaseHelper.addObject("mntner: random2-mnt");
        rebuildIndex();
    }

    @Test
    public void single_maintainer_found() {
        assertThat(query("AA1-MNT", "mntner"), contains("AA1-MNT"));
    }

    @Test
    public void match_start_of_word_dash_is_tokenised() {
        assertThat(query("AA1", "mntner"), contains("AA1-MNT"));
    }

    @Test
    public void match_start_of_word_first_syllable_only() {
        assertThat(query("some", "mntner"), contains("something-mnt"));
    }

    @Test
    public void match_start_of_word_first_syllable_only_case_insensitive() {
        assertThat(query("SoMe", "mntner"), contains("something-mnt"));
    }

    @Test
    public void match_multiple_maintainers() {
        assertThat(query("random", "mntner"), containsInAnyOrder("random1-mnt", "random2-mnt"));
    }

    @Test
    public void no_maintainers_found() {
        assertThat(query("invalid", "mntner"), is(empty()));
    }

    @Test
    public void no_parameters() {
        try {
            RestTest.target(getPort(), "whois/autocomplete").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("query parameter is required, and must be at least 2 characters long"));
        }
    }

    @Test
    public void query_parameter_too_short() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?query=a&field=mntner").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("query parameter is required, and must be at least 2 characters long"));
        }
    }

    @Test
    public void missing_query_parameter() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?field=mntner").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("query parameter is required, and must be at least 2 characters long"));
        }
    }

    @Test
    public void missing_field_parameter() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?query=test").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("field parameter is required"));
        }
    }

    @Test
    public void mixed_case_matched() {
        databaseHelper.addObject("mntner: MiXEd-MNT");
        rebuildIndex();

        assertThat(query("mIxeD", "mntner"), contains("MiXEd-MNT"));
    }


    @Test
    public void query_returns_maximum_results_and_sorted() {
        databaseHelper.addObject("mntner: ABC0-MNT");
        databaseHelper.addObject("mntner: ABC1-MNT");
        databaseHelper.addObject("mntner: ABC2-MNT");
        databaseHelper.addObject("mntner: ABC3-MNT");
        databaseHelper.addObject("mntner: ABC4-MNT");
        databaseHelper.addObject("mntner: ABC5-MNT");
        databaseHelper.addObject("mntner: ABC6-MNT");
        databaseHelper.addObject("mntner: ABC7-MNT");
        databaseHelper.addObject("mntner: ABC8-MNT");
        databaseHelper.addObject("mntner: ABC9-MNT");
        databaseHelper.addObject("mntner: ABC10-MNT");
        rebuildIndex();

        // TODO: [ES] search results are NOT sorted
        assertThat(query("ABC", "mntner"), hasSize(10));
    }

    @Test
    public void field_reference_matched_one_result_case_insensitive() {
        databaseHelper.addObject(
                "person:  Admin1\n" +
                "nic-hdl: AD1-TEST");

        rebuildIndex();

        assertThat(query("ad1", "admin-c"), contains("AD1-TEST"));
    }

    @Test
    public void field_references_matched() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test");
        databaseHelper.addObject(
                "role:  role test\n" +
                "nic-hdl: ww2-test\n");

        rebuildIndex();

        assertThat(query("ww", "admin-c"), containsInAnyOrder("ww1-test", "ww2-test"));
    }

    @Test
    public void field_references_mntners_matched() {
        databaseHelper.addObject("mntner:  bla1-mnt\n");
        databaseHelper.addObject("mntner:  bla2-mnt\n");
        databaseHelper.addObject("mntner:  bLA3-mnt\n");

        rebuildIndex();

        assertThat(query("bla", "mntner"), containsInAnyOrder("bla1-mnt", "bla2-mnt", "bLA3-mnt"));
    }


    //extended
    @Test
    public void key_type_only() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test");

        rebuildIndex();

        final String results = queryExtended("ww", "admin-c");

        assertThat(results, containsString("" +
                "[ {\n" +
                "  \"key\" : \"ww1-test\",\n" +
                "  \"type\" : \"person\"\n" +
                "} ]"));
    }

    @Test
    public void key_type_one_attribute_returned() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test");
        rebuildIndex();

        final String results = queryExtended("ww", "admin-c", "person");

        assertThat(results, containsString("" +
                "[ {\n" +
                "  \"key\" : \"ww1-test\",\n" +
                "  \"type\" : \"person\",\n" +
                "  \"person\" : \"person test\"\n" +
                "} ]"));
    }

    @Test
    public void key_type_multiple_single_attributes_returned() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test\n" +
                "created: then");

        rebuildIndex();

        final String results = queryExtended("ww", "admin-c", "person", "created");

        assertThat(results, containsString("" +
                "[ {\n" +
                "  \"key\" : \"ww1-test\",\n" +
                "  \"type\" : \"person\",\n" +
                "  \"person\" : \"person test\",\n" +
                "  \"created\" : \"then\"\n" +
                "} ]"));
    }

    @Test
    public void key_type_one_multiple_attribute_returned() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test\n" +
                "remarks: remarks1");

        rebuildIndex();

        final String results = queryExtended("ww", "admin-c", "remarks");

        assertThat(results, containsString("" +
                "[ {\n" +
                "  \"key\" : \"ww1-test\",\n" +
                "  \"type\" : \"person\",\n" +
                "  \"remarks\" : [ \"remarks1\" ]\n" +
                "} ]"));
    }

    @Test
    public void key_type_more_multiple_attributes_returned() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test\n" +
                "remarks: remarks1\n" +
                "remarks: remarks2");

        rebuildIndex();

        final String results = queryExtended("ww", "admin-c", "remarks");

        assertThat(results, containsString("" +
                "[ {\n" +
                "  \"key\" : \"ww1-test\",\n" +
                "  \"type\" : \"person\",\n" +
                "  \"remarks\" : [ \"remarks1\", \"remarks2\" ]\n" +
                "} ]"));
    }

    @Test
    public void key_type_auth_attributes_returned() {
        databaseHelper.addObject(
                "mntner:  AUTH-MNT\n" +
                "auth: MD5-PW bla\n" +
                "auth: PGPKEY-XYZ\n" +
                "auth: MD5-PW bue\n" +
                "auth: SSO UUID-123");

        rebuildIndex();

        final String results = queryExtended("AuTH", "mntner", "auth");

        assertThat(results, containsString("" +
                "[ {\n" +
                "  \"key\" : \"AUTH-MNT\",\n" +
                "  \"type\" : \"mntner\",\n" +
                "  \"auth\" : [ \"MD5-PW\", \"PGPKEY-XYZ\", \"MD5-PW\", \"SSO\" ]\n" +
                "} ]"));
    }

    @Test
    public void attribute_parameters_not_valid() {
        try {
            queryExtended("abc", "mntner", "invalidAttr");
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("invalid name for attribute(s) : [invalidAttr]"));
        }
    }


    // helper methods

    private String queryExtended(final String query, final String field, final String... attributeNames) {

        final StringBuffer attrParams = new StringBuffer("");
        for (String attributeName : attributeNames) {
            attrParams.append("&attribute=").append(attributeName);
        }

        return RestTest.target(getPort(), String.format("whois/autocomplete?extended&query=%s&field=%s%s", query, field, attrParams.toString())).request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
    }

    private List<String> query(final String query, final String field) {
        return RestTest
            .target(getPort(), String.format("whois/autocomplete?query=%s&field=%s", query, field))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(new GenericType<List<String>>(){});

    }

    private void rebuildIndex() {
        freeTextIndex.rebuild();
    }
}
