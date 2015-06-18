package net.ripe.db.whois.api.autocomplete;

import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.common.IntegrationTest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

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
        assertThat(query("AA1-MNT", "mntner"), is("[ \"AA1-MNT\" ]"));
    }

    @Test
    public void match_start_of_word_dash_is_tokenised() {
        assertThat(query("AA1", "mntner"), is("[ \"AA1-MNT\" ]"));
    }

    @Test
    public void match_start_of_word_first_syllable_only() {
        assertThat(query("some", "mntner"), is("[ \"something-mnt\" ]"));
    }

    @Test
    public void match_start_of_word_first_syllable_only_case_insensitive() {
        assertThat(query("SoMe", "mntner"), is("[ \"something-mnt\" ]"));
    }

    @Test
    public void match_multiple_maintainers() {
        assertThat(query("random", "mntner"), is("[ \"random1-mnt\", \"random2-mnt\" ]"));
    }

    @Test
    public void no_maintainers_found() {
        assertThat(query("invalid", "mntner"), is("[ ]"));
    }

    @Test
    public void no_parameters() {
        try {
            RestTest.target(getPort(), "whois/autocomplete").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("query (q) parameter is required, and must be at least 2 characters long"));
        }
    }

    @Test
    public void query_parameter_too_short() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?q=a&f=mntner").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("query (q) parameter is required, and must be at least 2 characters long"));
        }
    }

    @Test
    public void missing_query_parameter() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?f=mntner").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("query (q) parameter is required, and must be at least 2 characters long"));
        }
    }

    @Test
    public void missing_field_parameter() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?q=test").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("field (f) parameter is required"));
        }
    }

    @Test
    public void mixed_case_matched() {
        databaseHelper.addObject("mntner: MiXEd-MNT");
        rebuildIndex();

        assertThat(query("mIxeD", "mntner"), is("[ \"MiXEd-MNT\" ]"));
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

        // assertThat(query("ABC", "mntner"), is("[ \"ABC3-MNT\", \"ABC4-MNT\", \"ABC5-MNT\", \"ABC6-MNT\", \"ABC7-MNT\", \"ABC8-MNT\", \"ABC9-MNT\", \"ABC10-MNT\", \"ABC11-MNT\", \"ABC0-MNT\" ]"));  // TODO
        // assertThat(query("ABC", "mntner"), not(containsString("ABC10-MNT")));    // TODO
    }

    @Test
    public void field_reference_matched_one_result_case_insensitive() {
        databaseHelper.addObject(
                "person:  Admin1\n" +
                "nic-hdl: AD1-TEST");

        rebuildIndex();

        assertThat(query("ad1", "admin-c"), is("[ \"AD1-TEST\" ]"));
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

        assertThat(query("ww", "admin-c"), is("[ \"ww1-test\", \"ww2-test\" ]"));
    }

    @Ignore("it fails because results are coming in random order")
    @Test
    public void field_references_mntners_matched() {
        databaseHelper.addObject("mntner:  bla1-mnt\n");
        databaseHelper.addObject("mntner:  bla2-mnt\n");
        databaseHelper.addObject("mntner:  bLA3-mnt\n");

        rebuildIndex();

        assertThat(query("bla", "mntner"), is("[ \"bla1-mnt\", \"bla2-mnt\", \"bLA3-mnt\" ]"));
    }

    @Test
    public void getDetails() {
        databaseHelper.addObject(
                "person:  person test\n" +
                        "nic-hdl: ww1-test");
        databaseHelper.addObject(
                "role:  role test\n" +
                        "nic-hdl: ww2-test\n");

        rebuildIndex();

        final String results =

                RestTest.target(getPort(),
                String.format("whois/autocomplete/details?q=%s&f=%s&a=%s&a=%s", "ww", "admin-c", "last-modified", "e-mail"))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        System.out.println("\n\n\nresult = \n" + results+"\n\n");
//        assertThat(results, containsString(
//                "[ {\n" +
//                "  \"primarykey\" : \"ww1-test\",\n" +
//                "  \"objecttype\" : \"person\",\n" +
//                "  \"auths\" : [ \"MD5\", \"SSO\" ],\n" +
//                "  \"descriptions\" : [ \"fake descr\" ]\n" +
//                "}, {\n" +
//                "  \"primarykey\" : \"ww2-test\",\n" +
//                "  \"objecttype\" : \"role\",\n" +
//                "  \"auths\" : [ \"MD5\", \"SSO\" ],\n" +
//                "  \"descriptions\" : [ \"fake descr\" ]\n" +
//                "} ]"));

    }

    @Test
    public void attribute_parameters_not_valid() {
        try {
            RestTest.target(getPort(), "whois/autocomplete/details?q=abc&f=mntner&a=mntner&a=invalidAttr").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("invalid attribute(s) for parameter (a): [invalidAttr]"));
        }
    }


    // helper methods

    private String query(final String query, final String field) {
        return RestTest.target(getPort(),
                String.format("whois/autocomplete?q=%s&f=%s", query, field))
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
    }

    private void rebuildIndex() {
        freeTextIndex.rebuild();
    }
}
