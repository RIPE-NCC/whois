package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.internal.AbstractInternalTest;
import net.ripe.db.whois.internal.api.rnd.domain.ObjectVersion;
import net.ripe.db.whois.internal.api.rnd.rest.WhoisInternalResources;
import net.ripe.db.whois.internal.api.rnd.rest.WhoisVersionInternal;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;


@Category(IntegrationTest.class)
public class VersionListRestServiceTestIntegration extends AbstractInternalTest {
    @Autowired
    @Qualifier("whoisReadOnlySlaveDataSource")
    DataSource dataSource;
    @Autowired
    WhoisObjectMapper whoisObjectMapper;
    final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis();

    @Before
    public void setUp() throws Exception {
        testDateTimeProvider.reset();

        databaseHelper.setupWhoisDatabase(new JdbcTemplate(dataSource));
        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");
        JdbcTestUtils.deleteFromTables(whoisTemplate, "object_reference");
        JdbcTestUtils.deleteFromTables(whoisTemplate, "object_version");
    }


    @Test
    public void listVersions_created_updated() {
        DateTime start = new DateTime(2004, 12, 25, 0, 0, 0, 0);
        DateTime end = new DateTime(2005, 1, 1, 0, 0, 0, 0);
        DateTime newEnd = new DateTime(2006, 1, 1, 0, 0, 0, 0);
        objectReferenceDao.createVersion(new ObjectVersion(1l, ObjectType.AUT_NUM, "AS3333", start, end, 1));
        objectReferenceDao.createVersion(new ObjectVersion(1l, ObjectType.AUT_NUM, "AS3333", end, newEnd, 2));

        final WhoisInternalResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisInternalResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersions();
        assertThat(versions, hasSize(2));

        final DateTime fromFirstDateTime = dateTimeFormatter.parseDateTime(versions.get(0).getFrom());
        final DateTime toFirstDateTime = dateTimeFormatter.parseDateTime(versions.get(0).getTo());
        final DateTime fromLastDateTime = dateTimeFormatter.parseDateTime(versions.get(1).getFrom());
        final DateTime toLastDateTime = dateTimeFormatter.parseDateTime(versions.get(1).getTo());

        assertThat(fromFirstDateTime, is(start));
        assertThat(toFirstDateTime, is(end));
        assertThat(fromLastDateTime, is(toFirstDateTime));
        assertThat(toLastDateTime, is(newEnd));
    }

    @Test
    public void listVersions_created_deleted_recreated() {
        DateTime start = new DateTime(2006, 6, 15, 0, 0, 0, 0);
        DateTime end = new DateTime(2006, 6, 20, 0, 0, 0, 0);
        DateTime newStart = new DateTime(2006, 7, 12, 0, 0, 0, 0);
        DateTime newEnd = new DateTime(2006, 8, 30, 0, 0, 0, 0);
        objectReferenceDao.createVersion(new ObjectVersion(1l, ObjectType.AUT_NUM, "AS3335", start, end, 1));
        objectReferenceDao.createVersion(new ObjectVersion(1l, ObjectType.AUT_NUM, "AS3335", newStart, newEnd, 2));

        final WhoisInternalResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3335/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisInternalResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersions();
        assertThat(versions, hasSize(2));

        String baseHref = "http://int.db.ripe.net/api/rnd/test/AUT-NUM/AS3335/";

        final DateTime fromFirstDateTime = dateTimeFormatter.parseDateTime(versions.get(0).getFrom());
        final DateTime toFirstDateTime = dateTimeFormatter.parseDateTime(versions.get(0).getTo());
        final DateTime fromLastDateTime = dateTimeFormatter.parseDateTime(versions.get(1).getFrom());
        final DateTime toLastDateTime = dateTimeFormatter.parseDateTime(versions.get(1).getTo());

        assertThat(versions.get(0).getLink().getHref(), is(baseHref + "1"));
        assertThat(versions.get(1).getLink().getHref(), is(baseHref + "2"));

        assertThat(fromFirstDateTime, is(start));
        assertThat(toFirstDateTime, is(end));
        assertThat(fromLastDateTime, is(newStart));
        assertThat(toLastDateTime, is(newEnd));
    }

    @Test
    public void listVersions_key_does_not_exist() {
        try {
            RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3336/versions", null, apiKey)
                    .request(MediaType.APPLICATION_JSON)
                    .get(WhoisInternalResources.class);
            fail();
        } catch (ClientErrorException e) {
            final String str = e.getResponse().readEntity(String.class);
            System.out.println(str);

            final WhoisInternalResources whoisResources = e.getResponse().readEntity(WhoisInternalResources.class);
            assertThat(whoisResources.getErrorMessages().get(0).toString(), is("No entries found for object AS3336"));
            assertThat(e.getResponse().getStatus(), is(404));
        }
    }

    @Test
    public void listVersions_objecttype_in_lowercase() throws Exception {
        objectReferenceDao.createVersion(new ObjectVersion(1l, ObjectType.AUT_NUM, "AS3333", new DateTime(2006, 6, 20, 0, 0, 0, 0), new DateTime(2006, 7, 12, 0, 0, 0, 0), 1));
        final WhoisInternalResources result = RestTest.target(getPort(), "api/rnd/test/aut-num/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisInternalResources.class);
        assertThat(result.getVersions(), hasSize(1));
    }

    @Test
    public void allow_person_query() {
        objectReferenceDao.createVersion(new ObjectVersion(1l, ObjectType.PERSON, "AA1-RIPE", new DateTime(2006, 6, 20, 0, 0, 0, 0), new DateTime(2006, 7, 12, 0, 0, 0, 0), 1));
        final WhoisInternalResources result = RestTest.target(getPort(), "api/rnd/test/person/AA1-RIPE/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisInternalResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersions();
        assertThat(versions, hasSize(1));

    }

    @Test
    public void listVersions_created_deleted_expected_json() {
        final DateTime start = new DateTime(2008, 2, 20, 0, 0, 0, 0);
        final DateTime end = new DateTime(2008, 2, 21, 0, 0, 0, 0);
        objectReferenceDao.createVersion(new ObjectVersion(1l, ObjectType.AUT_NUM, "AS3333", start, end, 1));

        final String response = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);
        System.out.println(response);
        assertThat(response, is(String.format("" +
                        "{\"versions\":[ {\n" +
                        "  \"type\" : \"AUT-NUM\",\n" +
                        "  \"pkey\" : \"AS3333\",\n" +
                        "  \"revision\" : 1,\n" +
                        "  \"from\" : \"%s\",\n" +
                        "  \"to\" : \"%s\",\n" +
                        "  \"link\" : {\n" +
                        "    \"type\" : \"locator\",\n" +
                        "    \"href\" : \"http://int.db.ripe.net/api/rnd/test/AUT-NUM/AS3333/1\"\n" +
                        "  }\n" +
                        "} ],\n" +
                        "\"terms-and-conditions\" : {\n" +
                        "  \"type\" : \"locator\",\n" +
                        "  \"href\" : \"http://www.ripe.net/db/support/db-terms-conditions.pdf\"\n" +
                        "}\n" +
                        "}",
                    dateTimeFormatter.print(start), dateTimeFormatter.print(end))));
    }
}