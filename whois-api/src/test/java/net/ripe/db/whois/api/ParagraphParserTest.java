package net.ripe.db.whois.api;

import net.ripe.db.whois.update.domain.*;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ParagraphParserTest {
    @InjectMocks
    private ParagraphParser subject;
    public static final String OBJECT = "mntner: DEV-MNT";
    public static final String INPUT = OBJECT + "\npassword: pass";
    public static final String SIGNATURE = "" +
            "-----BEGIN PGP SIGNATURE-----\n" +
            "Version: GnuPG v1.4.9 (SunOS)\n" +
            "\n" +
            "iEYEARECAAYFAk/FbSMACgkQsAWoDcAb7KJmJgCfe2PjxUFIeHycZ85jteosU1ez\n" +
            "kL0An3ypg8F75jlPyTYIUuiCQEcP/9sz\n" +
            "=j7tD\n" +
            "-----END PGP SIGNATURE-----";

    @Test
    public void empty_message() {
        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(""));

        assertThat(paragraphs, hasSize(0));
    }

    @Test
    public void single_paragraph_unsigned() {
        final ContentWithCredentials contentWithCredentials = new ContentWithCredentials("" +
                "mntner: DEV-MNT\n" +
                "password: pass\n");

        final List<Paragraph> paragraphs = subject.createParagraphs(contentWithCredentials);

        assertThat(paragraphs, hasSize(1));
        assertParagraph(paragraphs.get(0), "mntner: DEV-MNT", new PasswordCredential("pass"));
    }

    @Test
    public void multiple_paragraphs_unsigned() {
        final String content1 = "" +
                "mntner: DEV-MNT\n" +
                "password: pass";

        final String content2 = "mntner: DEV2-MNT";

        final ContentWithCredentials contentWithCredentials = new ContentWithCredentials(content1 + "\n\n" + content2);

        final List<Paragraph> paragraphs = subject.createParagraphs(contentWithCredentials);

        assertThat(paragraphs, hasSize(2));
        assertParagraph(paragraphs.get(0), "mntner: DEV-MNT", new PasswordCredential("pass"));
        assertParagraph(paragraphs.get(1), "mntner: DEV2-MNT", new PasswordCredential("pass"));
    }

    @Test
    public void single_paragraph_signed() {
        final String content = "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE;

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));
        assertThat(paragraphs, hasSize(1));

        assertParagraph(
                paragraphs.get(0), "mntner: DEV-MNT",
                PgpCredential.createOfferedCredential(content),
                new PasswordCredential("pass"));
    }

    @Test
    public void multiple_paragraphs_signed() {
        final String content = "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE;
        final ContentWithCredentials contentWithCredentials = new ContentWithCredentials(content + "\n\n" + content);

        final List<Paragraph> paragraphs = subject.createParagraphs(contentWithCredentials);
        assertThat(paragraphs, hasSize(2));

        for (final Paragraph paragraph : paragraphs) {
            assertParagraph(paragraph, "mntner: DEV-MNT", new PasswordCredential("pass"), PgpCredential.createOfferedCredential(content));
        }
    }

    @Test
    public void multiple_paragraphs_mixed() {
        final String content1 = "" +
                "mntner: DEV1-MNT\n" +
                "password: pw";

        final String content2 = "mntner: DEV2-MNT";

        final String content = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE + "\n\n" +
                content1 + "\n\n" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE + "\n\n" +
                content2;

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));
        assertThat(paragraphs, hasSize(4));

        final PasswordCredential pass = new PasswordCredential("pass");
        final PasswordCredential pw = new PasswordCredential("pw");
        final PgpCredential pgpCredential = PgpCredential.createOfferedCredential(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE + "\n\n");

        assertParagraph(paragraphs.get(0), "mntner: DEV-MNT", pass, pw, pgpCredential);
        assertParagraph(paragraphs.get(1), "mntner: DEV1-MNT", pass, pw);
        assertParagraph(paragraphs.get(2), "mntner: DEV-MNT", pass, pw, pgpCredential);
        assertParagraph(paragraphs.get(3), "mntner: DEV2-MNT", pass, pw);
    }

    @Test
    public void override() throws Exception {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "override: some override";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));
        assertThat(paragraphs, hasSize(1));
        assertParagraph(paragraphs.get(0), "mntner: DEV-MNT", OverrideCredential.parse("some override"));
    }

    @Test
    public void password_with_whitespace() throws Exception {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "password:    \t     123 and something   \t \r\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));

        assertThat(paragraphs, hasSize(1));
        assertParagraph(paragraphs.get(0), "mntner: DEV-MNT", new PasswordCredential("123 and something"));
    }

    @Test
    public void single_content_multiple_passwords() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT\n" +
                        "descr: DEV maintainer\n" +
                        "password: pass1\n" +
                        "password: pass2\n" +
                        "password: pass2\n"));

        assertThat(paragraphs, hasSize(1));
        final Paragraph paragraph = paragraphs.get(0);
        assertThat(paragraph.getCredentials().all(), hasSize(2));
        assertThat(paragraph.getCredentials().ofType(PasswordCredential.class), containsInAnyOrder(new PasswordCredential("pass1"), new PasswordCredential("pass2")));
    }

    @Test
    public void invalid_password() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT\n" +
                        "descr: DEV maintainer\n" +
                        " password: pass1\n"));

        assertThat(paragraphs, hasSize(1));
        final Paragraph paragraph = paragraphs.get(0);
        assertThat(paragraph.getCredentials().all(), hasSize(0));
    }

    @Test
    public void multiple_passwords_in_different_paragraphs() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT\n" +
                        "password: password1\n\n" +
                        "mntner: DEV-MNT2\n" +
                        "password: password2"));

        assertThat(paragraphs, hasSize(2));
        final Credential[] expectedCredentials = {new PasswordCredential("password1"), new PasswordCredential("password2")};

        assertThat(paragraphs.get(0).getCredentials().all(), contains(expectedCredentials));
        assertThat(paragraphs.get(1).getCredentials().all(), contains(expectedCredentials));
    }

    @Test
    public void override_before() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("override: override\nmntner: DEV-MNT\n\nmntner: DEV-MNT"));

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains((Credential) OverrideCredential.parse("override")));
        assertThat(paragraphs.get(1).getCredentials().all(), hasSize(0));
    }

    @Test
    public void override_after() {
        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials("" +
                "mntner: DEV-MNT\n" +
                "override: override\n" +
                "\n" +
                "mntner: DEV-MNT"));

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains((Credential) OverrideCredential.parse("override")));
        assertThat(paragraphs.get(1).getCredentials().all(), hasSize(0));
    }

    @Test
    public void override_multiple() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT\n" +
                        "override: override1\n" +
                        "override: override2\n" +
                        "\n" +
                        "mntner: DEV-MNT"));

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains((Credential) OverrideCredential.parse("override1"), OverrideCredential.parse("override2")));
        assertThat(paragraphs.get(1).getCredentials().all(), hasSize(0));
    }

    @Test
    public void override_multiple_paragraphs() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT1\n" +
                        "override: override1\n" +
                        "override: override2\n" +
                        "\n" +
                        "mntner: DEV-MNT2\n" +
                        "override: override3\n"));

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT1"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains((Credential) OverrideCredential.parse("override1"), OverrideCredential.parse("override2")));
        assertThat(paragraphs.get(1).getCredentials().all(), contains((Credential) OverrideCredential.parse("override3")));
    }

    @Test(timeout = 2000)
    public void testPerformance() throws Exception {
        // Note: prevously, we had a regexp matcher that took unacceptable time to finish (>10 minutes).
        // Hint: don't try to match massive input with DOTALL and .*? - it will be too slow
        final String content = IOUtils.toString(new ClassPathResource("testMail/giantRawUnsignedObject").getInputStream());
        subject.createParagraphs(new ContentWithCredentials(content + "\n\n" + content));
    }

    private void assertParagraph(final Paragraph paragraph, final String content, final Credential... credentials) {
        assertThat(paragraph.getContent(), is(content));
        assertThat(paragraph.getCredentials().all(), containsInAnyOrder(credentials));
    }

    @Test
    public void multiple_paragraphs_password_attribute_removed_completely() throws Exception {
        final String content = "" +
                "mntner:one\n" +
                "password: one\n" +
                "source: RIPE\n" +
                "\n" +
                "password: two\n" +
                "\n" +
                "mntner:two\n" +
                "source:RIPE\n" +
                "password:three\n" +
                "\n" +
                "mntner:three\n" +
                "source:RIPE\n" +
                "password:four";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));

        assertThat(paragraphs.size(), is(4));
        assertThat(paragraphs.get(0).getContent(), is("mntner:one\nsource: RIPE"));
        assertThat(paragraphs.get(1).getContent(), is(""));
        assertThat(paragraphs.get(2).getContent(), is("mntner:two\nsource:RIPE"));
        assertThat(paragraphs.get(3).getContent(), is("mntner:three\nsource:RIPE"));
        assertThat(paragraphs.get(3).getCredentials().all(), hasSize(4));
    }

    @Test
    public void multiple_paragraphs_password_attribute_removed_completely_windows_lineending() throws Exception {
        final String content = "" +
                "mntner:one\r\n" +
                "password: one\r\n" +
                "source: RIPE\r\n" +
                "\r\n" +
                "password: two\r\n" +
                "\r\n" +
                "mntner:two\r\n" +
                "source:RIPE\r\n" +
                "password:three\r\n" +
                "\r\n" +
                "mntner:three\r\n" +
                "source:RIPE\r\n" +
                "password:four";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));

        assertThat(paragraphs.size(), is(4));
        assertThat(paragraphs.get(0).getContent(), is("mntner:one\nsource: RIPE"));
        assertThat(paragraphs.get(1).getContent(), is(""));
        assertThat(paragraphs.get(2).getContent(), is("mntner:two\nsource:RIPE"));
        assertThat(paragraphs.get(3).getContent(), is("mntner:three\nsource:RIPE"));
        assertThat(paragraphs.get(3).getCredentials().all(), hasSize(4));
    }

    @Test
    public void multiple_paragraphs_override_attribute_removed_completely() throws Exception {
        final String content = "" +
                "mntner:one\n" +
                "override: one\n" +
                "source: RIPE\n" +
                "\n" +
                "override: two\n" +
                "\n" +
                "mntner:two\n" +
                "source: RIPE\n" +
                "override: three\n\n" +
                "mntner:three\n" +
                "source:RIPE\n" +
                "override:three";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));

        assertThat(paragraphs.size(), is(4));
        assertThat(paragraphs.get(0).getContent(), is("mntner:one\nsource: RIPE"));
        assertThat(paragraphs.get(1).getContent(), is(""));
        assertThat(paragraphs.get(2).getContent(), is("mntner:two\nsource: RIPE"));
        assertThat(paragraphs.get(3).getContent(), is("mntner:three\nsource:RIPE"));
    }

    @Test
    public void signed_message() throws Exception {
        final String content = "" +
                "\n" +
                "\n" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "mntner:      SHRYANE-MNT\n" +
                "descr:       description\n" +
                "admin-c:     AA1-TEST\n" +
                "upd-to:      eshryane@ripe.net\n" +
                "auth:        MD5-PW $1$8Lm6as7E$ZwbUWIP3BfNAHjhS/RGHi.\n" +
                "auth:        PGPKEY-28F6CD6C\n" +
                "mnt-by:      SHRYANE-MNT\n" +
                "referral-by: SHRYANE-MNT\n" +
                "changed:     eshryane@ripe.net 20120212\n" +
                "remarks:     3\n" +
                "source:      RIPE\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJQj5F3AAoJEO6ZHuIo9s1sY1wIAKHMlozy+DDRPmMOVb/YrfrV\n" +
                "Y++tC3ZWhCWpPGScAWpTryzoKaHolzHZPyjdVPkSQWvEFiYTMuNHRjciOKd3szEE\n" +
                "qZmH3Lp86h9eEReXET6DRnLUvsEE/EsZv0SmhuPKCTGEoj05+/Io42S93tMn49f4\n" +
                "5K40BbEVWf8mlm5kChiM9Jfedcus9cAHT8iPQebpnOMaDyBFPYv3sbIfYPaZ18l/\n" +
                "NrMGhwAPxnGy/MgFl6UAdS+EgZHprTbLKnxiqX89BrC7+VJgYYII7nxjinYzJzD9\n" +
                "Y/Oo15iqHRecEYlHbttEgkpDiap4NjKRK8ZWrRyd6UZqcfcYcroNRDfZhWkIQdM=\n" +
                "=5MfA\n" +
                "-----END PGP SIGNATURE-----\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));

        assertThat(paragraphs.size(), is(1));
        assertThat(paragraphs.get(0).getContent(), is("" +
                "mntner:      SHRYANE-MNT\n" +
                "descr:       description\n" +
                "admin-c:     AA1-TEST\n" +
                "upd-to:      eshryane@ripe.net\n" +
                "auth:        MD5-PW $1$8Lm6as7E$ZwbUWIP3BfNAHjhS/RGHi.\n" +
                "auth:        PGPKEY-28F6CD6C\n" +
                "mnt-by:      SHRYANE-MNT\n" +
                "referral-by: SHRYANE-MNT\n" +
                "changed:     eshryane@ripe.net 20120212\n" +
                "remarks:     3\n" +
                "source:      RIPE"));

        assertThat(paragraphs.get(0).getCredentials().all(), hasSize(1));
        assertThat(paragraphs.get(0).getCredentials().all(),
                containsInAnyOrder((Credential) PgpCredential.createOfferedCredential(content)));
    }

    @Test
    public void double_signed_message() {
        final String firstSigned = "" +
                "- -----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST\n" +
                "- -----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJQ3V6oAAoJELvMuy1XY5UNXBwH/jzbWzYRrMoKKZemp1MwKFlj\n" +
                "7zcpiiwieX1LNAIeMg5xx2pBQD3+9ta+TOhVU7IoH3gXTO34djbJILtSBtYMBCK1\n" +
                "vfh9Lid6A+p8IuZt3/DHYdHq9ORRqKemrKig6y4jDNBfpsQKtInRAmEkh0tiZPTb\n" +
                "Dp40IN9T3wJ6/TwmsqQqea4qw3lHtJeXgHPSU2DjwEGriegH5zO9sixClJeh1E6A\n" +
                "wTOBGpJ9CkUVRrvXO5IoEuLUqQqejlhDVycDlVOCak3xuUg2BtGypXHz3YW/0oIu\n" +
                "slQ254zHaDNMULbEz7s+jH4aaaImAscCyo0w0BjI+0zS8Z3n4cxeftiRrMeoxhY=\n" +
                "=GZMp\n" +
                "- -----END PGP SIGNATURE-----\n";

        final String secondSigned = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n\n" +
                firstSigned +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJQ3V63AAoJEHbKke+BzPl9m6gH/17D1AswMbbJQHyXFNSHEbj3\n" +
                "h1FL9yUKFzLGnsuZW3mIHjcBPqFCR/7lJpyflVu5htAw1bguxLk1/Ilg/u1++bAR\n" +
                "pr7SXdhP3DQXlQna139QHd0KLbxi/CLwao7PyP/XBMo/fEqA/KUar0jkJyl6xIvj\n" +
                "i4sSu3dod9OycdScDTqH7K1Ejs/sWTVRHghrKWECyMpTp9HJhsEvljZFtpaRGQkd\n" +
                "s1EbnzDV/uhUN+stJav+i6YT4UXwsW5KfyjG2MTAsx/26k+LTtQ54wh7A2h2yZQa\n" +
                "NjCSNd0cbEE7+G3lI0d+Gs43KXgT1pcrW+BLh3uje5rVp9y2S1fvii6y+9+RKKI=\n" +
                "=Dowb\n" +
                "-----END PGP SIGNATURE-----";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(secondSigned));

        assertThat(paragraphs.size(), is(1));
        assertThat(paragraphs.get(0).getContent(), is("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST"));

        assertThat(paragraphs.get(0).getCredentials().ofType(PgpCredential.class), hasSize(2));
    }

    @Test
    public void triple_signed_message() {
        final String firstSigned = "" +
                "- - -----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST\n" +
                "- - -----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJQ3V6oAAoJELvMuy1XY5UNXBwH/jzbWzYRrMoKKZemp1MwKFlj\n" +
                "7zcpiiwieX1LNAIeMg5xx2pBQD3+9ta+TOhVU7IoH3gXTO34djbJILtSBtYMBCK1\n" +
                "vfh9Lid6A+p8IuZt3/DHYdHq9ORRqKemrKig6y4jDNBfpsQKtInRAmEkh0tiZPTb\n" +
                "Dp40IN9T3wJ6/TwmsqQqea4qw3lHtJeXgHPSU2DjwEGriegH5zO9sixClJeh1E6A\n" +
                "wTOBGpJ9CkUVRrvXO5IoEuLUqQqejlhDVycDlVOCak3xuUg2BtGypXHz3YW/0oIu\n" +
                "slQ254zHaDNMULbEz7s+jH4aaaImAscCyo0w0BjI+0zS8Z3n4cxeftiRrMeoxhY=\n" +
                "=GZMp\n" +
                "- - -----END PGP SIGNATURE-----\n";
        final String secondSigned = "" +
                "- -----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n\n" +
                firstSigned +
                "- -----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJQ3V63AAoJEHbKke+BzPl9m6gH/17D1AswMbbJQHyXFNSHEbj3\n" +
                "h1FL9yUKFzLGnsuZW3mIHjcBPqFCR/7lJpyflVu5htAw1bguxLk1/Ilg/u1++bAR\n" +
                "pr7SXdhP3DQXlQna139QHd0KLbxi/CLwao7PyP/XBMo/fEqA/KUar0jkJyl6xIvj\n" +
                "i4sSu3dod9OycdScDTqH7K1Ejs/sWTVRHghrKWECyMpTp9HJhsEvljZFtpaRGQkd\n" +
                "s1EbnzDV/uhUN+stJav+i6YT4UXwsW5KfyjG2MTAsx/26k+LTtQ54wh7A2h2yZQa\n" +
                "NjCSNd0cbEE7+G3lI0d+Gs43KXgT1pcrW+BLh3uje5rVp9y2S1fvii6y+9+RKKI=\n" +
                "=Dowb\n" +
                "- -----END PGP SIGNATURE-----\n";
        final String thirdSigned = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n\n" +
                secondSigned +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJQ3af4AAoJEO6ZHuIo9s1svFMH/1l53/lD1Y/ohIQ7kTg1FMO1\n" +
                "Rklha4mvqvPTuLJsyyifA9STR40tArHBdDUGIGAyufKiBtNo5JSpORSpdjyE+HRD\n" +
                "WIHyicjtGBEfPKUuoCRIahsuMvHyMmcfCDh/knRuiviXSHScXkRjtr+gP+oYwfeA\n" +
                "MfKxNfzZLzXRdfg5FJnFFLnvH1vg+UhOKJCqFBzuI2htE9o+xEj/PFk9keDzI+HZ\n" +
                "S9fujlovRgb36VUZZZAH2PZ9X6Wq1Y0ZQNmGpd2yZwQWdLPjBD5/PRATSwQyhwPu\n" +
                "tL77WF7kVAMapazmY+YViXLzK+H36khuED1PAZIPIVB5CU/xFirraWTBdnOpchM=\n" +
                "=o4kj\n" +
                "-----END PGP SIGNATURE-----";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(thirdSigned));

        assertThat(paragraphs.size(), is(1));
        assertThat(paragraphs.get(0).getContent(), is("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "changed: dbtest@ripe.net 20120101\n" +
                "source:  TEST"));

        assertThat(paragraphs.get(0).getCredentials().ofType(PgpCredential.class), hasSize(3));
    }

    @Test
    public void malformed_pgp_signed_message() {
        final String content = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +                     // no empty line between header and content
                "person:  First Person\n" +
                "address: St James Street\n" +
                "address: Burnley\n" +
                "address: UK\n" +
                "phone:   +44 282 420469\n" +
                "nic-hdl: FP1-TEST\n" +
                "mnt-by:  OWNER-MNT\n" +
                "changed: denis@ripe.net 20121016\n" +
                "source:  TEST\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1.4.12 (Darwin)\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +   // no empty line after headers
                "iQEcBAEBAgAGBQJQwIPwAAoJELvMuy1XY5UNmTgH/3dPZOV5DhEP7qYS9PvgFnK+\n" +
                "fVpmdXnI6IfzGiRrbOJWCpiu+vFT0QzKU22nH/JY7zDH77pjBlOQ5+WLG5/R2XYx\n" +
                "cy35J7HwKwChUg3COEV5XAnmiNxom8FnfimKTPdwNVLBZ6UmVSP5u2ua4uheTclR\n" +
                "71wej5okzHGtOyLVLH6YV1/p4/TNJOG6nDnABrowzsZqIMQ43N1+LHs4kfqyvJux\n" +
                "4xsP+PH9Tqiw1L8wVn/4XefLraawiPMLB1hLgPz6bTcoHXMEY0/BaKBOIkI3d49D\n" +
                "2I65qVJXecj9RSbkLZung8o9ItXzPooEXggQCHHq93EvwCcgKi8s4OTWqUfje5Y=\n" +
                "=it26\n" +
                "-----END PGP SIGNATURE-----";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content));

        assertThat(paragraphs.size(), is(1));
        assertThat(paragraphs.get(0).getContent(), is(content));
        assertThat(paragraphs.get(0).getCredentials().all(), hasSize(0));
    }
}
