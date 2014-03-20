package net.ripe.db.whois.query;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

// TODO: [AH] this should be in whois-query; however, crowdserverdummy is tied to whois-api because of jetty references
@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public class InverseQueryTestIntegration extends AbstractQueryIntegrationTest {

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "changed:   noreply@ripe.net 20120101\n" +
            "remarks:   remark\n" +
            "source:    TEST\n");

    private static final RpslObject OWNER_MNT = RpslObject.parse("" +
            "mntner:      OWNER-MNT\n" +
            "descr:       Owner Maintainer\n" +
            "admin-c:     PP1-TEST\n" +
            "upd-to:      noreply@ripe.net\n" +
            "auth:        MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/ #test\n" +
            "auth:        SSO person@net.net\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");

    @Before
    public void startupWhoisServer() throws Exception {
        databaseHelper.addObjects(PAULETH_PALTHEN, OWNER_MNT);

        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void inverse_auth_pgp() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B -i auth PGP-12345678");
        System.err.println(response);
        assertThat(response, containsString("%ERROR:101: no entries found"));
    }

    @Test
    public void inverse_auth_sso_with_email() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B -i auth SSO person@net.net");
        System.err.println(response);
        assertThat(response, containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }

    @Test
    public void inverse_auth_sso_with_uuid() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B -i auth SSO 906635c2-0405-429a-800b-0602bd716124");
        System.err.println(response);
        assertThat(response, containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }

    @Test
    public void inverse_auth_md5() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B -i auth MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/");
        System.err.println(response);
        assertThat(response, containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }

    @Test
    public void inverse_auth_unknown_md5() {
        final String response = DummyWhoisClient.query(QueryServer.port, "-B -i auth MD5-PW $1$deadbeef$Si7YudNf4rUGmR71n/cqk/");
        System.err.println(response);
        assertThat(response, containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }
}
