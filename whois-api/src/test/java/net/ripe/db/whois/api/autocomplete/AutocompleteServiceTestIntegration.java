package net.ripe.db.whois.api.autocomplete;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.AbstractIntegrationTest;
import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.freetext.FreeTextIndex;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
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
import java.util.Map;
import java.util.stream.Collectors;

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

    // simple searches (field and value)

    @Test
    public void single_maintainer_found() {
        assertThat(getValues(query("AA1-MNT", "mntner"), "key"), contains("AA1-MNT"));
    }

    @Test
    public void match_start_of_word_dash_is_tokenised() {
        assertThat(getValues(query("AA1", "mntner"), "key"), contains("AA1-MNT"));
    }

    @Test
    public void match_start_of_word_first_syllable_only() {
        assertThat(getValues(query("some", "mntner"), "key"), contains("something-mnt"));
    }

    @Test
    public void match_start_of_word_first_syllable_only_case_insensitive() {
        assertThat(getValues(query("SoMe", "mntner"), "key"), contains("something-mnt"));
    }

    @Test
    public void match_multiple_maintainers() {
        assertThat(getValues(query("random", "mntner"), "key"), containsInAnyOrder("random1-mnt", "random2-mnt"));
    }

    @Test
    public void no_maintainers_found() {
        assertThat(getValues(query("invalid", "mntner"), "key"), is(empty()));
    }

    @Test
    public void no_parameters() {
        try {
            RestTest.target(getPort(), "whois/autocomplete").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("invalid arguments"));
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
            assertThat(e.getResponse().readEntity(String.class), is("invalid arguments"));
        }
    }

    @Test
    public void missing_field_parameter() {
        try {
            RestTest.target(getPort(), "whois/autocomplete?query=test").request().get(String.class);
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("invalid arguments"));
        }
    }

    @Test
    public void mixed_case_matched() {
        databaseHelper.addObject("mntner: MiXEd-MNT");
        rebuildIndex();

        assertThat(getValues(query("mIxeD", "mntner"), "key"), contains("MiXEd-MNT"));
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
        assertThat(getValues(query("ABC", "mntner"), "key"), hasSize(10));
    }

    @Test
    public void field_reference_matched_one_result_case_insensitive() {
        databaseHelper.addObject(
                "person:  Admin1\n" +
                "nic-hdl: AD1-TEST");

        rebuildIndex();

        assertThat(getValues(query("ad1", "admin-c"), "key"), contains("AD1-TEST"));
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

        assertThat(getValues(query("ww", "admin-c"), "key"), containsInAnyOrder("ww1-test", "ww2-test"));
    }

    @Test
    public void field_references_mntners_matched() {
        databaseHelper.addObject("mntner:  bla1-mnt\n");
        databaseHelper.addObject("mntner:  bla2-mnt\n");
        databaseHelper.addObject("mntner:  bLA3-mnt\n");

        rebuildIndex();

        assertThat(getValues(query("bla", "mntner"), "key"), containsInAnyOrder("bla1-mnt", "bla2-mnt", "bLA3-mnt"));
    }

    @Test
    public void key_type_only_no_attributes() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test");

        rebuildIndex();

        assertThat(queryRaw("ww", "admin-c"),
            containsString(
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

        assertThat(queryRaw("ww", "admin-c", "person"),
            containsString(
                "[ {\n" +
                "  \"key\" : \"ww1-test\",\n" +
                "  \"type\" : \"person\",\n" +
                "  \"person\" : \"person test\"\n" +
                "} ]"));
    }

    // search by field and value and specify response attribute(s)

    @Test
    public void key_type_multiple_single_attributes_returned() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test\n" +
                "created: then");
        rebuildIndex();

        final List<Map<String, Object>> response = query("ww", "admin-c", "person", "created");

        assertThat(response.size(), is(1));
        assertThat(response.get(0).size(), is(4));
        assertThat(getValues(response, "key"), contains("ww1-test"));
        assertThat(getValues(response, "type"), contains("person"));
        assertThat(getValues(response, "person"), contains("person test"));
        assertThat(getValues(response, "created"), contains("then"));
    }

    @Test
    public void key_type_one_multiple_attribute_returned() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test\n" +
                "remarks: remarks1\n" +
                "source:  TEST");
        rebuildIndex();

        assertThat(getValues(query("ww", "admin-c", "remarks"), "remarks"), contains("[remarks1]"));
    }

    @Test
    public void key_type_more_multiple_attributes_returned() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: ww1-test\n" +
                "remarks: remarks1\n" +
                "remarks: remarks2");
        rebuildIndex();

        assertThat(getValues(query("ww", "admin-c", "remarks"), "remarks"), contains("[remarks1, remarks2]"));
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

        assertThat(queryRaw("AuTH", "mntner", "auth"),
            containsString(
                "[ {\n" +
                "  \"key\" : \"AUTH-MNT\",\n" +
                "  \"type\" : \"mntner\",\n" +
                "  \"auth\" : [ \"MD5-PW\", \"PGPKEY-XYZ\", \"MD5-PW\", \"SSO\" ]\n" +
                "} ]"));
    }

    @Test
    public void single_attribute_parameter_not_valid() {
        try {
            query("abc", "mntner", "invalidAttr");
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Attribute type invalidAttr not found"));
        }
    }

    @Test
    public void multiple_attribute_parameters_not_valid() {
        try {
            query("abc", "mntner", "invalidAttr1", "invalidAttr2");
            fail();
        } catch (BadRequestException e) {
            assertThat(e.getResponse().readEntity(String.class), is("Attribute type invalidAttr1 not found"));
        }
    }

    // complex lookups (specify attributes)

    @Test
    public void select_remarks_from_person() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: pt1-test\n" +
                "remarks: remarks1\n" +
                "source:  TEST");
        rebuildIndex();

        assertThat(
            getValues(
                query(
                    Lists.newArrayList(AttributeType.REMARKS),
                    Lists.newArrayList(ObjectType.PERSON),
                    Lists.newArrayList(AttributeType.NIC_HDL),
                    "pt"),
                "key"),
            contains("pt1-test"));
    }

    @Test
    public void select_remarks_from_person_and_role() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: pt1-test\n" +
                "remarks: remarks1\n" +
                "source:  TEST");
        databaseHelper.addObject(
                "role:    person test\n" +
                "nic-hdl: pt2-test\n" +
                "remarks: remarks2\n" +
                "source:  TEST");
        rebuildIndex();

        assertThat(
            getValues(
                query(
                    Lists.newArrayList(AttributeType.REMARKS),
                    Lists.newArrayList(ObjectType.PERSON, ObjectType.ROLE),
                    Lists.newArrayList(AttributeType.NIC_HDL),
                    "pt"),
                "key"),
            containsInAnyOrder("pt1-test", "pt2-test"));
    }

    @Test
    public void select_remarks_from_person_not_role() {
        databaseHelper.addObject(
                "person:  person test\n" +
                "nic-hdl: pt1-test\n" +
                "remarks: remarks1\n" +
                "source:  TEST");
        databaseHelper.addObject(
                "role:    person test\n" +
                "nic-hdl: pt2-test\n" +
                "remarks: remarks2\n" +
                "source:  TEST");
        rebuildIndex();

        assertThat(
            getValues(
                query(
                    Lists.newArrayList(AttributeType.REMARKS),
                    Lists.newArrayList(ObjectType.PERSON),
                    Lists.newArrayList(AttributeType.NIC_HDL),
                    "pt"),
                "key"),
            contains("pt1-test"));
    }

    @Test
    public void select_abuse_mailbox_from_role_where_nic_hdl_or_abuse_mailbox() {
        databaseHelper.addObject(
                "role:          test role\n" +
                "nic-hdl:       tr1-test\n" +
                "source:        TEST");
        databaseHelper.addObject(
                "role:          test role\n" +
                "nic-hdl:       tr2-test\n" +
                "abuse-mailbox: tr1@host.org\n" +
                "source:  TEST");
        rebuildIndex();

        final List<Map<String, Object>> response =
                query(
                    Lists.newArrayList(AttributeType.ABUSE_MAILBOX),
                    Lists.newArrayList(ObjectType.ROLE),
                    Lists.newArrayList(AttributeType.NIC_HDL, AttributeType.ABUSE_MAILBOX),
                    "tr1");

        assertThat(getValues(response, "key"), containsInAnyOrder("tr1-test", "tr2-test"));
        assertThat(getValues(response, "abuse-mailbox"), containsInAnyOrder(null, "tr1@host.org"));
    }


    // helper methods

    private List<Map<String, Object>> query(final String query, final String field, final String... attributes) {
        return RestTest
            .target(getPort(), String.format("whois/autocomplete?query=%s&field=%s%s", query, field, join("attribute", attributes)))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(new GenericType<List<Map<String, Object>>>(){});
    }

    private String queryRaw(final String query, final String field, final String... attributes) {
        return RestTest
            .target(getPort(), String.format("whois/autocomplete?query=%s&field=%s%s", query, field, join("attribute", attributes)))
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(String.class);
    }

    private List<Map<String, Object>> query(final List<AttributeType> select, final List<ObjectType> from, final List<AttributeType> where, final String like) {
        return RestTest
                .target(getPort(),
                    "whois/autocomplete?" +
                            (!select.isEmpty() ? join("select", select.stream().map(attributeType -> attributeType.getName()).collect(Collectors.toList())) : "") +
                            (!from.isEmpty()   ? join("from", from.stream().map(objectType -> objectType.getName()).collect(Collectors.toList())) : "") +
                            (!where.isEmpty()  ? join("where", where.stream().map(attributeType -> attributeType.getName()).collect(Collectors.toList())) : "") +
                            "&like=" + like)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .get(new GenericType<List<Map<String, Object>>>(){});
    }

    private List<String> getValues(final List<Map<String, Object>> map, final String key) {
        return map.stream().map(entry -> (entry.get(key) != null) ? entry.get(key).toString() : null).collect(Collectors.toList());
    }

    private String join(final String queryParam, final String ... values) {
        return join(queryParam, Lists.newArrayList(values));
    }

    private String join(final String queryParam, final List<String> values) {
        final StringBuilder builder = new StringBuilder();

        for (String value : values) {
            builder.append('&').append(queryParam).append('=').append(value);
        }

        return builder.toString();
    }


    private void rebuildIndex() {
        freeTextIndex.rebuild();
    }
}
