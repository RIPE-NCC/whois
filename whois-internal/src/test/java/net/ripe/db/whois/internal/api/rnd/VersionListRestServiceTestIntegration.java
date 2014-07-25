package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionInternal;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.joda.time.LocalDateTime.parse;


@Category(IntegrationTest.class)
public class VersionListRestServiceTestIntegration extends AbstractInternalTest {
    @Autowired
    @Qualifier("whoisReadOnlySlaveDataSource")
    DataSource dataSource;
    @Autowired
    WhoisObjectMapper whoisObjectMapper;
    private DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    @Before
    public void setUp() throws Exception {
        testDateTimeProvider.reset();

        databaseHelper.setupWhoisDatabase(new JdbcTemplate(dataSource));
        databaseHelper.insertApiKey(apiKey, "/api/rnd", "rnd api key");
    }


    @Test
    public void listVersions_created_updated() {
        final RpslObjectUpdateInfo add = updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));
        testDateTimeProvider.setTime(new LocalDateTime().plusDays(3));
        updateDao.updateObject(add.getObjectId(), RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "remarks: updated\n" +
                "source: TEST"));

        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersionsInternal().getVersions();
        assertThat(versions, hasSize(2));
        final String fromFirst = versions.get(0).getFrom();
        final String toFirst = versions.get(0).getTo();
        final String fromLast = versions.get(1).getFrom();
        final String toLast = versions.get(1).getTo();

        final LocalDateTime fromDate = parse(fromFirst, DEFAULT_DATE_TIME_FORMATTER);
        final LocalDateTime toDate = parse(toFirst, DEFAULT_DATE_TIME_FORMATTER);
        final LocalDateTime fromLastDate = parse(fromLast, DEFAULT_DATE_TIME_FORMATTER);

        assertThat(new Period(fromDate, toDate).getDays(), is(3));
        assertThat(toDate, is(fromLastDate));
        assertThat(toLast, is(""));
    }

    @Test
    public void listVersions_created_deleted() {
        final RpslObjectUpdateInfo add = updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));

        LocalDateTime deleteDate = new LocalDateTime().plusDays(3);
        testDateTimeProvider.setTime(deleteDate);
        updateDao.deleteObject(add.getObjectId(), "AS3333");

        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersionsInternal().getVersions();
        assertThat(versions, hasSize(1));

        LocalDateTime fromDate = parse(versions.get(0).getFrom(), DEFAULT_DATE_TIME_FORMATTER);
        LocalDateTime toDate = parse(versions.get(0).getTo(), DEFAULT_DATE_TIME_FORMATTER);

        assertThat(new Period(fromDate, toDate).getDays(), is(3));
    }


    @Test
    public void listVersions_created_deleted_recreated() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num: AS3335\n" +
                "source: TEST");
        final RpslObject person = RpslObject.parse("" +
                "person: First Last\n" +
                "nic-hdl: AA1-RIPE\n" +
                "remarks: Some remark\n" +
                "source: TEST");

        final RpslObjectUpdateInfo add = updateDao.createObject(rpslObject);

        updateDao.createObject(person);
        testDateTimeProvider.setTime(new LocalDateTime().plusDays(3));
        updateDao.deleteObject(add.getObjectId(), "AS3335");
        testDateTimeProvider.setTime(new LocalDateTime().plusDays(5));
        updateDao.createObject(rpslObject);

        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3335/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        String baseHref = "http://rest.db.ripe.net/api/rnd/test/AUT-NUM/AS3335/";

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersionsInternal().getVersions();
        assertThat(versions, hasSize(2));

        LocalDateTime fromDateFirst = parse(versions.get(0).getFrom(), DEFAULT_DATE_TIME_FORMATTER);
        LocalDateTime toDateFirst = parse(versions.get(0).getTo(), DEFAULT_DATE_TIME_FORMATTER);
        assertThat(new Period(fromDateFirst, toDateFirst).getDays(), is(3));
        assertThat(versions.get(0).getLink().toString(), containsString(baseHref + "1"));

        LocalDateTime fromDateLast = parse(versions.get(1).getFrom(), DEFAULT_DATE_TIME_FORMATTER);
        assertThat(new Period(toDateFirst, fromDateLast).getDays(), is(2));
        assertThat(versions.get(1).getTo(), is(""));
        assertThat(versions.get(1).getLink().toString(), containsString(baseHref + "2"));
    }

    @Test
    public void listVersions_key_does_not_exist() {
        try {
            RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3336/versions", null, apiKey)
                    .request(MediaType.APPLICATION_JSON)
                    .get(WhoisResources.class);

        } catch (ClientErrorException e) {
            final WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(whoisResources.getErrorMessages().get(0).toString(), is("No entries found for object AS3336"));
            assertThat(e.getResponse().getStatus(), is(404));
        }
    }

    @Test
    public void listVersions_objecttype_in_lowercase() throws Exception {
        updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));
        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/aut-num/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
        assertThat(result.getVersionsInternal().getVersions(), hasSize(1));
    }

    @Test
    public void allow_person_query() {
        final RpslObject person = RpslObject.parse("person: First Last\n" +
                "nic-hdl: AA1-RIPE\n" +
                "remarks: Some remark\n" +
                "source: TEST");
        updateDao.createObject(person);

        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/person/AA1-RIPE/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersionsInternal().getVersions();
        assertThat(versions, hasSize(1));

    }

    //TODO [TP]: update test when output is formatted
    @Test
    public void listVersions_created_deleted_expected_xml() {
        final RpslObjectUpdateInfo add = updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));

        final LocalDateTime createdDate = new LocalDateTime();
        final LocalDateTime deleteDate = createdDate.plusDays(3);
        testDateTimeProvider.setTime(deleteDate);
        updateDao.deleteObject(add.getObjectId(), "AS3333");

        final String response = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        assertThat(response, is(String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                        "<whois-resources xmlns:xlink=\"http://www.w3.org/1999/xlink\">" +
                        "<errormessages/>" +
                        "<versionsInternal type=\"AUT-NUM\" key=\"AS3333\">" +
                        "<version>" +
                        "<revision>1</revision>" +
                        "<from>%s</from>" +
                        "<to>%s</to>" +
                        "<link xlink:type=\"locator\" xlink:href=\"http://rest.db.ripe.net/api/rnd/test/AUT-NUM/AS3333/1\"/>" +
                        "</version>" +
                        "</versionsInternal>" +
                        "<terms-and-conditions xlink:type=\"locator\" xlink:href=\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"/>" +
                        "</whois-resources>", createdDate.toString(DEFAULT_DATE_TIME_FORMATTER), deleteDate.toString(DEFAULT_DATE_TIME_FORMATTER)
        )));
    }

    //TODO [TP]: update test when output is formatted
    @Test
    public void listVersions_created_deleted_expected_json() {
        final RpslObjectUpdateInfo add = updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));

        final LocalDateTime createdDate = new LocalDateTime();
        final LocalDateTime deleteDate = createdDate.plusDays(3);
        testDateTimeProvider.setTime(deleteDate);
        updateDao.deleteObject(add.getObjectId(), "AS3333");

        final String response = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        assertThat(response, is(String.format(
                        "{\"errormessages\":{\"errormessage\":[]}," +
                        "\"versionsInternal\":{\"type\":\"AUT-NUM\",\"key\":\"AS3333\"," +
                            "\"version\":[{" +
                                "\"revision\":1," +
                                "\"from\":\"%s\"," +
                                "\"to\":\"%s\"," +
                                "\"link\":{\"type\":\"locator\",\"href\":\"http://rest.db.ripe.net/api/rnd/test/AUT-NUM/AS3333/1\"}}]}," +
                        "\"terms-and-conditions\":{\"type\":\"locator\",\"href\":\"http://www.ripe.net/db/support/db-terms-conditions.pdf\"}}",
                    createdDate.toString(DEFAULT_DATE_TIME_FORMATTER), deleteDate.toString(DEFAULT_DATE_TIME_FORMATTER))));
    }

    @Test  //TODO [TP]: remove empty errormessages
    public void search_no_empty_errormessages_in_xml_response() {
        updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));

        final String result = RestTest.target(getPort(), "api/rnd/test/aut-num/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_XML_TYPE)
                .get(String.class);

        Assert.assertThat(result, containsString("<errormessages"));
        Assert.assertThat(result, containsString("<revision>1</revision>"));
    }
}