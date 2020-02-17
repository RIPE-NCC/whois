package net.ripe.db.whois.update.keycert;

import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.LocalDateTime;

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
    public void rsaPublicKey() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-28F6CD6C\n" +
                "method:          PGP\n" +
                "owner:           Ed Shryane <eshryane@ripe.net>\n" +
                "fingerpr:        1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C\n" +
                "certif:          -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "certif:          Comment: GPGTools - http://gpgtools.org\n" +
                "certif:          \n" +
                "certif:          mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV\n" +
                "certif:          4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF\n" +
                "certif:          7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz\n" +
                "certif:          RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0\n" +
                "certif:          VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt\n" +
                "certif:          Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz\n" +
                "certif:          aHJ5YW5lQHJpcGUubmV0PokBTwQTAQIAOQIbAwYLCQgHAwIGFQgCCQoLBBYCAwEC\n" +
                "certif:          HgECF4AWIQQcQFAKHcSo2NPqq/numR7iKPbNbAUCWjpsQAAKCRDumR7iKPbNbEA9\n" +
                "certif:          B/9+hT6e65papyTu4IH3aTlB5jcG7p6ZP3PtF41Pg7tixcAokyKGnyczv1kHIg18\n" +
                "certif:          zre8e7ngDLvjmbzYeQkNNG6q8tcbWBkU790+cBllyIFgi7D1z4ZLwk1MJLcTvX2N\n" +
                "certif:          BY55AF8W3fC+8p3u/g3kfjF/0BXLBAZb4j0MQ201LdLjucI/BPbxaGbkvxMqJqQ1\n" +
                "certif:          FSlEt/6W/4x052fNzR0aPcDGuVPudQ+hNNhVZMFvH/qC4qkfg3e1QumiR5U6+xdZ\n" +
                "certif:          bnkydo7f/OAACpw9ZGM4UQPVLTrbwa0oJJW0MOU/jEM8hnJfDijCtGoGF2qMi0+D\n" +
                "certif:          ITDALfI+HR2n65jfemFexPVBuQENBFCuU2YBCADdwFtShuaKGR4fTqg38jOVcYUr\n" +
                "certif:          3Mfmaoj1Ml+GzFxDUheT0L5XMkiccfihVo9z2vMTGcANx6TybX7Pb/pgPKYfs8mI\n" +
                "certif:          dVtHKrAvawDstt+i3b2YtZyMP5APK4DicXHzBKZmCJEpHxYPhoExO2AjLUcb1iX6\n" +
                "certif:          2UtDNcE61YThoA944CVuZ/pSmdBPAMx4TanVfmdnbcjQ0M5zU++tPRVc18qYd8yJ\n" +
                "certif:          gvwx994quiEmc8u8x3j3Rci1clXgfK7vwXo5Mw3fC/rII8ZHhKRr/3K1/fG0RPXC\n" +
                "certif:          Gyaf8/Ff1RHW4IVyO/cA3KWfVl9X7wYSu3ylag8ysPc+NR3FOmnAvZkMDAunABEB\n" +
                "certif:          AAGJAR8EGAECAAkFAlCuU2YCGwwACgkQ7pke4ij2zWxHjggAuzXC6jVMSApDDteF\n" +
                "certif:          ZWrNNzdv2kaqiCVqlzr+Cy7Du2DqTw4WmZbWWjFDNu+adHyEfA1LOsHuzGk75V3a\n" +
                "certif:          WlmOqCsDrh+E1+A2a0TIXwdZZMIOt1hITMWP0SimdBD5us7QpANPoJ9QBEzrVn3x\n" +
                "certif:          5hm407XBb0UIYBw0edAfyAzdnXjN2tkA4xQEM7p68KCa6WcdDEfvJlheKu4y3H2O\n" +
                "certif:          WLhNbDYpVyWsnng9Z6DNfvQc/CqFz7AiEA5mrUS27/wwxobghqVQkLQBDG855qzu\n" +
                "certif:          2ofoWxvqUbBfyix2Gn2EUsxZwPn0dVWe54YnpgZNow65THg0nfts1eMawbIMqFyB\n" +
                "certif:          wf0XpbkDLgRRK3t2EQgAvhJehC8EmYEMyAydlgQFQIarzf1Kw1044e87DFMJI6qZ\n" +
                "certif:          DE3IqNH+g1FbCjkrVV09uHbE3RvyS9aLGgXTc8hFzWNvLvRTUk23lzhPfialdwEb\n" +
                "certif:          kBQjbO5YqZvc91VDLyff5Zgtk0/RIY7cvAOZWSkxytaQ1EnqSNzkXtewZQ9pWrpD\n" +
                "certif:          EoZExbTCkPtiLWDKzf4IEG7vARNbgx5iuDmWVKyQ1V1TVobazaL4qrzRYUzEUOb+\n" +
                "certif:          884CubqL8aPs8DoVdXuWxD3CX0RdBdN2zcMEsC22mmDMAKb7ep2qyxMkhOjADV5s\n" +
                "certif:          xydQsFLirwkMJqvZxIdHtLXg/s0WzuVXeKg3otr0UwEAi/jgXQ6f8cGpjl3uOkrh\n" +
                "certif:          odUXKxJP0EB+oig09RaXQTEH/3GX0Vd2c+fA/9Rsa6NtQXt7qNjDTr0UW9o4F4SM\n" +
                "certif:          vGh784yGzIwlzBW7lZDtlCHiGJvKcGhJhD/kLchYrmwMR3BJK6rrQmnbzYBUE3+n\n" +
                "certif:          A6kmzCzzs7s23IwsOCqkQynzP/vzamiAUWX+BI+X6vQoUkKC7Xf6Bv3oh07D3roH\n" +
                "certif:          aPLntcFgX+mpIja5qnBoyLyvm++nlrFNzj6hUfjHMi5fkxKn8zv4wYLHSR18xK6f\n" +
                "certif:          i7eCu/xvKRNFZdwPVGYN0gAOktueLOg6OTNmPE0lUg8ntAP3JZ4/109u+IuF0vPm\n" +
                "certif:          HET7xW01lEiybqlI2FUgmzTvylOlJ6PDdaNCKMDCoShU344H/iXgnAfydjXFwHhG\n" +
                "certif:          pQlZFgbKIMkhXiXKgy3OWvbe8rvqI0PfQU6tklH8KqtMchaYkMA5ZYbrAuszekxe\n" +
                "certif:          oOukUafPmrgoFxfhe60x/IZgDbAaSU4menxnPg8TRM3SEeb7hcxOdxZFSUjWNlzA\n" +
                "certif:          WJ2+9fRtHu0TrRW5BEyy6CBzmPaG0XycijVan+c8/psD/3hK9BCwR/1k15aEBeL5\n" +
                "certif:          NUYBdv8aWX8873rFxQVpY+vtCyCglYwns4lhtpH4ECE+wb+Mu67mw61ll74/z/nh\n" +
                "certif:          lYU/+TDtrSC4wGryoTKH4fWfZb4GR4Uvq75OhNpbfpCl4o7ErTEj/hykpfjofiVJ\n" +
                "certif:          KDscsCWJAX8EGAECAAkFAlEre3YCGwIAagkQ7pke4ij2zWxfIAQZEQgABgUCUSt7\n" +
                "certif:          dgAKCRBW3fHlgCdDMLELAP44MaK64F0Sbdj5Rugkjmmz91z4LmysI39bIJH0NuME\n" +
                "certif:          0QD/cT/5tNNSpBQfhqu4Ud4CrGwg48GnMJkXDOp8qJtPu77/3wgAskacKCJLLQ5G\n" +
                "certif:          wy+dIEshEmwSiIFMCmiDZSom4hEjVZYcNfjpUhgxNkqBRB5ALzhb/4Iqqvb1rlg/\n" +
                "certif:          bDEcMOgmf4reQcNyvkmxSUmMOlT4q7fwzY4wYwjyKTGWYrEfseyHGmzySuyOh25V\n" +
                "certif:          x8v+AXV/j8i5k4Guksh3/jkj4DoNImXvAFH+tnz/o/70UjAM/rz3Ee3P1UgFDvOO\n" +
                "certif:          szlpRhwDO2yVnJD6cExUYWwzpEyKUhOHsOwNT8Ahep7l1mF+UjcAZWaWE1Liw3MG\n" +
                "certif:          twpr/cWEgje3FBR4XmFPPrd5PoT+Kw5YTbTCKaasPH1GL+5MPzRLsmrkhlfGQ/13\n" +
                "certif:          mnoC8fiEarkBDQRPONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUga\n" +
                "certif:          ZWfexKMnWDu8xpm1qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5\n" +
                "certif:          Ya8FZtwHFpo0TVTlsGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LS\n" +
                "certif:          By6wWlAFCwwr7XwNLGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1R\n" +
                "certif:          GapoDc3m7dvHa3+e27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5Gp\n" +
                "certif:          pC2hMPYGnJV07yahP0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQE2BBgBAgAg\n" +
                "certif:          AhsMFiEEHEBQCh3EqNjT6qv57pke4ij2zWwFAlo6bI0ACgkQ7pke4ij2zWz9QQf+\n" +
                "certif:          LUItAe7WcdpHYiFUhKzCgRFd1k+deEBzLxcRK7sRQKMMwapVRNVJfi+/b3pz70bt\n" +
                "certif:          K2K8LPw4SZSl0UjmuJvMnQJa1pSJBXLz0xB3UPI6pELmgSq08CxWUrWo1OV7nmD8\n" +
                "certif:          jRhYFnoNVYwBR4AGuTdwNqzayUZFDg2pUyryqNz+iMvVurOHPCu+qgamGDmG86ue\n" +
                "certif:          C0NIULSZVf7mDCYpk8D1vWeKTDcu7QXLrXASNaIQSEMWHKAt63rYS7POMcee/kz5\n" +
                "certif:          lYO+3gsvZWUVLQwcZiuXEOcLdNMb6zqFtv999S7WR+PN3+R+fadBJO0rIjK3SRYo\n" +
                "certif:          Cb+QO1GXlG668YnqONKddw==\n" +
                "certif:          =T08n\n" +
                "certif:          -----END PGP PUBLIC KEY BLOCK-----\n" +
                "admin-c:         AA1-RIPE\n" +
                "tech-c:          AA1-RIPE\n" +
                "mnt-by:          UPD-MNT\n" +
                "source:          TEST"));

        assertThat(subject.getPublicKey().getAlgorithm(), is(PublicKeyAlgorithmTags.RSA_GENERAL));
        assertThat(subject.getFingerprint(), is("1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Ed Shryane <eshryane@ripe.net>"));
    }

    @Test
    public void curve25519PublicKey() {
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

        assertThat(subject.getPublicKey().getAlgorithm(), is(PublicKeyAlgorithmTags.EDDSA));
        assertThat(subject.getFingerprint(), is("3F0D 878A 9352 5F7C 4BED  F475 A72E FF2A 2424 420B"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void secp256k1PublicKey() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-B9FD9E0E\n" +
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

        assertThat(subject.getPublicKey().getAlgorithm(), is(PublicKeyAlgorithmTags.ECDSA));
        assertThat(subject.getFingerprint(), is("33A3 9E9F 3515 31CE 6990  4F66 BAA5 1A80 B9FD 9E0E"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void brainpoolP512r1PublicKey() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-34A607E5\n" +
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

        assertThat(subject.getPublicKey().getAlgorithm(), is(PublicKeyAlgorithmTags.ECDSA));
        assertThat(subject.getFingerprint(), is("5F36 A717 5CE1 76D3 2564  A822 2FF6 9819 34A6 07E5"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void nistp521PublicKey() {
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
            RpslObject.parse(
                "key-cert:        PGPKEY-725D9FA9\n" +
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

        assertThat(subject.getPublicKey().getAlgorithm(), is(PublicKeyAlgorithmTags.ECDSA));
        assertThat(subject.getFingerprint(), is("75B5 6A59 4D66 C09D E50A  183B 0862 8883 725D 9FA9"));
        assertThat(subject.getMethod(), is("PGP"));
        assertThat(subject.getOwners(), contains("Test User <noreply@ripe.net>"));
    }

    @Test
    public void unreadableKeyring() {
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
        final PgpPublicKeyWrapper subject = PgpPublicKeyWrapper.parse(
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
        assertThat(subject.isRevoked(), is(true));
    }

    // helper methods

    private String getResource(final String resourceName) throws IOException {
        return IOUtils.toString(new ClassPathResource(resourceName).getInputStream());
    }
}
