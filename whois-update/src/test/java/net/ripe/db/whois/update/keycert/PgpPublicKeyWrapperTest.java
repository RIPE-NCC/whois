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
                    "key-cert:       PGPKEY-14C1CC04\n" +
                    "fingerpr:       ACBB 4324 393A DE35 15DA  2DDA 4D1E 900E 14C1 CC04\n" +
                    "certif: -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                    "certif:\n" +
                    "certif: mQINBFJmBYoBEADnhThTFgOpQwBkgJVFOoP6UQ3oBZTcRSYTx3A4pcQi4yziISVO\n" +
                    "certif: g9E9fh2YeGweAZAPRKrevxQ82lv5kDv8UvlCOnEh9vH4lpfDagiriEAkcTc7yUmC\n" +
                    "certif: JAyR8yg5Km7V01fvbLTWIZisf5EqwAwVdkIaen5CDYxxfay7v4jBGXxNwdQu4nsM\n" +
                    "certif: OGuYN3Tl0FBlWif0MyhGTyhl6rMT2llyKnlJaHbCoAspXvN1RXWtqo/bGrcom3Ff\n" +
                    "certif: Nmf3bD/8KxC58DmBLnhnDeau6tohSP8h3xr86CxdYb1Xz8E5IRVmZoo/NLM0bG1h\n" +
                    "certif: HkiURHiJqo36RoMarFEhCfyoHwv98HzueWV1Vw8ZWcZHMuHIskT//eI2ncUn3BzT\n" +
                    "certif: 3ou3eSx6LZoS4NdKtUKqUyT82HDXY9cUgLudDA/d2YUi5By2YvgGXFROmKjtqOTj\n" +
                    "certif: Uqu5k0BTAOjfX5/IVBA+z26XYJgI01bWivD5PI4thtJd88nX28evu6SwN0ev/d1T\n" +
                    "certif: T7+rGs0WMiofWLnpe+K1287JuQ+WpVhf9/UfccRbL5rYX5TQpxYdMzJvy1TxHinz\n" +
                    "certif: E+jEcEkY0HVBhOtr/ca56JViOlAQi9Ag47SbsDK1Cg8B2hO4MoEVupkBF92RHREQ\n" +
                    "certif: 1+RhnC8cHX5IvE9Cb4tYP3Dbyd/ZUWQutbbrCoaLPwzu6n68yI4JvQCjnQARAQAB\n" +
                    "certif: tChQaGlsIFBlbm5vY2sgPHBoaWwucGVubm9ja0BzcG9kaHVpcy5vcmc+iQJvBBMB\n" +
                    "certif: CABZAhsDAh4BAheAAhkBFiEErLtDJDk63jUV2i3aTR6QDhTBzAQFAlixMIMDCwkH\n" +
                    "certif: AxUKCAQWAwIBIhhoa3A6Ly9oYS5wb29sLnNrcy1rZXlzZXJ2ZXJzLm5ldC8ACgkQ\n" +
                    "certif: TR6QDhTBzAQ+HQ/+KF+eWjuOUSxp3MThe4vq3mIImob1C1ofhaOvF7GI4JxOAqvF\n" +
                    "certif: FvkRKzW3qVJyHjfR2zbexlW+0qfaXsMmdHAqskq0lzUC6FpriAl6bHNnZa3maUsY\n" +
                    "certif: CA7dC0Yvvyr3mGWAqwuegocVhVJLNeEkftvaoH25eptXtPNEh9xPiKVUP0OCSx/Z\n" +
                    "certif: ZnxZmZOPLCMK/fq5sCTsiAFc+vgnCXqE5IN/za6ocXRpT4Zv2Bkk572Nurp/PeKZ\n" +
                    "certif: eH9b8GpTkcPg8wyKtUlBpMqsGQVyXBOwHOuUZZiMh1Xw+8aTaZIdpoC4W0C+aPtP\n" +
                    "certif: g1h6ny1AIjVQ+nEveUefeoIwkT11YVZc/5/mXG10PtS7UN0YIkcI1c2JhTPMPSZ7\n" +
                    "certif: b8Pm4LLIFu0noaQNGSZdLdJZ7+6AUTw89QXKbD9gsvhI6e+WH+LME4yFMnQTLC4A\n" +
                    "certif: KzqLkNWob4cU8t/RLnaR6NFJsV7yWgRK5+kPBe4DaUPkIVDGWx66V/2IPrfvyrI9\n" +
                    "certif: 9OJTa/MN0FEdoHd1fL49EVt46yvPZxWgi8Zfcqj5xxK7/L178Dj1XD5ipEX4rMFc\n" +
                    "certif: MrpiKdM91aIhYrG8vOwEL5A648Joa459Kurz8MzKOqIEPoOrPM+xnEEQ4vNBbW9B\n" +
                    "certif: BW1Qe2n5IK7ZU1wCHpB6DfZHjKJgptwDQ3Vub9VRzXt5PZA9ijTWWrIPDV60J1Bo\n" +
                    "certif: aWwgUGVubm9jayA8cGhpbC5wZW5ub2NrQGdsb2JuaXgub3JnPokCbAQTAQgAVgIb\n" +
                    "certif: AwIeAQIXgBYhBKy7QyQ5Ot41Fdot2k0ekA4UwcwEBQJYsTCPAwsJBwMVCggEFgMC\n" +
                    "certif: ASIYaGtwOi8vaGEucG9vbC5za3Mta2V5c2VydmVycy5uZXQvAAoJEE0ekA4UwcwE\n" +
                    "certif: bOgQAL9E4Mxh3ESYMuZ7chEdqIv+zXibgVTVMuFvIswTblLvjKx1SIr7e2H96hZ/\n" +
                    "certif: /fuTYvFmRgEnn1LnZbpBiTkEsKYp5Ls+Kfb2ViWweSGu+ucB/+0TDuUO4fPkiiYe\n" +
                    "certif: A3k68sqC5s7EM6+IaMcoxsQSqJC2CqvN/MMao1czxPnIU/O88EHNMBciJ5QawRM6\n" +
                    "certif: UMLR98RZSvGSNAM3NsMZzXsp1S5Bzenx1ZUqcPjI/NVYsURFWXF3nCJglQLY0nCi\n" +
                    "certif: hwgaDAWuQne9JGNwaZA0ZXogaFe/rNDqXvwAc5o2HvLs5UN+1aNVRy6vQcUn7dXV\n" +
                    "certif: W8HXPXVZskfmbIIJGDM/duW2E/OyBVl/6uSbxy5qvB7N9MarGtjl+fSokSJdDizb\n" +
                    "certif: hiRpv4ds3NUb6tPuEjx7aoXf/wMYPczqUMJ/96b8/M45ZVTsvZQQ5ZarHwXiQq6v\n" +
                    "certif: ZGBrnoGMeq3T3zLUKmlx984F7vTj+ySPNQ/Jdd9M5uh08HYCRZfPSKTKezZWvSlQ\n" +
                    "certif: /jWz3juFn3J9fyer8Ij67AvrGmXdi/HKvobdbpxzKewR7NiMKoZtOxHEMsHOxbKo\n" +
                    "certif: Ojg4TyhnTk7JQ52pffjhqzhaYndHCux8TzpDpkXdwJybAq56Atq5/h9BHY4XR5cZ\n" +
                    "certif: /dvcNao2pKGEWccl6QPLQt1PR0oRRuyfg7layXYtTO1xxjzytB9QaGlsIFBlbm5v\n" +
                    "certif: Y2sgPHBkcEBzcG9kaHVpcy5vcmc+iQJsBBMBCABWAhsDAh4BAheAFiEErLtDJDk6\n" +
                    "certif: 3jUV2i3aTR6QDhTBzAQFAlixMI8DCwkHAxUKCAQWAwIBIhhoa3A6Ly9oYS5wb29s\n" +
                    "certif: LnNrcy1rZXlzZXJ2ZXJzLm5ldC8ACgkQTR6QDhTBzAS6JxAAjPxTWUqkCZJ1YG4W\n" +
                    "certif: Po/q7zvuensvPi8bSR+LqATWyyiWvDND1TplUYl2WPYcBBfiBJC/mKwEzNYLnNbu\n" +
                    "certif: iBLliEUihIx7NVmumDVo687tkyrT/0tua5OmjWLdwIAzm8vZctyNJhLwKtGPOsp4\n" +
                    "certif: B0Q8d93mZU/7tlLFiKxUlddSkoZcxiL9/vGV7aD6vMk90Py6bdQYXsiDHJx+CvK8\n" +
                    "certif: 4hELq1eotbPq6sg5UVqP9zRCZe/yIP7sguxX1v8pG4QuWsVx6iwHgyyf0oEXlJ3v\n" +
                    "certif: ZRJ9HtPz2cuMf6+jDMQUtbf6NiSEv/G2gshV+J4bo7eTk9ugfMw+AjHBVV0ngc73\n" +
                    "certif: fkz91PsjAHa090XNw4nGktvkS91B7rlqtKJvO3mFA4cW8rkBniOxHc0g3tikrAiI\n" +
                    "certif: K25KWbqlQMkjtXD9X385bYcHUzSho1bonc3e21XDsg0j6cWcKwGOwCNcfpyrlyYX\n" +
                    "certif: B6EGQ0SP9/D9ThvbfSkvCzII1l4xHHJPXgfebZbtUXpBznSOYXVXp9+SlMjsemnw\n" +
                    "certif: TQMso4/z7vBcO54hOIObD1Ae/XOkn6TNKju3tC9smCXiI95RDYr5nAQMc+k6zu/H\n" +
                    "certif: dfB98Gwlf/EprSVciyNODP+7igAFcQ88aZdwWsDxO6NonEY+Yuzg9FqNHj/DecRo\n" +
                    "certif: Zkg6eB/OsFRag5paajR9d5Xgvi60LFBoaWwgUGVubm9jayA8cGhpbC5wZW5ub2Nr\n" +
                    "certif: QGdydW1weS10cm9sbC5vcmc+iQJsBBMBCABWAhsDAh4BAheAFiEErLtDJDk63jUV\n" +
                    "certif: 2i3aTR6QDhTBzAQFAlixMI8DCwkHAxUKCAQWAwIBIhhoa3A6Ly9oYS5wb29sLnNr\n" +
                    "certif: cy1rZXlzZXJ2ZXJzLm5ldC8ACgkQTR6QDhTBzASmoxAAyeIZhP6Eh9mz4hzdAlJe\n" +
                    "certif: vJo/Wo9yGcn/Jck5AgOU6O+xCd9K5dpexWNDzSdscSFS1mrMbmekX/HYRAICjDjV\n" +
                    "certif: Ec+lsT5nSbAkef8zeHuH4imG+Ytawa3s1nbGH38ja1/4lN3bXY8YNbqpHk7of11c\n" +
                    "certif: Xbeji+Z/CbtsTLY0DcKmmbEFE0HYGDv53FKWGWixtT6qgQcsNzBbPLw32K1RN61o\n" +
                    "certif: DenAiglLKOM011O9LavEKE0fmYBLIv+GrtVsB3NbCRPmMA4J3P4yC5AdREVpjos/\n" +
                    "certif: 2C9YjqKq2pW877zDiWghu7aLREH/B4PLBCMOna/R9ydPR+MIPamOiqwV8676zWKK\n" +
                    "certif: qxT/XZX7EqNo9PsM2P7HLVkQN248s1Yvn6+e6DhBJSLUET4pFF8Pi089AwyoX2nm\n" +
                    "certif: pqacEAuuCuFykuIm+0dNOIOvy6mx8BM9/+wdNTACDhO00f08Idt+lvUCwV39A9VN\n" +
                    "certif: RAyy/qkynBd/vdC2m+0RWtxxk7UVhHI5yCiHQUzV9fofDUo/FzFAWtRPToYHT5e9\n" +
                    "certif: gMQSPaBVR9JjU2NHtsVjQRP7SK/fhaz0g/VDZna4ZxdbR+QrpVuNRpljXU0E16oM\n" +
                    "certif: caFiaQDore/hUqjLle6PRGlL6fgt7vSH0qIbFxrRDqixFzBX8LJ3N3t3o3/oylj6\n" +
                    "certif: Ndvb59wAuAWS/3hzhW2JXFq0G1BoaWwgUGVubm9jayA8cGRwQGV4aW0ub3JnPokC\n" +
                    "certif: bAQTAQgAVgIbAwIeAQIXgBYhBKy7QyQ5Ot41Fdot2k0ekA4UwcwEBQJYsTCQAwsJ\n" +
                    "certif: BwMVCggEFgMCASIYaGtwOi8vaGEucG9vbC5za3Mta2V5c2VydmVycy5uZXQvAAoJ\n" +
                    "certif: EE0ekA4UwcwEVfMP/2AEkVsFM9/j/kqe4OBn026EQwTnrcJbqaTrkVq/mpmaNn8L\n" +
                    "certif: 6VT9E2ug89C2V9SVSAUr7/Bd0R3pdJg4H2ktIFUsN6wjHzSbiXaTr+6LeR5CbkuD\n" +
                    "certif: L1RB+XOW1j27NPdxmg7XaECun3EQbdOWxRDt6NkI9uK4ewLadyTFim/q23oDcTiG\n" +
                    "certif: vjGMEXllCYMWKsnpZz98sz9Kht1ZnMa6xa/aL92K8oj3ZaOVzDbK8Sk1Fg+5sXG5\n" +
                    "certif: kMEFVGVgv2R9q+BjLBkAo1Hxd3ND+suHK0y+UMfNUQeCAl2i+a9V6+AwjCuppEUG\n" +
                    "certif: F4MRePSGOT/heGbxt4MPAhq8l94EW+PWyrTTRHIrntPMw6xWMSuBLqL7pDN2jUNw\n" +
                    "certif: LG1Ro5j+SAxIcJluiL0KAxMs64ZUe9PojlkzvtLS5Fg77g/KmGjxh0i4x4okKM7K\n" +
                    "certif: GgQO+Cmq/Nwmp/sfgJm3Iir7yO5Dvr8A5omwZkmY7RlCXOwAKc1cwAFsZG8ML95t\n" +
                    "certif: BK8Zunm0WfB7yiNqJuiA3eLkrJSFK9cbHaoS4yPfmN0mNG4jS0sLzV4Zh12pu9+U\n" +
                    "certif: DJyjrC4wCnQiaTnEfWyWrVL0dOaYIj0RtHRyAipFaYBbSvbMHBCE3mZowzhVgpmz\n" +
                    "certif: Xx7wnb5naWbA3G4QzgKlVf6tsUzgaEsq3YUboKovCUIzGqdLa5BLFmC7jdnztBxQ\n" +
                    "certif: aGlsIFBlbm5vY2sgPHBkcEBnbnVwZy5uZXQ+iQJsBBMBCABWAhsDAh4BAheAFiEE\n" +
                    "certif: rLtDJDk63jUV2i3aTR6QDhTBzAQFAlixMJADCwkHAxUKCAQWAwIBIhhoa3A6Ly9o\n" +
                    "certif: YS5wb29sLnNrcy1rZXlzZXJ2ZXJzLm5ldC8ACgkQTR6QDhTBzAQy1xAAkz0qdsS0\n" +
                    "certif: GgOQVlhiaEIruL9yTNecEdiHKsKqO6zSN6+qLGpO1lBerE+lMrflCqSRril5L0o5\n" +
                    "certif: V1ZUrh9GlTjNh7pb3WTxHusHQ3j1jNuw4zkuJ81isvas7YZh1aT9Lglm5vKwcg5D\n" +
                    "certif: kcwkvC1VOOljs2Rt0dt5+rEpf09/WQRdy704dOcnTcWqz09RmP1xBTjH8rMMWlmo\n" +
                    "certif: f8wzqrle6YQULCj0WdfBIbEClbuk461frjnJ8wKNVbD7OOnPimGcgEFXS8CLrGYj\n" +
                    "certif: 6o19utkVg02aOEmCETnytA9wjXozocHW9y5aOTo5eAWdbj6kSZdFli8C1NQbIZyc\n" +
                    "certif: Z4w5wrCOLmIPUZyrfaGkl2rEfeRDUdFmQhocSR0RcXEifL4/OdLy4c0XjAtHI7Rw\n" +
                    "certif: niyMBqVQQZ1P1vU5R5zBIyAIhstYHxSNhkF7mYhALwLe2R6K3/K89sZZk+Wroog/\n" +
                    "certif: J68RnLMMV5wNLYl+556tvMZV/X/gkINu5YBC/VP/ddtlYJB8K8W9F+c1aCyigS2R\n" +
                    "certif: LiBeNn3486kKPAdisbMr+ci8GT6/G13lE4Yus9hDWWLz035leMEs/RwavqSjJJyT\n" +
                    "certif: A96B4jUsoYFgf1Di7ca3KlNfHQro47sFzKyIOComHJLrW8jrE3YX0qytS2zYISGB\n" +
                    "certif: Ti8+zEGLfkH0QjlCmfgejBfKGMOfGOoSnVS0JFBoaWwgUGVubm9jayA8cGhpbEBw\n" +
                    "certif: ZW5ub2NrLXRlY2guY29tPokCbAQTAQgAVgIbAwIeAQIXgBYhBKy7QyQ5Ot41Fdot\n" +
                    "certif: 2k0ekA4UwcwEBQJYsTCQAwsJBwMVCggEFgMCASIYaGtwOi8vaGEucG9vbC5za3Mt\n" +
                    "certif: a2V5c2VydmVycy5uZXQvAAoJEE0ekA4UwcwErPoQANIM/ld/TjvQX7NBlZgZYqKV\n" +
                    "certif: guLfEIM0e25GnR4EyRE6q0xSxN9GcXZBEEvaVuVQ5TL5r1gSVOVdTiMYqSGrN0KX\n" +
                    "certif: SVdJQW2FOy9izKkJkXuYAV7tOASuYL1r3v/qODspkGCu3yS0PxnySRE2+UTl1jdm\n" +
                    "certif: awJluiqW8XzqZXdXBsAqT6DeivfzoLURsevrAMg6uxQgnrYWi0DgpJ1KIfG9XJiQ\n" +
                    "certif: TSAtWeEoNPJoKMesezF2g91TCgLZ1EbBJGNFVRj/EJU04iEzcq1OzCswiA7YBNWG\n" +
                    "certif: XyWw5wipn7y7Ylpg3JJTzaCiK4RiDCcFQzI3lzyLA3C1hsjqpGL6n+Ko1B6ftfYP\n" +
                    "certif: LvTrsPSZ4ezSaiZ9fPkHveTSAsmAm8yDrMOHv9nJjREZ3ClRCrGV+GZSifzacObZ\n" +
                    "certif: l0vg/lCB1rQWSqGJaWPSquk7oht3rPxI6q+6YCInlJRRaaFvelm0R9dttXrzLaIE\n" +
                    "certif: mN421WYV/oJN4N715ThkyU7cOq2f8tkKdJMYOanAfa+84JR30pZA3wZyGWxzcMs2\n" +
                    "certif: f+mKY2HLY55TzgehVTA7ubSrKMkF62MrU89cEai90KrBsNdCB8jIbzr6B5tA8nid\n" +
                    "certif: UYCZvErk74nzjCbv8geEU3h0fWTHtWq7ZOdp/BCYfLRyqS4OB2dYkHgA78waek8L\n" +
                    "certif: rn+wWIa6wvj0h7FY0S4quDgEV8EwpRIKKwYBBAGXVQEFAQEHQLd1cWAnTq1fuT96\n" +
                    "certif: N/X6DyW1BPeyZAmbB/0i1+3eFit1AwEIB4kCJQQYAQgADwUCV8EwpQIbDAUJBaOa\n" +
                    "certif: gAAKCRBNHpAOFMHMBOauEADf+cfzf/IJXXxsGqywi67R8sGctCTMCWXGY09M+ZgQ\n" +
                    "certif: U8DbD9SG9pPbRqgHqkfdiGLgBCSO1GDSfGhDGww23zrboXnELqX1x8gGoS6ykmgl\n" +
                    "certif: 4+PZv58AUBgxEg0m2jlP4/WEsMKxtrxWREsIU1HYSYWvlKZXt77Y9x+yR/gr3Qrn\n" +
                    "certif: Izk1kqXZdJ3/+uMDx26LPkn3kvOtlIyyZ0upKuBgPk/FsoebbWM4aG3TWUkYdWlO\n" +
                    "certif: XJUDRS4XvjvBlk2PNQCP7d8E9Fi7Gr4VGtwqlTyTOr8xwU+OsHsopj9KqB0aaNbH\n" +
                    "certif: ydgOP2BnP/q/HMA4ph6RvZiK9ht58dWfBYV8Y0DeJNUBaTCfC8h6zi0taCKZjZsF\n" +
                    "certif: s1Q17kUKHq116jjutahGqK0ItG7kemN7k9Pg4Zxpm12L8tOyGi5cpHwBOvwglJCK\n" +
                    "certif: OQTQQZtLKQoQLndx4a/ebi5nstCbxBl3eJI1PukAp2NKSP/9j8dwZtlfXdaNft9X\n" +
                    "certif: a/HG4eUZIW0/aq4PuUCDnlht0rDkKioTmitZeVaXZaUjKBNTicVDk07AOUXA1pW0\n" +
                    "certif: guoUEfvAJ1KRpwfXmQ7U7AFc83CX4qYaEIQm9Lco4MnoqEkj8gNklv13vJZPbMKB\n" +
                    "certif: uXitjyOaykPgL/3+72YBt6jF/Re2MJfwnsI3unFxWnjGzls5KzRkTemOypakEqWO\n" +
                    "certif: JbkCDQRXwTHMARAAugFDWrGPSG8PQdOuC9lOLn5WatzeNWVChHK4QCWWw4T5SmKi\n" +
                    "certif: zzdMcCa21BhQpJE2Rf+Y7Mv9mCoq9csIzT7r6yIKkcLsqLk3iTi0F3QJW2R2o1Uf\n" +
                    "certif: JY20HoXVcTCdpz2B8rsEQEE47WHHjAtgxXv2K3Vimvadse8/YQ+7MYNx68117QI9\n" +
                    "certif: AqfxeX6JEIOcZ6hqlMhI1ewB2o5Y7GqdGzmaSS5Fw4Q2LIDLTm1oLcmRyn5qSr91\n" +
                    "certif: 3RYB1Kcbvl1/IcOkPRSEtvAxDlURQsxrGOrakVDHwUp1L4/sd6Rf4FzTB9RyRe7u\n" +
                    "certif: J0OZ7pYZcsg7qV0OQTSBdKNWxCjDix3NE2e8ICZz72VxQdu3RP1OY1vN/XLtHuXl\n" +
                    "certif: Rqyc9tQMJ8CFEg5FRvqiBo0SFcrOHHuuZIIwjiZC+8kSQ+WAllu4Y09/yVN5d5QE\n" +
                    "certif: FcEum0eEIukLJdHipobI6pE+zdyg12wkqpgKfW/feG8WDnjcqLRbUADwplFRcpEG\n" +
                    "certif: AzCU1HZtImV+NKvKe0/joZFrZri+cRqT/SlOiDlh0cYHETe3FTTqKIoBPZqsIk5d\n" +
                    "certif: t+pMWOzUC4pReXczWxMbRUin9odml8lPgqWBNmmuhU3rTFMWZyNvqAdYRplRqBgc\n" +
                    "certif: 3ZYXV465/bmXYizrpkmlmdZrfxDDSVU+mjyD0gEpF8dUnu4PKQgltStjrYEAEQEA\n" +
                    "certif: AYkCJQQYAQgADwUCV8ExzAIbDAUJBaOagAAKCRBNHpAOFMHMBAalD/4yzh8GCXmi\n" +
                    "certif: bILIuSNr+Bz1EdJGWTawHq2luujvs5B8j3oYlNnXOCnrlgD9WGdOHI1MrrAjfUC0\n" +
                    "certif: seM22mRzyY6krS0KrGp2GfanXgFS4MadwN2RQLf9fQ5UjCH9HFpoNlWPZneBMdK0\n" +
                    "certif: 37+dHBp2gfAi6RGisMCSVZEht2H6oAW/9a41o+yrmzOJN0DUNlMBuAAbPpmdH+1W\n" +
                    "certif: KXfYUJEc2vCHDkP8aRiKDZcHlqjqzJvV5NVzdy0NklBuaT7OehRxlv0OIzdM6ghl\n" +
                    "certif: T7+vE/oo+dMP/yTn6y/fMfkDpE2B4RQVA7KpsrATu3XgXkO+9ka98Ki1aplclaL4\n" +
                    "certif: Ulp/q+kC1/jg/NKNiO2ySgIMSuANm5QXIyAOsyu6ypYec4rQAkbNwb5hmHpwsRee\n" +
                    "certif: Z+QU2OKLFRP/IhunN4uzS2RUbbGMGgPUwmGcTlCFvHH2Ytj8nzMNAJp3ZXWeUmrm\n" +
                    "certif: gAnxHXCHuyxOELur1M9ahK9AwhtflXrStDSnj0lzIP1Vd0U4uJlDl5r/6UqycQ0k\n" +
                    "certif: DDwN8ukHV6lLUJduZzOis4HDqRmofFDncg2XHCugd85atZ4ChPVNfXvxYGWplJ+P\n" +
                    "certif: YaijbukIUPtqYInoYESdTZqOosn/tDtdpMEx1oV7G+2W3wPOjh5T7hIxQw1uJF5D\n" +
                    "certif: khN0kUz/RmOcOx1WNalFsvtvxXmMluLMhrgzBFfBMHEWCSsGAQQB2kcPAQEHQCX+\n" +
                    "certif: QrQV1F1aohqbdcUODZcwbamprUoLIXV2nSyLiSx2iQJ/BBgBCAAJBQJXwTBxAhsC\n" +
                    "certif: AGoJEE0ekA4UwcwEXyAEGRYIAAYFAlfBMHEACgkQURBOZo3QRIH5oQEApL+jd0x4\n" +
                    "certif: w476elQ3F0M002NXd7FKouy9ageBM+oHS4kA/28oFXVcqDraz9f5bFnwX2407CDL\n" +
                    "certif: Kaz1Mp3jijvRp6IHqf0QAJGX5TZDr6CdERY5cOTCs70y02q2sVON6dxicE+UAxOL\n" +
                    "certif: 1nqpef2FiOobu4e+OAGYZngm8oNQBNNXA6ETc+Ug+zUVP5p3MwkpFAC52GWF9yqO\n" +
                    "certif: jOaZuQx1QZoWw/Whba2ix+rZSSi1zPaxrL8iQpe04Mt3IFwmLlxYhT2Z9uDF/lot\n" +
                    "certif: ROTW5PIcWmt2yHYbdL0XYrGp959DmKNGlprgTBbWTeuwaQUw/SOk8Oi83qv+8YdZ\n" +
                    "certif: NyuaxLz3qh0VSxx9vQnGMbslDpi2+hXOuTJuMVs7UtPtsFgZPOQIWdNC39otlpHQ\n" +
                    "certif: E6z8ezRlsOX7LFf+1CFPkPjbqrs0D4fOEr+yilo0Dj95ixnCe1lODykeEkwQE4Xd\n" +
                    "certif: lrIGjLOdi07Q/iMLTGQDL03PVrNXt1ak8pTJI7SZRsQulL3+Tqb52HynBDbuiwSy\n" +
                    "certif: jqdCGAZ/oRChWrW6tdg3bos9YiivPgIswCfry0tc3WGp2ygCbWFvgHZmxU8nwusr\n" +
                    "certif: TjciIh6b1xwG/webx/lBPMiKyRC+F1wLqdJBDo7bNuo3Gz8W733vBRw+DWSDPBeb\n" +
                    "certif: W7kk/k3JH0jX1MCdv+zO5wuur8DdqDLLlX2mZOTRZVJcGg4jQ4EJ29akJv5bSwa1\n" +
                    "certif: bFRXN0K2uNSGbtfHrcbDmg0aU5slv4vtsxeUFhOEcxHHCwCfgTy3SEF+cSlWWIbM\n" +
                    "certif: uQINBFfD2hsBEACqLMDpuA+/9VWscimKTs7+k0BiuxfPwNJAYYznAVNFt+GE464v\n" +
                    "certif: 6YJNXpKt07BRzDpuivaDPobqtFXc2nvBHcCUOP6QTUP89rOC/bw039B+KRaPlQJT\n" +
                    "certif: GbPKL/kqIXiK5ihjgSXdHDCmzNFHuec07pWgBMI+LYfZpKIHGsFVynIL53mmhxav\n" +
                    "certif: GTCSzJrBd6pyhoeCzMsIZAq6pZ0HKjfVWP7B3yBJfazCr2V/HkOmKV/vPJT+oflE\n" +
                    "certif: 4f+PP5tTuvEWE5UXM8VXnROMcxaNHLB43Pbh3A5neGgFm74Ha0tfWZHrZYnNCFRG\n" +
                    "certif: bxp7PnfbKL+tZ8xtyQr1pQ+x1y8Bkxj1MgiOj55MmRmjxlVJ+L6zyB5Tw7kqsaBH\n" +
                    "certif: iSDBWUz6SJz3pFD3X3GPD/nkNqhBhSzFM2qxHME3CkK+hU4jOEkcZpHhsjL+pXVu\n" +
                    "certif: dGNHIByDNj9lqP7vswg7cnGN7QIPdpBdvgcFg4qZS93LsLJlqhNDtCwd/Ut+QNT6\n" +
                    "certif: xE51HflZ+3/su9FEjUFKZMEtAu0TDoaf7iV9VyD84wjLWAm1GVXpDh1/WuSUBifM\n" +
                    "certif: fTyHXyLN2y2Ja5D1mws1g2ywzHBW/2e3gUzYSd4JQEWLYld0kZhQ5V/Y9Y19jDpD\n" +
                    "certif: UUgxkZmb5dnHRaGwmyx27zReKqN5NF2tdeWsUMibZkEQdib0n+WnzuJMYwARAQAB\n" +
                    "certif: iQQ+BBgBCAAJBQJXw9obAhsCAikJEE0ekA4UwcwEwV0gBBkBCAAGBQJXw9obAAoJ\n" +
                    "certif: EBPa2Zx+QVGcxvwP/2aIUD60sKExN2fLXj7mMZ/wWlDnCdqvTGD7lrk6r/fAQcaO\n" +
                    "certif: AgajCMEXOPZXlPBhdQ4jxD3FLs52CNZkcwzXMbspz1lfIOk2U1UGhmnAyriY4Uf5\n" +
                    "certif: cRu2RPR0HYwOBB0xr69SIrsmlX4pf1AnulE7CIY/oPBjB2XQRQ7ls8sMqmm+0TxR\n" +
                    "certif: ysaosHGu7Vbez5iKBm3p0rEh8TcVkgMivdUPue/ip+mCaDCfGeAiXLXWtiEiwaS3\n" +
                    "certif: Pq+QzHhZtBvShWlc3k2mCFlrGQwovPxY5SqGs6QwifrmnGSSlyaAorDZcQEkZe/H\n" +
                    "certif: P2/qXKb7uBD3/r8t2OE+BZKwJxW2fIpaO+u8k5EXSDzuxRqSNj3wYUI2+WNQzBmA\n" +
                    "certif: yOZ6XBX4Pz0xZyahtXCzJ+5deqCnEtJI1HdPSvM7STE6s6BmkhUl8weSAD+7v/HN\n" +
                    "certif: PWvQXYFoeGFeqvoVOCqB7jJZUj+n/eUh9PxsOtwdJlvdoODuQIYyzuSapm6OPnBK\n" +
                    "certif: g+v7Bp39Ym8j5Nfe3xqg+O6CQVH/qx3NoFrKfAaLKGsV++jnf894b23Y/fgu84My\n" +
                    "certif: t+Kn8uOrO6jbBwiWLkgn0uzmO57bi/6F7aMQwSxcMcAY3DhCoeXkeYq0QRZZd2ra\n" +
                    "certif: PbA5r278wPXWg/U5bHenGYX1COWlRehWqXkqR9ZJYY1hTT0/WSAK2ZLCGTK5tDYP\n" +
                    "certif: /iiHbpeWlZhwgx9JkfmgL+N5XoAW6oJna3tozS+xVM5pxTaTNO24vnQw+XQxkiCF\n" +
                    "certif: wtf81chd/oXhjWpLg/K1vF0AWGomN9yS5dtKtlWZ0H/3KeEGkKf9iRp8j1bVNF6m\n" +
                    "certif: Bhb8Xl+nKLWiqE/uezx6OYBFJuj6WpCgbmaRUbmKpX7P++JuOosg0n+BzzJYAIKP\n" +
                    "certif: 4+/FLL35qSpLW+DuWZaXbvgS/OgjJUL8AQj8Nwk7ViRyhBRwSAvwpdcwvlAH1VfT\n" +
                    "certif: HfpQ8a0jjN1Nzf8Tr9Ijo8NQnsa+5y6Pmf6l40j4C8HPsMB7SX8ptFig8lnBRPtz\n" +
                    "certif: EWj54/WtXJwGRG10XW4rdQU5hR9Tufc+WFuRfwdLgrhPTnKGyVG9zOkTd9Cl4j58\n" +
                    "certif: tEsju+m4HNkUN5goouvdxHSe/dmA6cQAWf6/nhJ/uSM3aJPSUOtZwPZO7/NzsMgk\n" +
                    "certif: wZTLXbehm+9xWMkPRt1QT7V5MgfxnxhVoIeoPAEYBo8t0P2GXVMNZdZkJPoViWGO\n" +
                    "certif: ei4iPE3rj6NBynIIoEZNDEJ0OQOUe6Naq5AaG/a6wPa9+ITzKY8VR5KMf3XgcKLB\n" +
                    "certif: lntyyxTgnHY7j5VrhxU3+mUrnwg8LIN9Sx4oWDks/SEB7KN3KGjSgczn1k3GIJRF\n" +
                    "certif: 8BhYin5Cuw/+aD16w5gSHxUhIgwH2BbM5X8eopbp/csA\n" +
                    "certif: =Zeg1\n" +
                    "certif: -----END PGP PUBLIC KEY BLOCK-----\n" +
                    "source: TEST"));
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
