package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionInternal;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.domain.serials.Operation;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.joda.time.LocalDateTime.parse;


@Category(IntegrationTest.class)
public class VersionListServiceTestIntegration extends AbstractInternalTest {
    private DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    @Autowired
    @Qualifier("whoisReadOnlySlaveDataSource")
    DataSource dataSource;

    @Autowired
    WhoisObjectMapper whoisObjectMapper;

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

        assertThat(Period.fieldDifference(fromDate, toDate).getDays(), is(3));
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


        assertThat(versions.get(0).getOperation(), is(Operation.UPDATE.toString()));
        assertThat(Period.fieldDifference(fromDate, toDate).getDays(), is(3));
    }


    @Test
    public void listVersions_created_deleted_recreated() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num: AS3335\n" +
                "source: TEST");
        final RpslObjectUpdateInfo add = updateDao.createObject(rpslObject);
        testDateTimeProvider.setTime(new LocalDateTime().plusDays(3));
        updateDao.deleteObject(add.getObjectId(), "AS3335");
        testDateTimeProvider.setTime(new LocalDateTime().plusDays(5));
        updateDao.createObject(rpslObject);

        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3335/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        String json = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3335/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE).get(String.class);
        String xml = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3335/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE).get(String.class);

        System.out.println(json);
        System.out.println(xml);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersionsInternal().getVersions();
        assertThat(versions, hasSize(2));

        LocalDateTime fromDateFirst = parse(versions.get(0).getFrom(), DEFAULT_DATE_TIME_FORMATTER);
        LocalDateTime toDateFirst = parse(versions.get(0).getTo(), DEFAULT_DATE_TIME_FORMATTER);
        assertThat(Period.fieldDifference(fromDateFirst, toDateFirst).getDays(), is(3));

        LocalDateTime fromDateLast = parse(versions.get(1).getFrom(), DEFAULT_DATE_TIME_FORMATTER);
        assertThat(Period.fieldDifference(toDateFirst, fromDateLast).getDays(), is(2));
        assertThat(versions.get(1).getTo(), is(""));
    }

    @Test
    public void listVersions_key_does_not_exist() {
        try {
            RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3336/versions", null, apiKey)
                    .request(MediaType.APPLICATION_JSON)
                    .get(WhoisResources.class);

        } catch (ClientErrorException e) {
            WhoisResources whoisResources = e.getResponse().readEntity(WhoisResources.class);
            assertThat(e.getResponse().getStatus(), is(404));

            //           assertThat(e.getResponse().getStatus(), is(404));
            //more assertions perhaps
        }
    }
}