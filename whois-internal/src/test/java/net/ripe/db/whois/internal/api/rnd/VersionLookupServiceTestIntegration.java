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
import javax.ws.rs.core.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
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
    }


    @Test
    public void lookupFirstVersion() {
        final RpslObject mntner = RpslObject.parse("" +
                "mntner:         TEST-MNT\n" +
                "descr:          Maintainer\n" +
                "auth:           SSO person@net.net\n" +
                "auth:           MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
                "mnt-by:         TEST-MNT\n" +
                "referral-by:    TEST-MNT\n" +
                "upd-to:         noreply@ripe.net\n" +
                "changed:        noreply@ripe.net 20120101\n" +
                "source:         TEST");


        final RpslObjectUpdateInfo objectInfo = updateDao.createObject(mntner);
        final LocalDateTime localDateTime = new LocalDateTime();
        System.out.println(DEFAULT_DATE_TIME_FORMATTER.print(localDateTime));
        testDateTimeProvider.setTime(localDateTime.plusHours(1));
        updateDao.updateObject(objectInfo.getObjectId(), new RpslObjectBuilder(mntner).removeAttribute(new RpslAttribute(AttributeType.AUTH, "SSO person@net.net")).get());

        final WhoisResources result = RestTest.target(getPort(), String.format("api/rnd/test/mntner/SSO-PASSWORD-MNT/versions/%s", DEFAULT_DATE_TIME_FORMATTER.print(localDateTime)), null, apiKey)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(WhoisResources.class);
        assertThat(result, not(is(nullValue())));

    }
}
