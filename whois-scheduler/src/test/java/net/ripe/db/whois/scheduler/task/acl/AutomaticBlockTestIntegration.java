package net.ripe.db.whois.scheduler.task.acl;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.QueryServer;
import net.ripe.db.whois.query.acl.IpResourceConfiguration;
import net.ripe.db.whois.query.acl.PersonalObjectAccounting;
import net.ripe.db.whois.scheduler.AbstractSchedulerIntegrationTest;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.InetAddress;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@Category(IntegrationTest.class)
public class AutomaticBlockTestIntegration extends AbstractSchedulerIntegrationTest {

    private static final int NR_DAYS_BEFORE_PERMANENT_BAN = 10;
    private final String personQuery = "-r -T person test";
    private InetAddress localHost;

    @Autowired PersonalObjectAccounting personalObjectAccounting;
    @Autowired AutomaticPermanentBlocks automaticPermanentBlocks;
    @Autowired IpResourceConfiguration ipResourceConfiguration;

    @Before
    public void startupServer() throws Exception {
        localHost = InetAddress.getByName("127.0.0.1");

        databaseHelper.clearAclLimits();
        databaseHelper.insertAclIpLimit("0/0", 1, true);
        databaseHelper.insertAclIpLimit("::0/0", 1, true);
        ipResourceConfiguration.reload();

        databaseHelper.addObject("person:test person\nnic-hdl:TP-RIPE");
        databaseHelper.addObject("role: Abuse Role\nnic-hdl:NOAB-RIPE");
        databaseHelper.addObject("role: NOAbuse Role\nnic-hdl:AB-RIPE\nabuse-mailbox: abuse@ripe.net");
        databaseHelper.addObject("organisation: ORG-1\nabuse-c: AB-RIPE");

        queryServer.start();
    }

    @After
    public void shutdownServer() {
        queryServer.stop(true);
    }

    @Test
    public void test_ban_and_unban() throws Exception {
        int currentDay = 0;
        for (int day = 1; day <= NR_DAYS_BEFORE_PERMANENT_BAN; day++) {
            testDateTimeProvider.setTime(new LocalDateTime().plusDays(currentDay++));
            queryAndCheckNotBanned(personQuery, "person:         test person");

            // Caught by ACL manager
            queryAndCheckBanned(QueryMessages.accessDeniedTemporarily(localHost));

            // Caught by ACL handler
            queryAndCheckBanned(QueryMessages.accessDeniedTemporarily(localHost));
            dailyMaintenance();
        }

        testDateTimeProvider.setTime(new LocalDateTime().plusDays(currentDay++));
        dailyMaintenance();
        queryAndCheckBanned(QueryMessages.accessDeniedPermanently(localHost));

        testDateTimeProvider.setTime(new LocalDateTime().plusDays(currentDay++));
        databaseHelper.unban("127.0.0.1/32");

        dailyMaintenance();

        testDateTimeProvider.setTime(new LocalDateTime().plusDays(currentDay++));
        queryAndCheckNotBanned(personQuery, "person:         test person");
    }

    @Test
    public void dont_ban_roles() throws Exception {
        final String roleQuery = "-r -T role NOAbuse";
        for (int tries = 1; tries <= 10; tries++) {
            queryAndCheckNotBanned(roleQuery, "role:           NOAbuse Role");
        }
    }

    private void queryAndCheckNotBanned(final String query, final String personOrRole) throws Exception {
        final String result = query(query);
        assertThat(result, containsString(personOrRole));
        assertThat(result, not(containsString(QueryMessages.accessDeniedTemporarily(localHost).toString())));
        assertThat(result, not(containsString(QueryMessages.accessDeniedPermanently(localHost).toString())));
    }

    private void queryAndCheckBanned(final Message messages) throws Exception {
        final String result = query(personQuery);
        assertThat(result, not(containsString("person:         test person")));
        assertThat(result, containsString(messages.toString()));
    }

    private void dailyMaintenance() {
        personalObjectAccounting.resetAccounting();
        automaticPermanentBlocks.run();
        ipResourceConfiguration.reload();
    }

    private String query(final String query) throws Exception {
        return DummyWhoisClient.query(QueryServer.port, query);
    }
}
