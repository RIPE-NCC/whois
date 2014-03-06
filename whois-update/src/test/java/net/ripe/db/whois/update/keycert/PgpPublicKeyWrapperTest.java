package net.ripe.db.whois.update.keycert;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.IOUtils;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PgpPublicKeyWrapperTest {

    @Mock DateTimeProvider dateTimeProvider;

    private RpslObject pgpKeycert;
    private RpslObject anotherPgpKeycert;
    private RpslObject x509Keycert;

    @Before
    public void setup() throws Exception {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.now());
        pgpKeycert = RpslObject.parse(getResource("keycerts/PGPKEY-A8D16B70.TXT"));
        anotherPgpKeycert = RpslObject.parse(getResource("keycerts/PGPKEY-28F6CD6C.TXT"));
        x509Keycert = RpslObject.parse(getResource("keycerts/X509-1.TXT"));
    }

    @Test
    public void pgpFingerprint() throws Exception {
        PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

        assertThat(subject.getFingerprint(),
                is("D079 99F1 92D5 41B6 E7BC  6578 9175 DB8D A8D1 6B70"));
    }

    @Test
    public void isPgpKey() throws IOException {
        assertThat(PgpPublicKeyWrapper.looksLikePgpKey(pgpKeycert), is(true));
        assertThat(PgpPublicKeyWrapper.looksLikePgpKey(x509Keycert), is(false));
    }

    @Test
    public void isEquals() throws IOException {
        PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);
        PgpPublicKeyWrapper another = PgpPublicKeyWrapper.parse(anotherPgpKeycert);

        assertThat(subject.equals(subject), is(true));
        assertThat(subject.equals(another), is(false));
    }

    @Test
    public void method() {
        PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

        assertThat(subject.getMethod(), is("PGP"));
    }

    @Test
    public void owner() {
        PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

        assertThat(subject.getOwners(), containsInAnyOrder("Test Person5 <noreply5@ripe.net>"));
    }

    @Test
    public void multiplePublicKeys() throws Exception {
        try {
            PgpPublicKeyWrapper.parse(RpslObject.parse(getResource("keycerts/PGPKEY-MULTIPLE-PUBLIC-KEYS.TXT")));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("The supplied object has multiple keys"));
        }
    }

    @Test
    public void onePublicKeyWithMultipleSubkeys() throws Exception {
        PgpPublicKeyWrapper result = PgpPublicKeyWrapper.parse(RpslObject.parse(getResource("keycerts/PGPKEY-MULTIPLE-SUBKEYS.TXT")));

        assertNotNull(result.getPublicKey());
        assertThat(result.getSubKeys(), hasSize(1));
    }

    @Test
    public void parsePrivateKey() throws Exception {
        try {
            PgpPublicKeyWrapper.parse(RpslObject.parse(getResource("keycerts/PGPKEY-PRIVATE-KEY.TXT")));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("The supplied object has no key"));
        }
    }

    @Test
    public void isExpired() throws Exception {
        PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
                RpslObject.parse(
                        "key-cert:       PGPKEY-C88CA438\n" +
                        "method:         PGP\n" +
                        "owner:          Expired <expired@ripe.net>\n" +
                        "fingerpr:       610A 2457 2BA3 A575 5F85  4DD8 5E62 6C72 C88C A438\n" +
                        "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                        "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                        "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                        "certif:\n" +
                        "certif:         mI0EUOoKSgEEAMvJBJzUBKDA8BGK+KpJMuGSOXnQgvymxgyOUOBVkLpeOcPQMy1A\n" +
                        "certif:         4fffXJ4V0xdlqtikDATCnSIBS17ihi7xD8fUvKF4dJrq+rmaVULoy06B68IcfYKQ\n" +
                        "certif:         yoRJqGii/1Z47FuudeJp1axQs1JER3OJ64IHuLblFIT7oS+YWBLopc1JABEBAAG0\n" +
                        "certif:         GkV4cGlyZWQgPGV4cGlyZWRAcmlwZS5uZXQ+iL4EEwECACgFAlDqCkoCGwMFCQAB\n" +
                        "certif:         UYAGCwkIBwMCBhUIAgkKCwQWAgMBAh4BAheAAAoJEF5ibHLIjKQ4tEMD/j8VYxdY\n" +
                        "certif:         V6JM8rDokg+zNE4Ifc7nGaUrsrF2YRmcIg6OXVhPGLIqfQB2IsKub595sA1vgwNs\n" +
                        "certif:         +Cg0tzaQfzWh2Nz5NxFGnDHm5tPfOfiADwpMuLtZby390Wpbwk7VGZMqfcDXt3uy\n" +
                        "certif:         Ch4rvayDTtzQqDVqo1kLgK5dIc/UIlX3jaxWuI0EUOoKSgEEANYcEMxrEGD4LSgk\n" +
                        "certif:         vHVECSOB0q32CN/wSrvVzL6hP8RuO0gwwVQH1V8KCYiY6kDEk33Qb4f1bTo+Wbi6\n" +
                        "certif:         9yFvn1OvLh3/idb3U1qSq2+Y6Snl/kvgoVJQuS9x1NePtCYL2kheTAGiswg6CxTF\n" +
                        "certif:         RZ3c7CaNHsCbUdIpQmNUxfcWBH3PABEBAAGIpQQYAQIADwUCUOoKSgIbDAUJAAFR\n" +
                        "certif:         gAAKCRBeYmxyyIykON13BACeqmXZNe9H/SK2AMiFLIx2Zfyw/P0cKabn3Iaan7iF\n" +
                        "certif:         kSwrZQhF4571MBxb9U41Giiyza/t7vLQH1S+FYFUqfWCa8p1VQDRavi4wDgy2PDp\n" +
                        "certif:         ouhDqH+Mfkqb7yv40kYOUJ02eKkdgSgwTEcpfwq9GU4kJLVO5O3Y3nOEAx736gPQ\n" +
                        "certif:         xw==\n" +
                        "certif:         =XcVO\n" +
                        "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                        "mnt-by:         UPD-MNT\n" +
                        "changed:        dbtest@ripe.net 20120101\n" +
                        "source:         TEST"));

        assertThat(subject.isExpired(dateTimeProvider), is(true));
    }

    @Test
    public void notExpired() throws Exception {
        PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

        assertThat(subject.isExpired(dateTimeProvider), is(false));
    }

    @Test
    public void isRevoked() throws Exception {
        try {
            PgpPublicKeyWrapper.parse(
                    RpslObject.parse(
                            "key-cert:       PGPKEY-A48E76B2\n" +
                            "method:         PGP\n" +
                            "owner:          Revoked <revoked@ripe.net>\n" +
                            "fingerpr:       D9A8 D291 0E72 DE20 FE50  C8FD FC24 50DF A48E 76B2\n" +
                            "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                            "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                            "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                            "certif:         \n" +
                            "certif:         mI0EUOtGSgEEALdT44Ijp/M+KUvuSjLR//SBhvAO1V+MzgpWPB6vSEZO8uSxAQCQ\n" +
                            "certif:         4gdXcsgpHjbcqe1KO00obtB74scD50l4sm9XPPr6tMK9I7MgwRlgRoJDWmw3lTFG\n" +
                            "certif:         7H1MSqI+RY9EcXTxbtfflfkoexvwIhheHge9OUNsdbgX4Su/Tv6KCVYxABEBAAGI\n" +
                            "certif:         nwQgAQIACQUCUOtIJwIdAgAKCRD8JFDfpI52spqaBACUfqolAt+ubV5+9hlF9RuD\n" +
                            "certif:         oE0B/OBmB/YwdNIs90s/zBwdiC8F6fB0dMJS0prFfOIJHCoMP6cSLUX83LjimNUk\n" +
                            "certif:         b6yYrNaFwAacWQaIA4lgw6GEsvo9tT0ZKZ7/tmcAl0uE3xJ5SyLMaJ5+2ayZ4U6O\n" +
                            "certif:         6r/ZepAn4V0+zJAecy8BabQaUmV2b2tlZCA8cmV2b2tlZEByaXBlLm5ldD6IuAQT\n" +
                            "certif:         AQIAIgUCUOtGSgIbAwYLCQgHAwIGFQgCCQoLBBYCAwECHgECF4AACgkQ/CRQ36SO\n" +
                            "certif:         drJJnwP+KU0nu8SfDb60Vvmhv2NceH5kPeAHJY3h4p6JSvo5b+RwjhxVQ2j7j4t2\n" +
                            "certif:         du9ozj+DrCpaQ4WfOttwg+wgFOSxhcV6y/o60BZMXCYf3DSP0wQiIC/w4vAQq1+U\n" +
                            "certif:         bNfDnGKhvJp7zob2BLTlpTi16APghTjnIXMVuUFfjFqURaemVT+4jQRQ60ZKAQQA\n" +
                            "certif:         1lvszl35cbExUezs+Uf2IoXrbqGNw4S7fIJogZigxeUkcgd+uK3aoL+zMlGOJuv1\n" +
                            "certif:         OyTh4rQfi+U99aVHQazRO4KSFsB1JjmlizRBkHtRJ5/4u5v8gzUa92Jj1MXHs0gS\n" +
                            "certif:         qQ0cCdRUMnZxcgg+4mYslUp2pC/vzk0II2HEnSQa/UsAEQEAAYifBBgBAgAJBQJQ\n" +
                            "certif:         60ZKAhsMAAoJEPwkUN+kjnay+jwEAJWGJFkFX1XdvGtbs7bPCdcMcJ3c/rj1vO91\n" +
                            "certif:         gNAjK/onsAzsBzsOSOx2eCEb4ftDASLmvnuK2h+lYLn5GOy0QpmCsZ37E3RcnhZq\n" +
                            "certif:         uKUMNY9A83YE8MV8MZXzds4p6XG1+YR7bP9nmgKqsLG9stCPAugVQqxVBbcQRsRV\n" +
                            "certif:         dnTzEonl\n" +
                            "certif:         =fnvN\n" +
                            "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                            "mnt-by:         UPD-MNT\n" +
                            "changed:        dbtest@ripe.net 20120101\n" +
                            "source:         TEST"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("The supplied key is revoked"));
        }
    }

    private String getResource(final String resourceName) throws IOException {
        return IOUtils.toString(new ClassPathResource(resourceName).getInputStream());
    }
}
