package net.ripe.db.whois.query;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.support.TelnetWhoisClient;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import static net.ripe.db.whois.RpslObjectFixtures.KEYCERT;
import static net.ripe.db.whois.RpslObjectFixtures.OWNER_MNT;
import static net.ripe.db.whois.RpslObjectFixtures.PAULETH_PALTHEN;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

// TODO: [AH] this should be in whois-query; however, crowdserverdummy is tied to whois-api because of jetty references
@Category(IntegrationTest.class)
@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public class InverseQueryTestIntegration extends AbstractQueryIntegrationTest {

    @Autowired IpRanges ipRanges;

    @Before
    public void startupWhoisServer() throws Exception {
        databaseHelper.addObjects(PAULETH_PALTHEN, KEYCERT, OWNER_MNT);

        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Test
    public void inverse_auth_pgpkey_case_sensitive() throws Exception {
        assertThat(query("-B -i auth PgPKeY-A8D16B70"), containsString("OWNER-MNT"));
    }

    @Test
    public void inverse_upd_to_case_sensitive() throws Exception {
        assertThat(query("-B -i upd-to noreply@riPe.net"), containsString("OWNER-MNT"));
    }

    @Test
    public void inverse_auth_pgpkey() throws Exception {
        assertThat(query("-B -i auth PGPKEY-A8D16B70"), containsString("OWNER-MNT"));
    }

    @Test
    public void inverse_auth_nonexistant_pgpkey() {
        assertThat(query("-B -i auth PGPKEY-12345678"),
                containsString("%ERROR:101: no entries found"));
    }

    @Test
    public void inverse_auth_sso_with_email() {
        assertThat(query("-B -i auth SSO person@net.net"),
                containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }

    @Test
    public void inverse_auth_sso_with_uuid_untrusted() {
        ipRanges.setTrusted("::0");

        assertThat(query("-B -i auth SSO 906635c2-0405-429a-800b-0602bd716124"),
                containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }

    @Test
    public void inverse_auth_sso_with_uuid_trusted() {
        ipRanges.setTrusted("127/8", "::1");

        final String response = query("-B -i auth SSO 906635c2-0405-429a-800b-0602bd716124");

        assertThat(response, containsString("mntner:         OWNER-MNT"));
        assertThat(response, containsString("auth:           SSO # Filtered"));
    }

    @Test
    public void inverse_auth_md5() {
        assertThat(query("-B -i auth MD5-PW $1$d9fKeTr2$Si7YudNf4rUGmR71n/cqk/"),
                containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }

    @Test
    public void inverse_auth_unknown_md5() {
        assertThat(query("-B -i auth MD5-PW $1$deadbeef$Si7YudNf4rUGmR71n/cqk/"),
                containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
    }

    @Test
    public void inverse_invalid_nic_hdl() {
        databaseHelper.addObject(
                "person:    Henry Mitchell\n" +
                "nic-hdl:   TEST-HM3\n" +
                "source:    TEST");
        databaseHelper.addObject(
                "mntner:    Another Maintainer\n" +
                "tech-c:    TEST-HM3\n" +
                "source:    TEST");

        assertThat(query("-i tech-c TEST-HM3"), containsString("Another Maintainer"));
    }

    @Test
    public void inverse_referral_by_is_invalid() {
        assertThat(query("-i referral-by TEST"), containsString("\"referral-by\" is not a known attribute."));
    }

    private String query(final String query) {
        return TelnetWhoisClient.queryLocalhost(QueryServer.port, query);
    }
}
