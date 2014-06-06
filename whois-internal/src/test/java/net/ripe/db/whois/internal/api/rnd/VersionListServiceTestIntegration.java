package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.domain.WhoisVersionInternal;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;


@Category(IntegrationTest.class)
public class VersionListServiceTestIntegration extends AbstractInternalTest {

    @Before
    public void setup() {
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
        final String from = versions.get(0).getFrom();
        final String to = versions.get(0).getTo();

        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
        final LocalDateTime fromDate = LocalDateTime.parse(from, formatter);
        final LocalDateTime toDate = LocalDateTime.parse(to, formatter);

        assertThat(Period.fieldDifference(fromDate, toDate).getDays(), is(3));
    }

    @Test
    public void listVersions_created_deleted() {
        final RpslObjectUpdateInfo add = updateDao.createObject(RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST"));
        testDateTimeProvider.setTime(new LocalDateTime().plusDays(3));
        updateDao.deleteObject(add.getObjectId(), "AS3333");

        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersionsInternal().getVersions();
        assertThat(versions, hasSize(2));

        // which order should they be shown in?
        assertThat(versions.get(0).getOperation().equals("DEL") || versions.get(1).getOperation().equals("DEL"), is(true));
    }

    @Test
    public void listVersions_created_deleted_recreated() {
        final RpslObject rpslObject = RpslObject.parse("" +
                "aut-num: AS3333\n" +
                "source: TEST");
        final RpslObjectUpdateInfo add = updateDao.createObject(rpslObject);
        testDateTimeProvider.setTime(new LocalDateTime().plusDays(3));
        updateDao.deleteObject(add.getObjectId(), "AS3333");
        updateDao.createObject(rpslObject);

        final WhoisResources result = RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(result.getErrorMessages(), hasSize(0));
        final List<WhoisVersionInternal> versions = result.getVersionsInternal().getVersions();
        assertThat(versions, hasSize(3));
    }

    @Test
    public void listVersions_key_does_not_exist() {
        try {
            RestTest.target(getPort(), "api/rnd/test/AUT-NUM/AS3333/versions", null, apiKey)
                .request(MediaType.APPLICATION_XML)
                .get(WhoisResources.class);

        } catch (ClientErrorException e) {
            assertThat(e.getResponse().getStatus(), is(404));
            //more assertions perhaps
        }
    }
}