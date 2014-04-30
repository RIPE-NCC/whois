package net.ripe.db.whois.query;

import net.ripe.db.whois.common.IntegrationTest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.support.DummyWhoisClient;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
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
            "auth:        PGPKEY-A8D16B70\n" +
            "mnt-by:      OWNER-MNT\n" +
            "referral-by: OWNER-MNT\n" +
            "changed:     dbtest@ripe.net 20120101\n" +
            "source:      TEST");

    private static final RpslObject KEYCERT = RpslObject.parse(
            "key-cert:       PGPKEY-A8D16B70\n" +
            "method:         PGP\n" +
            "owner:          Test Person5 <noreply5@ripe.net>\n" +
            "fingerpr:       D079 99F1 92D5 41B6 E7BC  6578 9175 DB8D A8D1 6B70\n" +
            "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "certif:         Version: GnuPG/MacGPG2 v2.0.18 (Darwin)\n" +
            "certif:         Comment: GPGTools - http://gpgtools.org\n" +
            "certif:\n" +
            "certif:         mI0EUXD+mAEEAKbIL2qj82+7FXCFJntqY50bdNI4NrhrMfGiBMpwmuuFscvI+iT7\n" +
            "certif:         /9AcLaAefKrKCqrM/MYLOx6b6UnIyBFqu/JVdl4aAtUpEUc4YV6jjvKa/29lAwi4\n" +
            "certif:         /gepCwBFL1b5hV7pxnM13ZOODrf10FMjq4Y0EXP4CFVf0wi0ryqIftatABEBAAG0\n" +
            "certif:         IFRlc3QgUGVyc29uNSA8bm9yZXBseTVAcmlwZS5uZXQ+iLkEEwECACMFAlFw/pgC\n" +
            "certif:         GwMHCwkIBwMCAQYVCAIJCgsEFgIDAQIeAQIXgAAKCRCRdduNqNFrcDArBACPPxu+\n" +
            "certif:         mdBqhEysSeQR1DrL02X4mwR3kkCElva4Yx91/TaZf0NC/Xa3Zr4mUKQgNW+Bp3j8\n" +
            "certif:         QN05jY4nqVYjkiFW6U9TMnGyFcwVzQdEJzgvjeZsuANd1RxdL8E4N0l4J+lYf5WN\n" +
            "certif:         5wfDgHE1aRm1QC5h1bOqThZ4/hFmddffL3TI77iNBFFw/pgBBACsA5ZBIZ7Ax2oA\n" +
            "certif:         XGPHooWP6K5d9Q5MDDyoyI6eyhVGhRFY0W40/9EdCCnzv3NNC5rnkXNrUpct3WVl\n" +
            "certif:         NpZybSVUqMkuOyAwgxqfe8k/EPpi6IfRUZeft/Hfpby7ycVUSWPxb4AmzNvR4lUp\n" +
            "certif:         wLfAJSETsrffOCo1hhGgT7Qg2SMLswARAQABiJ8EGAECAAkFAlFw/pgCGwwACgkQ\n" +
            "certif:         kXXbjajRa3AecwP7BbunGw89R2u8C+sw+chO3gyWr+klccxZ2g2RiGOMKWEVQXUM\n" +
            "certif:         Ru0OLzbKfGajl1RO4oo6aTLQAKwi7RoQO31mf699Nadt8nLnI3anVT3tcdI/HXNM\n" +
            "certif:         0qCy3r2tct/P63LCn+uIT4WvBjCp3gxPok3FdJf6iRon/J5lMNe3M3VjJoM=\n" +
            "certif:         =DgsR\n" +
            "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
            "mnt-by:         OWNER-MNT\n" +
            "changed:        dbtest@ripe.net 20120101\n" +
            "source:         TEST");

    @Before
    public void startupWhoisServer() throws Exception {
        databaseHelper.addObjects(PAULETH_PALTHEN, KEYCERT, OWNER_MNT);

        queryServer.start();
    }

    @After
    public void shutdownWhoisServer() {
        queryServer.stop(true);
    }

    @Ignore("TODO: [ES] mixed case lookup fails")
    @Test
    public void inverse_auth_pgpkey_case_sensitive() throws Exception {
        assertThat(query("-B -i auth PgPKeY-A8D16B70"), containsString("OWNER-MNT"));
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
    public void inverse_auth_sso_with_uuid() {
        assertThat(query("-B -i auth SSO 906635c2-0405-429a-800b-0602bd716124"),
                containsString("% Inverse search on 'auth' attribute is limited to 'key-cert' objects only"));
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

    private String query(final String query) {
        return DummyWhoisClient.query(QueryServer.port, query);
    }
}
