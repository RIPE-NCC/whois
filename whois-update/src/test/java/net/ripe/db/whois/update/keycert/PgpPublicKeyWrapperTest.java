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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PgpPublicKeyWrapperTest {

    // copied from GnuPG source ./common/openpgpdefs.h
    private static final int PUBKEY_ALGO_ECDSA = 19;
    private static final int PUBKEY_ALGO_EDDSA = 22;

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
    public void pgpFingerprint() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

        assertThat(subject.getFingerprint(), is("D079 99F1 92D5 41B6 E7BC  6578 9175 DB8D A8D1 6B70"));
    }

    @Test
    public void isPgpKey() {
        assertThat(PgpPublicKeyWrapper.looksLikePgpKey(pgpKeycert), is(true));
        assertThat(PgpPublicKeyWrapper.looksLikePgpKey(x509Keycert), is(false));
    }

    @Test
    public void isEquals() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);
        final PgpPublicKeyWrapper another = PgpPublicKeyWrapper.parse(anotherPgpKeycert);

        assertThat(subject.equals(subject), is(true));
        assertThat(subject.equals(another), is(false));
    }

    @Test
    public void method() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

        assertThat(subject.getMethod(), is("PGP"));
    }

    @Test
    public void owner() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

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
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(RpslObject.parse(getResource("keycerts/PGPKEY-MULTIPLE-SUBKEYS.TXT")));

        assertNotNull(subject.getPublicKey());
        assertThat(subject.getSubKeys(), hasSize(1));
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
    public void isExpired() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
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
                        "source:         TEST"));

        assertThat(subject.isExpired(dateTimeProvider), is(true));
    }

    @Test
    public void notExpired() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(pgpKeycert);

        assertThat(subject.isExpired(dateTimeProvider), is(false));
    }

    @Test
    public void curve_25519() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-2424420B\n" +
                "method:          PGP\n" +
                "owner:           Test User <noreply@ripe.net>\n" +
                "fingerpr:        3F0D 878A 9352 5F7C 4BED  F475 A72E FF2A 2424 420B\n" +
                "certif:          -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:          Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:          mDMEXDSFIBYJKwYBBAHaRw8BAQdAApW0Ud7TmlaWDeMN6nfxRSRbC5sE8U+oKc8Y\n" +
                "certif:          4nQWT7C0HFRlc3QgVXNlciA8bm9yZXBseUByaXBlLm5ldD6IkAQTFggAOBYhBD8N\n" +
                "certif:          h4qTUl98S+30dacu/yokJEILBQJcNIUgAhsDBQsJCAcCBhUKCQgLAgQWAgMBAh4B\n" +
                "certif:          AheAAAoJEKcu/yokJEIL4cwA/2jOgBV0OHylbE5iDx1VIoMnIJCS12JODgwqwMms\n" +
                "certif:          rbVfAP9RAtdxNqdt/Bwu5mX6fTSGSff6yicqzGretWlkRh8aBbg4BFw0hSASCisG\n" +
                "certif:          AQQBl1UBBQEBB0Dx8kHIOKw/klxcIYcYhY28leG80qnPMgP4wgRK5JpefQMBCAeI\n" +
                "certif:          eAQYFggAIBYhBD8Nh4qTUl98S+30dacu/yokJEILBQJcNIUgAhsMAAoJEKcu/yok\n" +
                "certif:          JEILEp4BAJLGZuQ5qJkl8eqGKb6BCVmoynaFTutYbm2IIed6pmDJAQCa7CqeUY1V\n" +
                "certif:          duNXCkPvStUGG6dIRgtWlW7vwSVwgnd3BA==\n" +
                "certif:          =GGwH\n" +
                "certif:          -----END PGP PUBLIC KEY BLOCK-----\n" +
                "admin-c:         AA1-RIPE\n" +
                "tech-c:          AA1-RIPE\n" +
                "mnt-by:          UPD-MNT\n" +
                "source:          TEST"));

        assertThat(subject.getPublicKey().getAlgorithm(), is(PUBKEY_ALGO_EDDSA));
        assertThat(subject.getFingerprint(), is("3F0D 878A 9352 5F7C 4BED  F475 A72E FF2A 2424 420B"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void secp256k1() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-2424420B\n" +
                "method:          PGP\n" +
                "owner:           Test User <noreply@ripe.net>\n" +
                "fingerpr:        33A3 9E9F 3515 31CE 6990  4F66 BAA5 1A80 B9FD 9E0E\n" +
                "certif:          -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:          Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:          mE8EXDSMoxMFK4EEAAoCAwTxz0PIDQKoET5UHIzIm5qDVjX9gmrtyDQoZVwv4Slt\n" +
                "certif:          eXzopMSdXYXDbnVhA5ym79c6LVayNVnH//Gq1VNg9OlGtBxUZXN0IFVzZXIgPG5v\n" +
                "certif:          cmVwbHlAcmlwZS5uZXQ+iJAEExMIADgWIQQzo56fNRUxzmmQT2a6pRqAuf2eDgUC\n" +
                "certif:          XDSMowIbIwULCQgHAgYVCgkICwIEFgIDAQIeAQIXgAAKCRC6pRqAuf2eDlWWAQDK\n" +
                "certif:          iE5XJnPkT/Lt/cdvN9kAg9UHf9LfVc5fMIuyn5BHHwD/dmKogsVpKmQqkqyYgZLD\n" +
                "certif:          pcCPtZM2VnGFbQx8XoKHarw=\n" +
                "certif:          =uRm2\n" +
                "certif:          -----END PGP PUBLIC KEY BLOCK-----\n" +
                "admin-c:         AA1-RIPE\n" +
                "tech-c:          AA1-RIPE\n" +
                "mnt-by:          UPD-MNT\n" +
                "source:          TEST"));

        assertThat(subject.getPublicKey().getAlgorithm(), is(PUBKEY_ALGO_ECDSA));
        assertThat(subject.getFingerprint(), is("33A3 9E9F 3515 31CE 6990  4F66 BAA5 1A80 B9FD 9E0E"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void brainpoolP512r1() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-2424420B\n" +
                "method:          PGP\n" +
                "owner:           Test User <noreply@ripe.net>\n" +
                "fingerpr:        5F36 A717 5CE1 76D3 2564  A822 2FF6 9819 34A6 07E5\n" +
                "certif:          -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:          Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:          mJMEXDSNxhMJKyQDAwIIAQENBAMEo42EFGwrUkORmJqVGP76Gf0vSrN0gfk/3BSr\n" +
                "certif:          Ts1CV+vni7q0x4+BRJpqKITBtDi1uK7cBe0W1IRNbri0SZ9xgzT677+afPpgADaf\n" +
                "certif:          56r3+ISgEPZTmV0xgHwPrQ3g6QqebC0JjzXfHzxRghQgfX2R/ySbcnddOow0lt8y\n" +
                "certif:          /pSf7Zu0HFRlc3QgVXNlciA8bm9yZXBseUByaXBlLm5ldD6I0AQTEwoAOBYhBF82\n" +
                "certif:          pxdc4XbTJWSoIi/2mBk0pgflBQJcNI3GAhsDBQsJCAcCBhUKCQgLAgQWAgMBAh4B\n" +
                "certif:          AheAAAoJEC/2mBk0pgflPPgCAI2FvUH4rn3/pAuVm2i71I2G9AqSsVR9B5YhVgxb\n" +
                "certif:          PZwOdLnpApBsr+L6dF7h5sMXgpgRjYsruv5No63NW3nT4ZsCAKXKkq4yScqGn7Jc\n" +
                "certif:          T4GD+5v3/aAiZZaTiRAHjXTLt9+OxZUAbZ3j/AMzIG/vjP4yJpoJ9ekteYghzK2A\n" +
                "certif:          ylYZa0o=\n" +
                "certif:          =SE10\n" +
                "certif:          -----END PGP PUBLIC KEY BLOCK-----" +
                "admin-c:         AA1-RIPE\n" +
                "tech-c:          AA1-RIPE\n" +
                "mnt-by:          UPD-MNT\n" +
                "source:          TEST"));

        assertThat(subject.getPublicKey().getAlgorithm(), is(PUBKEY_ALGO_ECDSA));
        assertThat(subject.getFingerprint(), is("5F36 A717 5CE1 76D3 2564  A822 2FF6 9819 34A6 07E5"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void nistp521() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-2424420B\n" +
                "method:          PGP\n" +
                "owner:           Test User <noreply@ripe.net>\n" +
                "fingerpr:        75B5 6A59 4D66 C09D E50A  183B 0862 8883 725D 9FA9\n" +
                "certif:          -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:          Comment: GPGTools - http://gpgtools.org\n" +
                "certif:\n" +
                "certif:          mJMEXDSOkxMFK4EEACMEIwQApCgTDFyM4TVCGD+LxdzTH/hQlgnfopR+ovy9hwfy\n" +
                "certif:          NERZoCZ5cxzJbz8O95CQwTU2xwtnqjAY7+Rz/yL48yAXl74A59E7NQUoefhk0RCo\n" +
                "certif:          mmWZ9fQLLxcDIKmcCyL4uctMlBYUGOG8N9JRO27eWbKDq3uinjeA9IBgut+fy8Lc\n" +
                "certif:          O7qgFEK0HFRlc3QgVXNlciA8bm9yZXBseUByaXBlLm5ldD6I0wQTEwoAOBYhBHW1\n" +
                "certif:          allNZsCd5QoYOwhiiINyXZ+pBQJcNI6TAhsDBQsJCAcCBhUKCQgLAgQWAgMBAh4B\n" +
                "certif:          AheAAAoJEAhiiINyXZ+peD0CCQFd6H/nOZGGya/oPsGBJ6sYMkshYo1P1a0RFJgV\n" +
                "certif:          51JGmKdhiFm1gAvqeyHPg+mgMbUcZ+hShWquxkYQ/YOtIh39OAII/K7OR/5Mrvrv\n" +
                "certif:          482unlxDRkyBvGxYQhJWjyEITXeaQ5voIu9LmkVrDVZFKK+aBRYjMp7BOWTXdZeC\n" +
                "certif:          kGmktixbUx8=\n" +
                "certif:          =2ATM\n" +
                "certif:          -----END PGP PUBLIC KEY BLOCK-----\n" +
                "admin-c:         AA1-RIPE\n" +
                "tech-c:          AA1-RIPE\n" +
                "mnt-by:          UPD-MNT\n" +
                "source:          TEST"));

        assertThat(subject.getPublicKey().getAlgorithm(), is(PUBKEY_ALGO_ECDSA));
        assertThat(subject.getFingerprint(), is("75B5 6A59 4D66 C09D E50A  183B 0862 8883 725D 9FA9"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void unreadable_keyring() {
        try {
            PgpPublicKeyWrapper.parse(
                RpslObject.parse(
                    "key-cert:        PGPKEY-2424420B\n" +
                    "method:          PGP\n" +
                    "owner:           Test User <noreply@ripe.net>\n" +
                    "fingerpr:        3F0D 878A 9352 5F7C 4BED  F475 A72E FF2A 2424 420B\n" +
                    "certif:          -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                    "certif:          Comment: GPGTools - http://gpgtools.org\n" +
                    "certif:=20\n" +    // adding a quoted-printable space causes the parsing error
                    "certif:          mDMEXDSFIBYJKwYBBAHaRw8BAQdAApW0Ud7TmlaWDeMN6nfxRSRbC5sE8U+oKc8Y\n" +
                    "certif:          4nQWT7C0HFRlc3QgVXNlciA8bm9yZXBseUByaXBlLm5ldD6IkAQTFggAOBYhBD8N\n" +
                    "certif:          h4qTUl98S+30dacu/yokJEILBQJcNIUgAhsDBQsJCAcCBhUKCQgLAgQWAgMBAh4B\n" +
                    "certif:          AheAAAoJEKcu/yokJEIL4cwA/2jOgBV0OHylbE5iDx1VIoMnIJCS12JODgwqwMms\n" +
                    "certif:          rbVfAP9RAtdxNqdt/Bwu5mX6fTSGSff6yicqzGretWlkRh8aBbg4BFw0hSASCisG\n" +
                    "certif:          AQQBl1UBBQEBB0Dx8kHIOKw/klxcIYcYhY28leG80qnPMgP4wgRK5JpefQMBCAeI\n" +
                    "certif:          eAQYFggAIBYhBD8Nh4qTUl98S+30dacu/yokJEILBQJcNIUgAhsMAAoJEKcu/yok\n" +
                    "certif:          JEILEp4BAJLGZuQ5qJkl8eqGKb6BCVmoynaFTutYbm2IIed6pmDJAQCa7CqeUY1V\n" +
                    "certif:          duNXCkPvStUGG6dIRgtWlW7vwSVwgnd3BA==\n" +
                    "certif:          =GGwH\n" +
                    "certif:          -----END PGP PUBLIC KEY BLOCK-----\n" +
                    "admin-c:         AA1-RIPE\n" +
                    "tech-c:          AA1-RIPE\n" +
                    "mnt-by:          UPD-MNT\n" +
                    "source:          TEST"));
            fail();
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("The supplied object has no key"));
        }
    }

    @Test
    public void isRevoked() {
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
