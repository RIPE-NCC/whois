package net.ripe.db.whois.internal.api.rnd;

import net.ripe.db.whois.api.RestTest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.rest.mapper.WhoisObjectMapper;
import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.internal.AbstractInternalTest;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

@Category(IntegrationTest.class)
public class VersionLookupServiceTestIntegration extends AbstractInternalTest {

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

        updateDao.createObject(RpslObject.parse("" +
                "mntner:         TEST-MNT\n" +
                "descr:          Maintainer\n" +
                "auth:           SSO person@net.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:         TEST-MNT\n" +
                "referral-by:    TEST-MNT\n" +
                "upd-to:         noreply@ripe.net\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST"));
    }

    @Test
    public void lookupFirstVersion() {
        final RpslObject mntner = RpslObject.parse("" +
                "mntner:         TST-MNT\n" +
                "descr:          Maintainer\n" +
                "auth:           SSO person@net.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:         TST-MNT\n" +
                "referral-by:    TST-MNT\n" +
                "upd-to:         noreply@ripe.net\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST");


        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(mntner);
        final LocalDateTime localDateTime = new LocalDateTime();
        testDateTimeProvider.setTime(localDateTime.plusDays(1));
        updateDao.updateObject(objectInfo.getObjectId(), new RpslObjectBuilder(mntner).removeAttribute(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")).get());

        final String creationTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime);
        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/mntner/TST-MNT/versions/%s", creationTimestamp), null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
        assertThat(result, not(is(nullValue())));
        assertThat(result.getWhoisObjects(), hasSize(1));
//        assertThat(result.getWhoisObjects().get(0).getTimestamp(), is(creationTimestamp));
    }

    @Test
    public void lookupRandomVersion() {
        final RpslObject organisation = RpslObject.parse("" +
                "organisation: ORG-TOL1-TEST\n" +
                "org-name: Acme carpets\n" +
                "org-type: OTHER\n" +
                "address: street\n" +
                "address: postcode\n" +
                "address: country\n" +
                "e-mail: test@dev.net\n" +
                "mnt-ref: TEST-MNT\n" +
                "mnt-by: TEST-MNT\n" +
                "changed: test@test.net\n" +
                "source: TEST");

        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(organisation);

        final LocalDateTime localDateTime = new LocalDateTime();
        testDateTimeProvider.setTime(localDateTime.plusDays(5));

        updateDao.updateObject(objectInfo.getObjectId(),
                new RpslObjectBuilder(organisation)
                        .removeAttributeType(AttributeType.ADDRESS)
                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "one line address"))
                        .get()
        );


        testDateTimeProvider.setTime(localDateTime.plusDays(13));

        updateDao.updateObject(objectInfo.getObjectId(),
                new RpslObjectBuilder(organisation)
                        .removeAttributeType(AttributeType.ADDRESS)
                        .addAttributeSorted(new RpslAttribute(AttributeType.ADDRESS, "different address"))
                        .get());

        final String randomTimestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(6));
        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/organisation/ORG-TOL1-TEST/versions/%s", randomTimestamp), null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(result.getWhoisObjects().get(0).getAttributes().get(3).getValue(), is("one line address"));
    }

    @Test(expected = NotFoundException.class)
    public void doesNotExist() {
        RestTest.target(getPort(), String.format("api/rnd/test/aut-num/AS123/versions/%s", new LocalDateTime()), null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
    }

    @Test
    public void sameOrVeryNearlyTheSameTimestamp() {
        final RpslObject before = RpslObject.parse("" +
                "person: Test Person\n" +
                "address: street\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "changed: test@ripe.net\n" +
                "source: TEST");
        final RpslObject after = RpslObject.parse("" +
                "person: Test Person\n" +
                "address: other street\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "changed: test@ripe.net\n" +
                "source: TEST");

        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(before);
        updateDao.updateObject(objectInfo.getObjectId(), after);

        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/person/TP1-TEST/versions/%s", new LocalDateTime()), null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);

        assertThat(result.getWhoisObjects().get(0).getAttributes().get(1).getValue(), is("other street"));
    }

    @Test(expected = NotFoundException.class)
    public void deleted() {
        final RpslObject object = RpslObject.parse("" +
                "person: Test Person\n" +
                "address: street\n" +
                "nic-hdl: TP1-TEST\n" +
                "mnt-by: TEST-MNT\n" +
                "changed: test@ripe.net\n" +
                "source: TEST");
        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(object);

        final LocalDateTime localDateTime = new LocalDateTime();
        testDateTimeProvider.setTime(localDateTime.plusDays(7));

        updateDao.deleteObject(objectInfo.getObjectId(), "TP1-TEST");


        testDateTimeProvider.setTime(localDateTime.plusDays(13));
        updateDao.createObject(object);

        final String timestamp = DEFAULT_DATE_TIME_FORMATTER.print(localDateTime.plusDays(9));
        RestTest.target(getPort(), String.format("api/rnd/test/person/TP1-TEST/versions/%s", timestamp), null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
    }
}
