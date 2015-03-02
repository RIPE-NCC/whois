package net.ripe.db.whois.query.planner;

import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.TestTimestampsMode;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

@Component
@ActiveProfiles(WhoisProfile.TEST)
@ContextConfiguration(locations = {"classpath:applicationContext-query-test.xml"})
public class TimestampFilterFunctionTest  extends AbstractJUnit4SpringContextTests {

    @Autowired
    private TestTimestampsMode testTimestampsMode;

    @Autowired
    private TimestampFilterFunction timestampFilterFunction;

    @Test
    public void attributes_in_db_timestamps_off() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:  TST-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "created: 2014-01-26T11:44:59Z\n" +
                "last-modified: 2014-01-26T11:44:59Z\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        testTimestampsMode.setTimestampsOff(true);
        final ResponseObject result = timestampFilterFunction.apply(object);

        assertThat(result.toString(), not(containsString("created")));
        assertThat(result.toString(), not(containsString("last-modified")));
    }


    @Test
    public void attributes_not_in_db_and_timestamps_off() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:  TST-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        testTimestampsMode.setTimestampsOff(true);
        final ResponseObject result = timestampFilterFunction.apply(object);

        assertThat(result.toString(), not(containsString("created")));
        assertThat(result.toString(), not(containsString("last-modified")));
    }

    @Test
    public void attributes_not_in_db_and_timestamps_on() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:  TST-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        testTimestampsMode.setTimestampsOff(false);
        final ResponseObject result = timestampFilterFunction.apply(object);

        assertThat(result.toString(), not(containsString("created")));
        assertThat(result.toString(), not(containsString("last-modified")));
    }

    @Test
    public void attributes_in_db_and_timestamps_on() {
        final RpslObject object = RpslObject.parse("" +
                "mntner:  TST-MNT\n" +
                "descr:   description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by:  TST-MNT\n" +
                "referral-by: TST-MNT\n" +
                "upd-to:  dbtest@ripe.net\n" +
                "auth:    MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "created: 2014-01-26T11:44:59Z\n" +
                "last-modified: 2014-01-26T11:44:59Z\n" +
                "changed: dbtest@ripe.net 20120707\n" +
                "source:  TEST");

        testTimestampsMode.setTimestampsOff(false);
        final ResponseObject result = timestampFilterFunction.apply(object);

        assertThat(result.toString(), containsString("created"));
        assertThat(result.toString(), containsString("last-modified"));
    }


}
