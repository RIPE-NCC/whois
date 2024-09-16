package net.ripe.db.whois.query;

import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.support.AbstractQueryIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

// TODO: [AH] this should be in whois-query; however, crowdserverdummy is tied to whois-api because of jetty references
@Tag("IntegrationTest")
@ContextConfiguration(locations = {"classpath:applicationContext-api-test.xml"})
public class InverseQueryTestIntegration extends AbstractQueryIntegrationTest {

    private static final RpslObject PAULETH_PALTHEN = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
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
            "source:         TEST");

    @Autowired IpRanges ipRanges;

    @BeforeEach
    public void startupWhoisServer() throws Exception {
        databaseHelper.addObjects(PAULETH_PALTHEN, KEYCERT, OWNER_MNT);

        queryServer.start();
    }

    @AfterEach
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


    @Test
    public void inverse_mnt_ref_person() {
        databaseHelper.addObject(
                "person:    Henry Mitchell\n" +
                        "nic-hdl:   TEST-HM3\n" +
                        "mnt-ref:   OWNER-MNT\n" +
                        "source:    TEST");

        final String response = query("-i mnt-ref OWNER-MNT");

        assertThat(response, containsString("TEST-HM3"));
    }

    @Test
    public void inverse_mnt_ref_role() {
        databaseHelper.addObject(
                "role:    tester\n" +
                        "nic-hdl:   RL-TEST\n" +
                        "mnt-ref:   OWNER-MNT\n" +
                        "source:    TEST");

        final String response = query("-i mnt-ref OWNER-MNT");

        assertThat(response, containsString("RL-TEST"));
    }

    @Test
    public void inverse_mnt_ref_irt() {
        databaseHelper.addObject(
                "irt: irt-IRT1\n" +
                        "mnt-ref:   OWNER-MNT\n" +
                        "source:    TEST");

        final String response = query("-i mnt-ref OWNER-MNT");

        assertThat(response, containsString("irt-IRT1"));
    }

    @Test
    public void inverse_mnt_ref_mntner() {
        databaseHelper.addObject(
                "mntner: TEST-MNT\n" +
                        "mnt-ref:   OWNER-MNT\n" +
                        "mnt-by:   TEST-MNT\n" +
                        "source:    TEST");

        final String response = query("-i mnt-ref OWNER-MNT");

        assertThat(response, containsString("TEST-MNT"));
    }

    @Test
    public void inverse_mnt_ref_no_results() {
        final String response = query("-i mnt-ref OWNER-MNT");

        assertThat(response, containsString("no entries found"));
    }

    @Test
    public void inverse_sponsoring_org_then_succeed() {
        databaseHelper.addObject(RpslObject.parse("" +
                "organisation: ORG-SPONSOR\n" +
                "org-name:     Sponsoring Org Ltd\n" +
                "org-type:     LIR\n" +
                "descr:        test org\n" +
                "address:      street 5\n" +
                "e-mail:       org1@test.com\n" +
                "mnt-ref:      OWNER-MNT\n" +
                "mnt-by:       OWNER-MNT\n" +
                "source:       TEST\n" +
                ""));
        databaseHelper.addObject("" +
                "aut-num:        AS102\n" +
                "as-name:        End-User-2\n" +
                "descr:          description\n" +
                "sponsoring-org: ORG-SPONSOR\n" +
                "admin-c:        PP1-TEST\n" +
                "tech-c:         PP1-TEST\n" +
                "mnt-by:         OWNER-MNT\n" +
                "source:         TEST\n");

        final String response = query("-i sponsoring-org ORG-SPONSOR");

        assertThat(response, containsString("ORG-SPONSOR"));
        assertThat(response, containsString("aut-num:        AS102"));
    }

    @Test
    public void inverse_email_from_trusted_then_succeed()  {
        ipRanges.setTrusted("127/8", "::1");

        final String response = query("-Bi e-mail noreply@ripe.net");

        assertThat(response, containsString("noreply@ripe.net"));
        assertThat(response, containsString("person:         Pauleth Palthen"));
    }


    @Test
    public void inverse_email_from_untrusted_then_fail() {
        ipRanges.setTrusted("::0");

        final String response = query("-Bi e-mail noreply@ripe.net");

        assertThat(response, containsString("attribute is not searchable"));
        assertThat(response, containsString("is not an inverse searchable attribute"));
    }
}
