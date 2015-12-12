package net.ripe.db.whois.update.keycert;

import com.google.common.base.Charsets;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.Charset;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class PgpSignedMessageTest {

    @Test
    public void isEquals() {
        final PgpSignedMessage firstMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "Comment: none\n" +         // extra comment
                "\t \n" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "source:  TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcIoAAoJELvMuy1XY5UNfJ4H/1jB67IY8q7ViMCMu6YyVGie\n" +
                "9i586ARdRfhHLe6K4TRgjM6TLVJ1VGrLFr2JNPy3cdQ8c1Gdj9GfZetgLrENs5jK\n" +
                "CsAsYFWBiOKTQym937V+KYCoAaWsQeETiPHTi52BjQpHMkktSHKJfOQMvK2IjbSt\n" +
                "3jGgbwYevlhFkV9Fx3AVuEtH21HUGq9zaRFv7O6LyQPYKAL+Vq09TRzLPQQAx0Kx\n" +
                "F2kTjjlAoJPTDGOxLnK8IOVaPwaIdTwMlpRHftv3tgmOp90ozvbImIx0j8GPT+Px\n" +
                "RuoAPlts+k6fPx/BjVUvY47N65jQOg0AF0dquMgybc9zdOvo1x+18rTtt/t5Amw=\n" +
                "=qtOc\n" +
                "-----END PGP SIGNATURE-----");
        final PgpSignedMessage anotherMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcVsAAoJELvMuy1XY5UNw6oH/2JXeEM1aXmdmizbuFlLzNqA\n" +
                "9lu1VwLuO0Z5CexblBYcRQ/6UN9dPDh+uRYJG+6avDpKW3BqwXOSjbsU4cq7cK21\n" +
                "ZViWSgAUEXHKfNXTCaJ7WIFQqJOP5JJRqIwJ1Vb2rhK77Ty8z0JpL5lcU0a7cGgo\n" +
                "SjWLBsUO0Mv8hu5aoNikeO65tuqIk20wbKUH6pMwa616zLALPvGajteG4Zy7+q5W\n" +
                "wOapi/308l0Gbj1WfM6b9uy3+H5OjrRUybqhqJJ/4AOxcLW+uSCHuWosqorbJ0CQ\n" +
                "IuHnlRMsqJ42oUpgT3+swz/N7lwmyLFejDs9Edhd/KSQIVTRTd/z5SjtTHy7wOA=\n" +
                "=yMvf\n" +
                "-----END PGP SIGNATURE-----");

        assertThat(firstMessage.equals(firstMessage), is(true));
        assertThat(firstMessage.equals(anotherMessage), is(false));
    }

    @Test
    public void multiple_dashes_in_signed_message() throws Exception {
        final String signedMessage =
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "\n" +
                "- --\n" +
                "Best regards,\n" +
                "Firstname Lastname\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcduAAoJELvMuy1XY5UNRlwH/3m8rIzM8HpjMremgaVw26AP\n" +
                "bebxrXLKSk8SoAJvDQwrHEYAhihza4QRXv06OZJahBylZ5eDpysAgSPwjqpVDre5\n" +
                "jumQFY4V4Ggxm0zWrfPqkjQP3AndpijWcYPM+iZWgEkB1zHmXAfVzu//cKNw00kM\n" +
                "EDV2df3cge/fMfEydFYUXnHUkQUMuwrlqKX6q3YJD+/73X2x9HKGmPD9bBh2tLnL\n" +
                "EvTj+7wkzXh6psdTph0rjU0SKVxwAPamlIHu0hc8JtogyIVkDsf4PH852bb5Zbgg\n" +
                "cg6jQAiknEvoCwNDozsLZw9zFwr3QukaTM2r6PLICq9Z5HuPDiE2KNmmEqV6xHY=\n" +
                "=P921\n" +
                "-----END PGP SIGNATURE-----";

        final PgpSignedMessage subject = PgpSignedMessage.parse(signedMessage);

        assertThat(subject.verify(getPublicKey_5763950D()), is(true));
        assertThat(subject.verify(getPublicKey_28F6CD6C()), is(false));
        assertThat(subject.getSignedContent(), containsString("\n--\nBest regards,\n"));
    }

    @Test
    public void keyId() {
        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "Comment: none\n" +
                "\t \n" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "source:  TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcIoAAoJELvMuy1XY5UNfJ4H/1jB67IY8q7ViMCMu6YyVGie\n" +
                "9i586ARdRfhHLe6K4TRgjM6TLVJ1VGrLFr2JNPy3cdQ8c1Gdj9GfZetgLrENs5jK\n" +
                "CsAsYFWBiOKTQym937V+KYCoAaWsQeETiPHTi52BjQpHMkktSHKJfOQMvK2IjbSt\n" +
                "3jGgbwYevlhFkV9Fx3AVuEtH21HUGq9zaRFv7O6LyQPYKAL+Vq09TRzLPQQAx0Kx\n" +
                "F2kTjjlAoJPTDGOxLnK8IOVaPwaIdTwMlpRHftv3tgmOp90ozvbImIx0j8GPT+Px\n" +
                "RuoAPlts+k6fPx/BjVUvY47N65jQOg0AF0dquMgybc9zdOvo1x+18rTtt/t5Amw=\n" +
                "=qtOc\n" +
                "-----END PGP SIGNATURE-----");
        assertThat(pgpSignedMessage.getKeyId(), is("5763950D"));
    }

    @Test
    public void verify_success() {
        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "Comment: none\n" +
                "\t \n" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "source:  TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcIoAAoJELvMuy1XY5UNfJ4H/1jB67IY8q7ViMCMu6YyVGie\n" +
                "9i586ARdRfhHLe6K4TRgjM6TLVJ1VGrLFr2JNPy3cdQ8c1Gdj9GfZetgLrENs5jK\n" +
                "CsAsYFWBiOKTQym937V+KYCoAaWsQeETiPHTi52BjQpHMkktSHKJfOQMvK2IjbSt\n" +
                "3jGgbwYevlhFkV9Fx3AVuEtH21HUGq9zaRFv7O6LyQPYKAL+Vq09TRzLPQQAx0Kx\n" +
                "F2kTjjlAoJPTDGOxLnK8IOVaPwaIdTwMlpRHftv3tgmOp90ozvbImIx0j8GPT+Px\n" +
                "RuoAPlts+k6fPx/BjVUvY47N65jQOg0AF0dquMgybc9zdOvo1x+18rTtt/t5Amw=\n" +
                "=qtOc\n" +
                "-----END PGP SIGNATURE-----");

        final boolean result = pgpSignedMessage.verify(getPublicKey_5763950D());

        assertThat(result, is(true));
    }

    @Test
    public void extra_newlines() {
        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "  \t \n" +
                "\n" +
                "\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTciyAAoJELvMuy1XY5UNpoYIAINRyb8/9chIAtniin1rxxpJ\n" +
                "CUvaxR7Cq53BmBoSzK4FqNCFt0fTjn9gpUGBoZKtlu+sCxUqsfyy/TQUkkky+f8D\n" +
                "d55ZWEeKpcAN+X4idntx0WDRujWkgDTTyUOFtU744B0LdUFNZO1xU5NVApxGGaQ7\n" +
                "/9wCtFRoPqwYtRJixh2k3JQmgT0uAojFMhDHNCTW2lxJgHNr9TE4XJs/uzK/e72h\n" +
                "EHKPqXTDa/sZW9kWD+EPTthlPtzgCWGKdoqWKGUWNo9v4w7ouh3nteG5iccyk1eC\n" +
                "tHGThBUfNK21yUobomm6FHVixykug2UHtdmiDcS8LaFdctb6PdOvn2jaAJGCMY4=\n" +
                "=GHH4\n" +
                "-----END PGP SIGNATURE-----");

        assertThat(pgpSignedMessage.verify(getPublicKey_5763950D()), is(true));
    }

    @Test
    public void verify_failure() {
        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(
               ("-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "Comment: none\n" +
                "\t \n" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "source:  TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcIoAAoJELvMuy1XY5UNfJ4H/1jB67IY8q7ViMCMu6YyVGie\n" +
                "9i586ARdRfhHLe6K4TRgjM6TLVJ1VGrLFr2JNPy3cdQ8c1Gdj9GfZetgLrENs5jK\n" +
                "CsAsYFWBiOKTQym937V+KYCoAaWsQeETiPHTi52BjQpHMkktSHKJfOQMvK2IjbSt\n" +
                "3jGgbwYevlhFkV9Fx3AVuEtH21HUGq9zaRFv7O6LyQPYKAL+Vq09TRzLPQQAx0Kx\n" +
                "F2kTjjlAoJPTDGOxLnK8IOVaPwaIdTwMlpRHftv3tgmOp90ozvbImIx0j8GPT+Px\n" +
                "RuoAPlts+k6fPx/BjVUvY47N65jQOg0AF0dquMgybc9zdOvo1x+18rTtt/t5Amw=\n" +
                "=qtOc\n" +
                "-----END PGP SIGNATURE-----").replace("Admin Person", "Some Text"));
        assertThat(pgpSignedMessage.getKeyId(), is("5763950D"));
        assertThat(pgpSignedMessage.verify(getPublicKey_5763950D()), is(false));
    }

    @Test
    public void parse_signed_message_with_multiple_paragraphs_and_headers() {
        final String message =
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "  \t \n" +
                "aut-num:        AS1234\n" +
                "\n" +
                "person:\t\tfirst last\n" +
                "\n\n" +
                "inetnum:\t10/8\n" +
                "\n\t\n" +
                "\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.13 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJRZB2VAAoJELvMuy1XY5UNFnsH/izUQBQPksabdSSJCggfWtLe\n" +
                "ijbxiN7IZDJyijJfrSq7GvQTBYt4mzQjFLpDl7Zc/1M/D0W/HJzj3WW7BMErYKaV\n" +
                "Go7laXrY9l5VkGoO6YpUbJY9zumXq8u7sU32WuMx92LDkhWkdfYAs536i6mdpEi+\n" +
                "cX0Z9qYzguINyzCbZzeR80NIJH54c7qPBA1jLYwutD2mrDDi46hxUG7VxNXQ02TX\n" +
                "VhaO8bcXrUxH0EuWsXFgKeZfuIpwucyd3UN+1/s8+QtSSCBsXxqWD+aEI7bU37oe\n" +
                "jhIixNOJ9dTzaq7/byJinsGfsdwXPBMRGA/0gcLUAHGLvJybOwoXqOxi21iT6tA=\n" +
                "=8U1H\n" +
                "-----END PGP SIGNATURE-----";

        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(message);
        assertThat(pgpSignedMessage.getKeyId(), is("5763950D"));
        assertThat(pgpSignedMessage.verify(getPublicKey_28F6CD6C()), is(false));
        assertThat(pgpSignedMessage.verify(getPublicKey_5763950D()), is(true));
    }

    @Test
    public void parse_signed_message_with_multiple_signers() {
        final String message =
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "- -----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "source:  TEST\n" +
                "- -----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcszAAoJELvMuy1XY5UN/KYH/1ODY1QNeTgLT8UoLMHcVMYJ\n" +
                "vpbxxHK8g62kBulcjn39tGaN7FzMNnbYTu3DBdvmsRDGTeMxkGoMsSgStw8zglYp\n" +
                "CCyDOyTidaRNSDs9XW5Iyq/9Zii4lg0z9m7vPWpcLQYfdlRb8g4Wd15gGoHtQtxn\n" +
                "qN2j81rkvpv1XsmmkN3H+kHalnUFG3MCd/VvBpizHn2+Wmoc+DGIQrqVwypVpFUK\n" +
                "jvBELpjEXjGxhxU9CZQ+AgDo0HB9BOlYoEE5roFTHIF3nLurBWvHu90xOBfotACs\n" +
                "DpVNCPe8ifyJSjVy2sJzKitPO7uWqx6SpRslnLWIDWJy1+pXuSPijpSYx/N+1/U=\n" +
                "=0gPP\n" +
                "- -----END PGP SIGNATURE-----\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTctFAAoJEHbKke+BzPl9feIH/0Vfygye2rf8qdow/GD6t/9V\n" +
                "WnUBsk6rFNWEO/5iYb9W/naJMokL8THn6Bivv1sBP4s3V9hvu8kFPUCHQWoTy6qc\n" +
                "+b6g+KMmA1+vhSde9z+GtFrF3t+AqagAtMvBLRPiv0dyVH9ND71iLR9AEWxLQ3j9\n" +
                "3UV2Y0f0zdetYUR869tIGlVwo0r5w6vNfGT7322iULituY3qxFKy4ZUoCO+qAmAA\n" +
                "M26YlI03y2FuRq/LCk2gopcFfBIRDKo+cC8ceLi7/CF6jAeZEsGpxn0aC1kBrmW9\n" +
                "gH2fOg/rQAmKswAlspqZv+JXftzBadM/apD19QjbxnoqjE9PgcxYh6gGA8QR7gA=\n" +
                "=OeTr\n" +
                "-----END PGP SIGNATURE-----";

        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(message);
        assertThat(pgpSignedMessage.getKeyId(), is("81CCF97D"));
        assertThat(pgpSignedMessage.verify(getPublicKey_28F6CD6C()), is(false));

        final PgpSignedMessage innerPgpSignedMessage = PgpSignedMessage.parse(pgpSignedMessage.getSignedContent());
        assertThat(innerPgpSignedMessage.getKeyId(), is("5763950D"));
        assertThat(innerPgpSignedMessage.verify(getPublicKey_5763950D()), is(true));
    }

    @Test
    public void verify_multipart_plain_text_message() throws Exception {
        final String signedData =
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/plain;\n" +
                "\tcharset=us-ascii\n\n" +
                "This is a test.";
        final String signature =
                "iQEcBAEBAgAGBQJQ3GncAAoJEO6ZHuIo9s1sUz0H/0j2YAw4VyiuUgkzQ8rgm2WY\n" +
                "58dBXMgbCB6fWYNTO8tJkb62ELn/6mjczZg149xLvh9uoKod5HoFWhrimaddNAYe\n" +
                "l52VHP1f6/076Pu7f9ADWV2IaaKMoJ568Vt1rWRhQhAfvfe1Wpbs2R6fVUrzl+QD\n" +
                "84M4/Byb2CGC5N6EzOEUBEm+3XVWQ9fjwWFt0Ann7LDisK3+gZdOpdcTILgDCGKG\n" +
                "l/FtlwmBHGfONHVGC+lRDi39IXXLAbpmrTSJ/9AGGXyiTISyOl5Yx3kDIR22yhKa\n" +
                "3fIZWxWJJtBjzdFxvgLN7KqPFLI6tcBFB5oW5V5RAI+4zLJUg7goLLZRe3CYkDc=\n" +
                "=8tAB\n";

        PgpSignedMessage subject = PgpSignedMessage.parse(signedData, signature);
        assertThat(subject.verify(getPublicKey_28F6CD6C()), Matchers.is(true));
    }

    @Test
    public void verify_multipart_alternative_message() throws Exception {
        final String signedData =
                "Content-Type: multipart/alternative;\n" +
                "\tboundary=\"Apple-Mail=_CA96AAFD-E7AD-465B-804B-ADE6C5D1A1B3\"\n" +
                "\n" +
                "\n" +
                "--Apple-Mail=_CA96AAFD-E7AD-465B-804B-ADE6C5D1A1B3\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/plain;\n" +
                "\tcharset=us-ascii\n" +
                "\n" +
                "This is a test\n" +
                "--Apple-Mail=_CA96AAFD-E7AD-465B-804B-ADE6C5D1A1B3\n" +
                "Content-Transfer-Encoding: 7bit\n" +
                "Content-Type: text/html;\n" +
                "\tcharset=us-ascii\n" +
                "\n" +
                "<html><head></head><body style=\"word-wrap: break-word; " +
                "-webkit-nbsp-mode: space; -webkit-line-break: after-white-space; \">" +
                "<b>This is a test</b></body></html>\n" +
                "--Apple-Mail=_CA96AAFD-E7AD-465B-804B-ADE6C5D1A1B3--\n";
        final String signature =
                "iQEcBAEBAgAGBQJQ3FVdAAoJEO6ZHuIo9s1sGowH/io07xFwVDwQC8vV3n7lndvN\n" +
                "Xpq3/qKoguKh67Ukrr/qBg/R1HFiWU/iKBkNrEweKQ8/BUSkMopUuaMwCZZIVSTp\n" +
                "2T0mj5WaiOtnqBTFsGIvcadg/7mn7Oq2s6YRaQ/5VuBZr4WGsGetpbVmO0p++0Nk\n" +
                "/QhOF/aC1+to0p/AGrokLserxWHmJ2jfSsL+uUoh7YYO3LMKoRiEwEpKSJBjm00W\n" +
                "s/bzalT+4xg8xEqoUlFfUdhCcjDXdIBYl7JjM13lkMwLNzxMHIxfN3fwB83ehWVp\n" +
                "40N9PpN3W9m96pl2m3mI6UwRtW3GZzasup4FnsdkXRpt73rFwUmn5UyiWFbK1Xo=\n" +
                "=IifW\n";

        PgpSignedMessage subject = PgpSignedMessage.parse(signedData, signature);
        assertThat(subject.verify(getPublicKey_28F6CD6C()), Matchers.is(true));
    }

    // latin1 extended characters are not encoded into bytes properly, unless the original charset is specified.
    @Test
    public void verify_latin1_encoded_message_with_umlaut_character() {
        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person: Test Person\n" +
                "address:München, Germany\n" +
                "phone:  +49 282 411141\n" +
                "fax-no: +49 282 411140\n" +
                "nic-hdl:TP1-TEST\n" +
                "mnt-by: UPD-MNT\n" +
                "source: TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcMYAAoJELvMuy1XY5UNJsgIAJ/6zx94WFMvAVfJG6fu8xuE\n" +
                "5OkT3uGRPuoPPhFidGgMmZl9wqdVg5FoUmrf+OQlreFapZu7wEXBdqcpRlelnNwa\n" +
                "hEflTJY5JzQD9sqKD7i9E3fRnCWA438DEATPeuxHC+1+RbE1eLD9YSOdnDg/bt3U\n" +
                "x8aKHa6AJ09I6eDhAf8+hSpZjKQWgj9+FsHBScqbLmAIPN1ujeLXGmJOdIMFh2Fj\n" +
                "1rHkHnYy6gEcaO37/M9GsbKP7xoGJfzabw9xoU/hVoEXvBl+NSbDgVbfol4hkVQb\n" +
                "/uMu1kyTSjDpuQX8iOsorW7w+cAFGN3pS6mUo+5qIJI5SjnU8XZLl6SRe5FDnuc=\n" +
                "=Cqk0\n" +
                "-----END PGP SIGNATURE-----", Charsets.ISO_8859_1);

        assertThat(pgpSignedMessage.verify(getPublicKey_5763950D()), is(true));
    }

    @Test
    public void verify_iso_8859_7_encoded_message_with_greek_characters() {
        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:     Test Person\n" +
                "address:    ακρόπολη\n" +
                "phone:      +30 123 411141\n" +
                "fax-no:     +30 123 411140\n" +
                "nic-hdl:    TP1-TEST\n" +
                "mnt-by:     UPD-MNT\n" +
                "source:     TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTa/LAAoJELvMuy1XY5UNpukIAIOAgObWVSNbyOklWgIGjH6q\n" +
                "XeJY6LhysQCQTEbD+AMJhd5rQQbN4jnK+MwaTneCwKuEwppzcWzthd0ZFdWFk+gR\n" +
                "N5PYtfHcWn46a0jW6pA1EUxGf+8V8vVSM0hnDJnK/rHv7F+aCo/4uYTID6AGit/Q\n" +
                "/lsURfe58oQZ88N8rJcA+WVDkEdEpqsRErqWi0J5TE7h0lnm7xLyGOF8OQBs/kqg\n" +
                "QypGr+WzELhLkrL7nBCiL00wylYR4pD1P2VzNSrezgcsprgrCGpzurXaXVi7nWgs\n" +
                "//7jgTneD5yXfR4iWV6/JKps9zksPLR33bcB0rWLdnzdXRGO83FbORtdGOfVjqE=\n" +
                "=0RVp\n" +
                "-----END PGP SIGNATURE-----", Charset.forName("ISO-8859-7"));

        assertThat(pgpSignedMessage.verify(getPublicKey_5763950D()), is(true));
    }

    @Test
    public void verify_utf8_encoded_message_with_umlaut_character() {
        final PgpSignedMessage pgpSignedMessage = PgpSignedMessage.parse(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:     Test Person\n" +
                "address:    München, Germany\n" +
                "phone:      +49 282 411141\n" +
                "fax-no:     +49 282 411140\n" +
                "nic-hdl:    TP1-TEST\n" +
                "mnt-by:     UPD-MNT\n" +
                "source:     TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJWTcysAAoJELvMuy1XY5UNYq0H/RMzeb454o4IFQqFzQJzTpQY\n" +
                "66lzy8CjgZGAE2R2v0Q3KZwNRGNYkYYsVe9rXPbN3nWJR4rF5dKlxeDtR1Ac2zkA\n" +
                "byCEnKXlkZE8Cdj7NHpJ4hyYz0SHV+tSNNPRurIaDaWQA63eeNlfwfJPwfMHNsVA\n" +
                "027+dMGTSj+S1HE7I+fXx2YaMDKa7TRez1ruo8JJ3qOUtTdl1qgdFMX3BGXBRjNE\n" +
                "QuSCFQpkwx56H3B4emnILLWp0l/19AFkQXdbFJNfB9xndBwzhgfOobz9RpuF5pBI\n" +
                "DBXkWzwkb/+qHTcixxaQQrjC5EH6hJKFF0YBuF4pfCC2bU81zSEXHIaEwBf7CQA=\n" +
                "=/8Rc\n" +
                "-----END PGP SIGNATURE-----", Charsets.UTF_8);

        assertThat(pgpSignedMessage.verify(getPublicKey_5763950D()), is(true));
    }

    // helper methods

    private PGPPublicKey getPublicKey_28F6CD6C() {
        PgpPublicKeyWrapper wrapper = PgpPublicKeyWrapper.parse(
                RpslObject.parse(
                        "key-cert:       PGPKEY-28F6CD6C\n" +
                        "method:         PGP\n" +
                        "owner:          Ed Shryane <eshryane@ripe.net>\n" +
                        "fingerpr:       1C40 500A 1DC4 A8D8 D3EA  ABF9 EE99 1EE2 28F6 CD6C\n" +
                        "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                        "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                        "certif:         Comment: GPGTools - http://gpgtools.org\n" +
                        "certif:\n" +
                        "certif:         mQENBE841dMBCAC80IDqJpJC7ch16NEaWvLDM8CslkhiqYk9fgXgUdMNuBsJJ/KV\n" +
                        "certif:         4oUwzrX+3lNvMPSoW7yRfiokFQ48IhYVZuGlH7DzwsyfS3MniXmw6/vT6JaYPuIF\n" +
                        "certif:         7TmMHIIxQbzJe+SUrauzJ2J0xQbnKhcfuLkmNO7jiOoKGJWIrO5wUZfd0/4nOoaz\n" +
                        "certif:         RMokk0Paj6r52ZMni44vV4R0QnuUJRNIIesPDYDkOZGXX1lD9fprTc2DJe8tjAu0\n" +
                        "certif:         VJz5PpCHwvS9ge22CRTUBSgmf2NBHJwDF+dnvijLuoDFyTuOrSkq0nAt0B9kTUxt\n" +
                        "certif:         Bsb7mNxlARduo5419hBp08P07LJb4upuVsMPABEBAAG0HkVkIFNocnlhbmUgPGVz\n" +
                        "certif:         aHJ5YW5lQHJpcGUubmV0PokBOAQTAQIAIgUCTzjV0wIbAwYLCQgHAwIGFQgCCQoL\n" +
                        "certif:         BBYCAwECHgECF4AACgkQ7pke4ij2zWyUKAf+MmDQnBUUSjDeFvCnNN4JTraMXFUi\n" +
                        "certif:         Ke2HzVnLvT/Z/XN5W6TIje7u1luTJk/siJJyKYa1ZWQoVOCXruTSge+vP6LxENOX\n" +
                        "certif:         /sOJ1YxWHJUr3OVOfW2NoKBaUkBBCxi/CSaPti7YPHF0D6rn3GJtoJTnLL4KPnWV\n" +
                        "certif:         gtja4FtpsgwhiPF/jVmx6/d5Zc/dndDLZZt2sMjh0KDVf7F03hsF/EAauBbxMLvK\n" +
                        "certif:         yEHMdw7ab5CxeorgWEDaLrR1YwHWHy9cbYC00Mgp1zQR1ok2wN/XZVL7BZYPS/UC\n" +
                        "certif:         H03bFi3AcN1Vm55QpbU0QJ4qPN8uwYc5VBFSSYRITUCwbB5qBO5kIIBLP7kBDQRP\n" +
                        "certif:         ONXTAQgA16kMTcjxOtkU8v3sLAIpr2xWwG91BdB2fLV0aUgaZWfexKMnWDu8xpm1\n" +
                        "certif:         qY+viF+/emdXBc/C7QbFUmhmXCslX5kfD10hkYFTIqc1Axk5Ya8FZtwHFpo0TVTl\n" +
                        "certif:         sGodZ2gy8334rT9yMH+bZNSlZ+07Fxa7maC1ycxPPL/68+LSBy6wWlAFCwwr7XwN\n" +
                        "certif:         LGnrBbELgvoi04yMu1EpqAvxZLH1TBgzrFcWzXJjj1JKIB1RGapoDc3m7dvHa3+e\n" +
                        "certif:         27aQosQnNVNWrHiS67zqWoC963aNuHZBY174yfKPRaN6s5GppC2hMPYGnJV07yah\n" +
                        "certif:         P0mwRcp4e3AaJIg2SP9CUQJKGPY+mQARAQABiQEfBBgBAgAJBQJPONXTAhsMAAoJ\n" +
                        "certif:         EO6ZHuIo9s1souEH/ieP9J69j59zfVcN6FimT86JF9CVyB86PGv+naHEyzOrBjml\n" +
                        "certif:         xBn2TPCNSE5KH8+gENyvYaQ6Wxv4Aki2HnJj5H43LfXPZZ6HNME4FPowoIkumc9q\n" +
                        "certif:         mndn6WXsgjwT9lc2HQmUgolQObg3JMBRe0rYzVf5N9+eXkc5lR/PpTOHdesP17uM\n" +
                        "certif:         QqtJs2hKdZKXgKNufSypfQBLXxkhez0KvoZ4PvrLItZTZUjrnRXdObNUgvz5/SVh\n" +
                        "certif:         4Oqesj+Z36YNFrsYobghzIqOiP4hINsm9mQoshz8YLZe0z7InwcFYHp7HvQWEOyj\n" +
                        "certif:         kSYadR4aN+CVhYHOsn5nxbiKSFNAWh40q7tDP7K5AQ0EUK5TZgEIAN3AW1KG5ooZ\n" +
                        "certif:         Hh9OqDfyM5VxhSvcx+ZqiPUyX4bMXENSF5PQvlcySJxx+KFWj3Pa8xMZwA3HpPJt\n" +
                        "certif:         fs9v+mA8ph+zyYh1W0cqsC9rAOy236LdvZi1nIw/kA8rgOJxcfMEpmYIkSkfFg+G\n" +
                        "certif:         gTE7YCMtRxvWJfrZS0M1wTrVhOGgD3jgJW5n+lKZ0E8AzHhNqdV+Z2dtyNDQznNT\n" +
                        "certif:         7609FVzXyph3zImC/DH33iq6ISZzy7zHePdFyLVyVeB8ru/BejkzDd8L+sgjxkeE\n" +
                        "certif:         pGv/crX98bRE9cIbJp/z8V/VEdbghXI79wDcpZ9WX1fvBhK7fKVqDzKw9z41HcU6\n" +
                        "certif:         acC9mQwMC6cAEQEAAYkBHwQYAQIACQUCUK5TZgIbDAAKCRDumR7iKPbNbEeOCAC7\n" +
                        "certif:         NcLqNUxICkMO14Vlas03N2/aRqqIJWqXOv4LLsO7YOpPDhaZltZaMUM275p0fIR8\n" +
                        "certif:         DUs6we7MaTvlXdpaWY6oKwOuH4TX4DZrRMhfB1lkwg63WEhMxY/RKKZ0EPm6ztCk\n" +
                        "certif:         A0+gn1AETOtWffHmGbjTtcFvRQhgHDR50B/IDN2deM3a2QDjFAQzunrwoJrpZx0M\n" +
                        "certif:         R+8mWF4q7jLcfY5YuE1sNilXJayeeD1noM1+9Bz8KoXPsCIQDmatRLbv/DDGhuCG\n" +
                        "certif:         pVCQtAEMbznmrO7ah+hbG+pRsF/KLHYafYRSzFnA+fR1VZ7nhiemBk2jDrlMeDSd\n" +
                        "certif:         +2zV4xrBsgyoXIHB/Rel\n" +
                        "certif:         =Aova\n" +
                        "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                        "notify:         noreply@ripe.net\n" +
                        "mnt-by:         OWNER-MNT\n" +
                        "source:         TEST\n"));

        return wrapper.getPublicKey();
    }

    private PGPPublicKey getPublicKey_5763950D() {
        PgpPublicKeyWrapper wrapper = PgpPublicKeyWrapper.parse(
                RpslObject.parse(
                        "key-cert:       PGPKEY-5763950D\n" +
                        "method:         PGP\n" +
                        "owner:          No Reply <noreply@ripe.net>\n" +
                        "fingerpr:       884F 8E23 69E5 E6F1 9FB3  63F4 BBCC BB2D 5763 950D\n" +
                        "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                        "certif:         Version: GnuPG v1.4.12 (Darwin)\n" +
                        "certif:\n" +
                        "certif:         mQENBFC0yvUBCACn2JKwa5e8Sj3QknEnD5ypvmzNWwYbDhLjmD06wuZxt7Wpgm4+\n" +
                        "certif:         yO68swuow09jsrh2DAl2nKQ7YaODEipis0d4H2i0mSswlsC7xbmpx3dRP/yOu4WH\n" +
                        "certif:         2kZciQYxC1NY9J3CNIZxgw6zcghJhtm+LT7OzPS8s3qp+w5nj+vKY09A+BK8yHBN\n" +
                        "certif:         E+VPeLOAi+D97s+Da/UZWkZxFJHdV+cAzQ05ARqXKXeadfFdbkx0Eq2R0RZm9R+L\n" +
                        "certif:         A9tPUhtw5wk1gFMsN7c5NKwTUQ/0HTTgA5eyKMnTKAdwhIY5/VDxUd1YprnK+Ebd\n" +
                        "certif:         YNZh+L39kqoUL6lqeu0dUzYp2Ll7R2IURaXNABEBAAG0I25vcmVwbHlAcmlwZS5u\n" +
                        "certif:         ZXQgPG5vcmVwbHlAcmlwZS5uZXQ+iQE4BBMBAgAiBQJQtMr1AhsDBgsJCAcDAgYV\n" +
                        "certif:         CAIJCgsEFgIDAQIeAQIXgAAKCRC7zLstV2OVDdjSCACYAyyWr83Df/zzOWGP+qMF\n" +
                        "certif:         Vukj8xhaM5f5MGb9FjMKClo6ezT4hLjQ8hfxAAZxndwAXoz46RbDUsAe/aBwdwKB\n" +
                        "certif:         0owcacoaxUd0i+gVEn7CBHPVUfNIuNemcrf1N7aqBkpBLf+NINZ2+3c3t14k1BGe\n" +
                        "certif:         xCInxEqHnq4zbUmunCNYjHoKbUj6Aq7janyC7W1MIIAcOY9/PvWQyf3VnERQImgt\n" +
                        "certif:         0fhiekCr6tRbANJ4qFoJQSM/ACoVkpDvb5PHZuZXf/v+XB1DV7gZHjJeZA+Jto5Z\n" +
                        "certif:         xrmS5E+HEHVBO8RsBOWDlmWCcZ4k9olxp7/z++mADXPprmLaK8vjQmiC2q/KOTVA\n" +
                        "certif:         uQENBFC0yvUBCADTYI6i4baHAkeY2lR2rebpTu1nRHbIET20II8/ZmZDK8E2Lwyv\n" +
                        "certif:         eWold6pAWDq9E23J9xAWL4QUQRQ4V+28+lknMySXbU3uFLXGAs6W9PrZXGcmy/12\n" +
                        "certif:         pZ+82hHckh+jN9xUTtF89NK/wHh09SAxDa/ST/z/Dj0k3pQWzgBdi36jwEFtHhck\n" +
                        "certif:         xFwGst5Cv8SLvA9/DaP75m9VDJsmsSwh/6JqMUb+hY71Dr7oxlIFLdsREsFVzVec\n" +
                        "certif:         YHsKINlZKh60dA/Br+CC7fClBycEsR4Z7akw9cPLWIGnjvw2+nq9miE005QLqRy4\n" +
                        "certif:         dsrwydbMGplaE/mZc0d2WnNyiCBXAHB5UhmZABEBAAGJAR8EGAECAAkFAlC0yvUC\n" +
                        "certif:         GwwACgkQu8y7LVdjlQ1GMAgAgUohj4q3mAJPR6d5pJ8Ig5E3QK87z3lIpgxHbYR4\n" +
                        "certif:         HNaR0NIV/GAt/uca11DtIdj3kBAj69QSPqNVRqaZja3NyhNWQM4OPDWKIUZfolF3\n" +
                        "certif:         eY2q58kEhxhz3JKJt4z45TnFY2GFGqYwFPQ94z1S9FOJCifL/dLpwPBSKucCac9y\n" +
                        "certif:         6KiKfjEehZ4VqmtM/SvN23GiI/OOdlHL/xnU4NgZ90GHmmQFfdUiX36jWK99LBqC\n" +
                        "certif:         RNW8V2MV+rElPVRHev+nw7vgCM0ewXZwQB/bBLbBrayx8LzGtMvAo4kDJ1kpQpip\n" +
                        "certif:         a/bmKCK6E+Z9aph5uoke8bKoybIoQ2K3OQ4Mh8yiI+AjiQ==\n" +
                        "certif:         =HQmg\n" +
                        "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                        "notify:         noreply@ripe.net\n" +
                        "mnt-by:         ADMIN-MNT\n" +
                        "source:         TEST"));

        return wrapper.getPublicKey();
    }

    private PGPPublicKey getPublicKey_81CCF97D() {
        PgpPublicKeyWrapper wrapper = PgpPublicKeyWrapper.parse(
                RpslObject.parse(
                        "key-cert:       PGPKEY-81CCF97D\n" +
                        "method:         PGP\n" +
                        "owner:          Unknown <unread@ripe.net>\n" +
                        "fingerpr:       EDDF 375A B830 D1BB 26E5  ED3B 76CA 91EF 81CC F97D\n" +
                        "certif:         -----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                        "certif:\n" +
                        "certif:         mQENBFC0yfkBCAC/zYZw2vDpNF2Q7bfoTeTmhEPERzUX3y1y0jJhGEdbp3re4v0i\n" +
                        "certif:         XWDth4lp9Rr8RimoqQFN2JNFuUWvohiDAT91J+vAG/A67xuTWXACyAPhRtRIFhxS\n" +
                        "certif:         tBu8h/qEv8yhudhjYfVHu8rUbm59BXzO80KQA4UP5fQeDVwGFbvB+73nF1Pwbg3n\n" +
                        "certif:         RzgLvKZlxdgV2RdU+DvxabkHgiN0ybcJx3nntL3Do2uZEdkkDDKkN6hkUJY0cFbQ\n" +
                        "certif:         Oge3AK84huZKnIFq8+NA/vsE3dg3XhbCYUlS4yMe0cvnZrH23lnu4Ubp1KBILHVW\n" +
                        "certif:         K4vWnMEXcx/T2k4/vpXogZNUH6E3OjtlyjX5ABEBAAG0GVVua25vd24gPHVucmVh\n" +
                        "certif:         ZEByaXBlLm5ldD6JATgEEwECACIFAlC0yfkCGwMGCwkIBwMCBhUIAgkKCwQWAgMB\n" +
                        "certif:         Ah4BAheAAAoJEHbKke+BzPl9UasH/1Tc2YZiJHw3yaKvZ8jSXDmZKmO69C7YvgsX\n" +
                        "certif:         B72w4K6d92vy8dLLreqEpzXKtWB1+K6bLZv6MEdNbvQReG3rw1i2Io7kdsKFn9QC\n" +
                        "certif:         OeY4OwpzBMZIJGWWXxOLz9Auo9a43xU+wL92/oCqFJrLuuppgOIVkL0pBWRDQYqp\n" +
                        "certif:         3MqyHdsUOEdd7pwUlGJlfLqa7wmO+r04EG1OBRLBg5p4gVARqDrVMA3ym9KF750T\n" +
                        "certif:         78Il1eWrceLglI5F0h4RYEmQ3amF/ukbPyzf26+J6MnWeDSO3Q8P/aDO3L7ccNoC\n" +
                        "certif:         VwyHxUumWgfQVEnt6IaKLSjxVPhhAFO0wLd2tgaUH1y/ug1RgJe5AQ0EULTJ+QEI\n" +
                        "certif:         APgAjb0YCTRvIdlYFfKQfLCcIbifwFkBjaH9fN8A9ZbeXSWtO7RXEvWF70/ZX69s\n" +
                        "certif:         1SfQyL4cnIUN7hEd7/Qgx63IXUfNijolbXOUkh+S41tht+4IgJ7iZsELuugvbDEb\n" +
                        "certif:         VynMXFEtqCXm1zLfd0g2AsWPFRczkj7hWE0gNs7iKvEiGrjFy0eSd/q07oWLxJfq\n" +
                        "certif:         n4GBBPMGkfKxWhy5AXAkPZp1mc7mlYuNP9xrn76bl69T0E69kDPS3JetSaVWj0Uh\n" +
                        "certif:         NSJSjP1Zc8g+rvkeum3HKLoW0svRo2XsldjNMlSuWb/oxeaTdGZV6SxTJ+T1oHAi\n" +
                        "certif:         tovyQHusvGu3D9dfvTcW3QsAEQEAAYkBHwQYAQIACQUCULTJ+QIbDAAKCRB2ypHv\n" +
                        "certif:         gcz5fe7cB/9PrDR7ybLLmNAuoafsVQRevKG8DfVzDrgThgJz0jJhb1t74qy5xXn+\n" +
                        "certif:         zW8d/f/JZ8jr7roWA64HKvdvo8ZXuGEf6H20p1+HbjYpT52zteNU/8ljaqIzJBes\n" +
                        "certif:         tl8ecFB7qg3qUSDQseNaA1uHkZdxGybzgI69QlOyh8fRfOCh/ln9vAiL0tW+Kzjg\n" +
                        "certif:         8VMY0N3HzBcAPSB7U8wDf1qMzS5Lb1yNunD0Ut5qxCq3fxcdLBk/ZagHmtXoelhH\n" +
                        "certif:         Bng8TRND/cDUWWH7Rhv64NxUiaKsrM/EmrHFOpJlXuMRRx4FtRPZeXTOln7zTmIL\n" +
                        "certif:         qqHWqaQHNMKDq0pf24NFrIMLc2iXCSh+\n" +
                        "certif:         =FPEl\n" +
                        "certif:         -----END PGP PUBLIC KEY BLOCK-----\n" +
                        "notify:         unread@ripe.net\n" +
                        "mnt-by:         ADMIN-MNT\n" +
                        "source:         TEST"));
        return wrapper.getPublicKey();
    }

    @Ignore("TODO: [ES] headers in signature block should not be required")
    @Test
    public void headers_in_signature_are_mandatory() {
    final PgpSignedMessage signedMessage = PgpSignedMessage.parse(
            "-----BEGIN PGP SIGNED MESSAGE-----\n" +
            "Hash: SHA1\n" +
            "\n" +
            "person:  First Person\n" +
            "address: St James Street\n" +
            "address: Burnley\n" +
            "address: UK\n" +
            "phone:   +44 282 420469\n" +
            "nic-hdl: FP1-TEST\n" +
            "mnt-by:  OWNER-MNT\n" +
            "source:  TEST\n" +
            "-----BEGIN PGP SIGNATURE-----\n" +
//                "Version: GnuPG v1.4.12 (Darwin)\n" +             // TODO: if no headers found, parsing fails,
//                "Comment: GPGTools - http://gpgtools.org\n" +     // TODO: as ArmoredInputStream hasHeaders = true by default
//                "\n" +
            "iQEcBAEBAgAGBQJWTcPDAAoJELvMuy1XY5UNd50IAKLecIhQCMaUu8hVUW68uXGO\n" +
            "uWqnkI3/C3CzgNai438xlKqtYNmyhgzCh3LbJSiNkJqRg6QK9G+egcpf9wtlLJJl\n" +
            "1N72OBV1IS+yWSmkscNuihJNZBwlYrkG7SXAite5wjaY++nVjkNqqCVkHPwD6dr8\n" +
            "AcPN0lxdhFbnvVlcFJVZPWrEXKEZbRxBr2BosF48IiKDskfu/2wiUWiBwF5QkuTB\n" +
            "GX/pLJwQ40J79jVhRkiVbJ0YvAcHKbE/ftgw3ZBwjUJk2X2Ck1pv+HOTurhgXRvh\n" +
            "gr8cbqpXlg1D0FNl3jWe/RTQ/U465ZZiMyUodI93LMZQNPtR7xh3+T8r1gVORMI=\n" +
            "=D3U7\n" +
            "-----END PGP SIGNATURE-----");

        assertThat(signedMessage.getKeyId(), is("5763950D"));
    }
}




