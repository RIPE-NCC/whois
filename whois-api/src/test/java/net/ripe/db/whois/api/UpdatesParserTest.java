package net.ripe.db.whois.api;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.common.credentials.Credential;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.common.credentials.OverrideCredential;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.common.credentials.PasswordCredential;
import net.ripe.db.whois.update.domain.PgpCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdatesParserTest {
    private static final String SOURCE = "RIPE";
    private static final String MNTNER_DEV_MNT = "mntner: DEV-MNT\nsource: " + SOURCE;

    @Mock UpdateContext updateContext;

    @InjectMocks UpdatesParser subject = new UpdatesParser(1000000);

    @Test
    public void no_paragraphs() {
        final List<Update> updates = subject.parse(updateContext, Lists.newArrayList());
        assertThat(updates, hasSize(0));
    }

    @Test
    public void single_paragraph() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.UNSPECIFIED));
        assertThat(update.getDeleteReasons(), hasSize(0));
        assertThat(update.isOverride(), is(false));
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));
        assertThat(update.getParagraph().getContent(), is(MNTNER_DEV_MNT));
        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void multiple_paragraphs() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT));
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(2));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_before() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("delete: reason\n" + MNTNER_DEV_MNT));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(1));

        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains("reason"));
        assertThat(update.isOverride(), is(false));
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_after() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT + "\ndelete: reason"));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(1));

        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains("reason"));
        assertThat(update.isOverride(), is(false));
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_middle() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "delete: reason\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n"));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains("reason"));
        assertThat(update.isOverride(), is(false));
        assertThat(update.getSubmittedObject(), is(RpslObject.parse("" +
                "mntner: UPD-MNT\n" +
                "descr: description\n" +
                "admin-c: TEST-RIPE\n" +
                "mnt-by: UPD-MNT\n" +
                "upd-to: dbtest@ripe.net\n" +
                "auth:   MD5-PW $1$fU9ZMQN9$QQtm3kRqZXWAuLpeOiLN7. # update\n" +
                "source: TEST\n")));

        verify(updateContext, never()).ignore(any(Paragraph.class));
    }

    @Test
    public void delete_multiple() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(MNTNER_DEV_MNT + "\ndelete: reason1\ndelete: reason2"));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.DELETE));
        assertThat(update.getDeleteReasons(), contains("reason1", "reason2"));
        assertThat(update.isOverride(), is(false));
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(MNTNER_DEV_MNT)));
    }

    @Test
    public void broken_override_and_delete_multiple() {
        final String input = MNTNER_DEV_MNT + "\n delete: reason\n override: pw";
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials(input));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
        final Update update = updates.get(0);
        assertThat(update.getOperation(), is(Operation.UNSPECIFIED));
        assertThat(update.getDeleteReasons(), hasSize(0));
        assertThat(update.isOverride(), is(false));
        assertThat(update.getSubmittedObject(), is(RpslObject.parse(input)));
    }

    @Test
    public void no_object() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("some text"));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(0));

        verify(updateContext, times(1)).ignore(any(Paragraph.class));
    }

    @Test
    public void invalid_object() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("1234:5678"));

        final List<Update> updates = subject.parse(updateContext, content);
        assertThat(updates, hasSize(0));

        verify(updateContext, times(1)).ignore(any(Paragraph.class));
    }

    @Test
    public void no_source_still_parses() {
        List<ContentWithCredentials> content = Lists.newArrayList();
        content.add(new ContentWithCredentials("mntner: DEV-MNT"));

        final List<Update> updates = subject.parse(updateContext, content);

        assertThat(updates, hasSize(1));
    }

    public static final String OBJECT = "mntner: DEV-MNT";
    private static final String INPUT = OBJECT + "\npassword: pass";
    private static final String SIGNATURE = "" +
            "-----BEGIN PGP SIGNATURE-----\n" +
            "Version: GnuPG v1.4.9 (SunOS)\n" +
            "\n" +
            "iEYEARECAAYFAk/FbSMACgkQsAWoDcAb7KJmJgCfe2PjxUFIeHycZ85jteosU1ez\n" +
            "kL0An3ypg8F75jlPyTYIUuiCQEcP/9sz\n" +
            "=j7tD\n" +
            "-----END PGP SIGNATURE-----";

    @Test
    public void empty_message() {
        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(""), updateContext);

        assertThat(paragraphs, hasSize(0));
    }

    @Test
    public void single_paragraph_unsigned() {
        final ContentWithCredentials contentWithCredentials = new ContentWithCredentials("" +
                "mntner: DEV-MNT\n" +
                "password: pass\n");

        final List<Paragraph> paragraphs = subject.createParagraphs(contentWithCredentials, updateContext);

        assertThat(paragraphs, hasSize(1));
        assertParagraphNoDryRun(paragraphs.get(0), "mntner: DEV-MNT", new PasswordCredential("pass"));
    }

    @Test
    public void multiple_paragraphs_unsigned() {
        final String content1 = "" +
                "mntner: DEV-MNT\n" +
                "password: pass";

        final String content2 = "mntner: DEV2-MNT";

        final ContentWithCredentials contentWithCredentials = new ContentWithCredentials(content1 + "\n\n" + content2);

        final List<Paragraph> paragraphs = subject.createParagraphs(contentWithCredentials, updateContext);

        assertThat(paragraphs, hasSize(2));
        assertParagraphNoDryRun(paragraphs.get(0), "mntner: DEV-MNT", new PasswordCredential("pass"));
        assertParagraphNoDryRun(paragraphs.get(1), "mntner: DEV2-MNT", new PasswordCredential("pass"));
    }

    @Test
    public void single_paragraph_signed() {
        final String content = "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE;

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(1));

        assertParagraphNoDryRun(
                paragraphs.get(0), "mntner: DEV-MNT",
                PgpCredential.createOfferedCredential(content),
                new PasswordCredential("pass"));
    }

    @Test
    public void multiple_paragraphs_signed() {
        final String content = "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE;
        final ContentWithCredentials contentWithCredentials = new ContentWithCredentials(content + "\n\n" + content);

        final List<Paragraph> paragraphs = subject.createParagraphs(contentWithCredentials, updateContext);
        assertThat(paragraphs, hasSize(2));

        for (final Paragraph paragraph : paragraphs) {
            assertParagraphNoDryRun(paragraph, "mntner: DEV-MNT", new PasswordCredential("pass"), PgpCredential.createOfferedCredential(content));
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

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(4));

        final PasswordCredential pass = new PasswordCredential("pass");
        final PasswordCredential pw = new PasswordCredential("pw");
        final PgpCredential pgpCredential = PgpCredential.createOfferedCredential(
                "-----BEGIN PGP SIGNED MESSAGE-----\n" + "Hash: SHA1\n\n" + INPUT + "\n" + SIGNATURE + "\n\n");

        assertParagraphNoDryRun(paragraphs.get(0), "mntner: DEV-MNT", pass, pw, pgpCredential);
        assertParagraphNoDryRun(paragraphs.get(1), "mntner: DEV1-MNT", pass, pw);
        assertParagraphNoDryRun(paragraphs.get(2), "mntner: DEV-MNT", pass, pw, pgpCredential);
        assertParagraphNoDryRun(paragraphs.get(3), "mntner: DEV2-MNT", pass, pw);
    }

    @Test
    public void override() {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "override: some override";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(1));
        assertParagraphNoDryRun(paragraphs.get(0), "mntner: DEV-MNT", OverrideCredential.parse("some override"));
    }

    @Test
    public void override_with_dryRun() {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "dry-run: some\n" +
                "override: some override";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(1));
        final Paragraph paragraph = paragraphs.get(0);
        assertThat(paragraph.getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraph.getCredentials().all(), containsInAnyOrder(OverrideCredential.parse("some override")));

        verify(updateContext).dryRun();
    }

    @Test
    public void dryRun() {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "dry-run: some dry run";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(1));
        final Paragraph paragraph = paragraphs.get(0);
        assertThat(paragraph.getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraph.getCredentials().all(), hasSize(0));
        verify(updateContext).dryRun();
    }

    @Test
    public void dryRun_detached() {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "\n" +
                "\n" +
                "\n" +
                "dry-run: some dry run";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(2));

        final Paragraph paragraph1 = paragraphs.get(0);
        assertThat(paragraph1.getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraph1.getCredentials().all(), hasSize(0));

        final Paragraph paragraph2 = paragraphs.get(1);
        assertThat(paragraph2.getContent(), is(""));
        assertThat(paragraph2.getCredentials().all(), hasSize(0));

        verify(updateContext).dryRun();
    }

    @Test
    public void dryRun_specified_multiple_times() {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "dry-run: some dry run\n" +
                "dry-run: some dry run\n" +
                "dry-run: some dry run";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(1));
        final Paragraph paragraph = paragraphs.get(0);
        assertThat(paragraph.getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraph.getCredentials().all(), hasSize(0));
        verify(updateContext).dryRun();
    }

    @Test
    public void dryRun_multiple_objects() {
        final String content = "" +
                "mntner: DEV1-MNT\n" +
                "dry-run: some dry run\n" +
                "\n" +
                "mntner: DEV2-MNT\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(2));

        final Paragraph paragraph1 = paragraphs.get(0);
        assertThat(paragraph1.getContent(), is("mntner: DEV1-MNT"));
        assertThat(paragraph1.getCredentials().all(), hasSize(0));

        final Paragraph paragraph2 = paragraphs.get(1);
        assertThat(paragraph2.getContent(), is("mntner: DEV2-MNT"));
        assertThat(paragraph2.getCredentials().all(), hasSize(0));

        verify(updateContext).dryRun();
    }

    @Test
    public void dryrun_anywhere() {
        final String content = "" +
                "dry-run:\n" +
                "mntner: DEV1-MNT\n" +
                "password:pwd\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(1));

        final Paragraph paragraph = paragraphs.get(0);
        assertThat(paragraph.getContent(), is("mntner: DEV1-MNT"));

        verify(updateContext).dryRun();
    }

    @Test
    public void maximum_object_size_exceeded() {
        final StringBuilder sb = new StringBuilder();
        while (sb.length() < 2_000_000) {
            sb.append("mntner: DEV1-MNT\n");
        }

        subject.parse(updateContext, Lists.newArrayList(new ContentWithCredentials(sb.toString())));

        verify(updateContext).addGlobalMessage(eq(UpdateMessages.maximumObjectSizeExceeded(sb.length() - 1, 1_000_000)));
    }

    @Test
    public void dryrun_removal_leaves_no_blankline() {
        final String content = "" +
                "person:  First Person\n" +
                "address: Burnley\n" +
                "dry-run:\n" +
                "nic-hdl: TEST-TEST\n" +
                "source:  TEST\n" +
                "\n" +
                "password:   owner";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);
        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("person:  First Person\n" +
                "address: Burnley\n" +
                "nic-hdl: TEST-TEST\n" +
                "source:  TEST"));
        assertThat(paragraphs.get(1).getContent(), is(""));
    }

    @Test
    public void password_with_whitespace() {
        final String content = "" +
                "mntner: DEV-MNT\n" +
                "password:    \t     123 and something   \t \r\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(1));
        assertParagraphNoDryRun(paragraphs.get(0), "mntner: DEV-MNT", new PasswordCredential("123 and something"));
    }

    @Test
    public void single_content_multiple_passwords() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT\n" +
                        "descr: DEV maintainer\n" +
                        "password: pass1\n" +
                        "password: pass2\n" +
                        "password: pass2\n"), updateContext);

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
                        " password: pass1\n"), updateContext);

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
                        "password: password2"), updateContext);

        assertThat(paragraphs, hasSize(2));
        final Credential[] expectedCredentials = {new PasswordCredential("password1"), new PasswordCredential("password2")};

        assertThat(paragraphs.get(0).getCredentials().all(), contains(expectedCredentials));
        assertThat(paragraphs.get(1).getCredentials().all(), contains(expectedCredentials));
    }

    @Test
    public void override_before() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("override: override\nmntner: DEV-MNT\n\nmntner: DEV-MNT"), updateContext);

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains(OverrideCredential.parse("override")));
        assertThat(paragraphs.get(1).getCredentials().all(), hasSize(0));
    }

    @Test
    public void override_after() {
        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials("" +
                "mntner: DEV-MNT\n" +
                "override: override\n" +
                "\n" +
                "mntner: DEV-MNT"), updateContext);

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains(OverrideCredential.parse("override")));
        assertThat(paragraphs.get(1).getCredentials().all(), hasSize(0));
    }

    @Test
    public void override_multiple() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT\n" +
                        "override: denis,override1\n" +
                        "override: override2\n" +
                        "\n" +
                        "mntner: DEV-MNT"), updateContext);

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains(OverrideCredential.parse("denis,override1"), OverrideCredential.parse("override2")));
        assertThat(paragraphs.get(1).getCredentials().all(), hasSize(0));
    }

    @Test
    public void override_multiple_paragraphs() {
        final List<Paragraph> paragraphs = subject.createParagraphs(
                new ContentWithCredentials("" +
                        "mntner: DEV-MNT1\n" +
                        "override: denis,override1\n" +
                        "override: override2\n" +
                        "\n" +
                        "mntner: DEV-MNT2\n" +
                        "override: override3\n"), updateContext);

        assertThat(paragraphs, hasSize(2));
        assertThat(paragraphs.get(0).getContent(), is("mntner: DEV-MNT1"));
        assertThat(paragraphs.get(0).getCredentials().all(), contains(OverrideCredential.parse("denis,override1"), OverrideCredential.parse("override2")));
        assertThat(paragraphs.get(1).getCredentials().all(), contains(OverrideCredential.parse("override3")));
    }

    @Test
    @Timeout(value = 2000, unit = TimeUnit.MILLISECONDS)
    public void testPerformance() throws Exception {
        // Note: prevously, we had a regexp matcher that took unacceptable time to finish (>10 minutes).
        // Hint: don't try to match massive input with DOTALL and .*? - it will be too slow
        final String content = IOUtils.toString(new ClassPathResource("testMail/giantRawUnsignedObject").getInputStream(), Charset.defaultCharset());
        subject.createParagraphs(new ContentWithCredentials(content + "\n\n" + content), updateContext);
    }

    private void assertParagraphNoDryRun(final Paragraph paragraph, final String content, final Credential... credentials) {
        assertThat(paragraph.getContent(), is(content));
        assertThat(paragraph.getCredentials().all(), containsInAnyOrder(credentials));

        verify(updateContext, never()).dryRun();
    }

    @Test
    public void multiple_paragraphs_password_attribute_removed_completely() {
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

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(4));
        assertThat(paragraphs.get(0).getContent(), is("mntner:one\nsource: RIPE"));
        assertThat(paragraphs.get(1).getContent(), is(""));
        assertThat(paragraphs.get(2).getContent(), is("mntner:two\nsource:RIPE"));
        assertThat(paragraphs.get(3).getContent(), is("mntner:three\nsource:RIPE"));
        assertThat(paragraphs.get(3).getCredentials().all(), hasSize(4));
    }

    @Test
    public void multiple_paragraphs_password_attribute_removed_completely_windows_lineending() {
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

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(4));
        assertThat(paragraphs.get(0).getContent(), is("mntner:one\nsource: RIPE"));
        assertThat(paragraphs.get(1).getContent(), is(""));
        assertThat(paragraphs.get(2).getContent(), is("mntner:two\nsource:RIPE"));
        assertThat(paragraphs.get(3).getContent(), is("mntner:three\nsource:RIPE"));
        assertThat(paragraphs.get(3).getCredentials().all(), hasSize(4));
    }

    @Test
    public void multiple_paragraphs_override_attribute_removed_completely() {
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

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(4));
        assertThat(paragraphs.get(0).getContent(), is("mntner:one\nsource: RIPE"));
        assertThat(paragraphs.get(1).getContent(), is(""));
        assertThat(paragraphs.get(2).getContent(), is("mntner:two\nsource: RIPE"));
        assertThat(paragraphs.get(3).getContent(), is("mntner:three\nsource:RIPE"));
    }

    @Test
    public void signed_message() {
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
                "remarks:     3\n" +
                "source:      RIPE\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJVXytBAAoJELvMuy1XY5UNcSkH/3FzhXBSdyYLK0CQjUOAJ5Te\n" +
                "xR5h69m1MMvBJXfFxpZN34renHxrQKypN0L09wVEKfvUYRPB6u1d/LqATjEOd5cV\n" +
                "Li6V4AvOx+Kd2IEpWUaXw2rO/v02Iw3d6rXMy3dnl8XN0xFDkGxMXO1jPpGmfL8j\n" +
                "WXTtKxt9Vcpp9WRkFix0jtMPlvNId4Gy3NOEm70v25fm8DO+X8Lp+GU9Ko4u5VC1\n" +
                "nPPgO0EH4eWtpaJFQAIFrHzQRa8HxFNsXzjYuFV4B5WO2aGTgGD3eexRAFPGczMG\n" +
                "z8paI/+V51hgi7uF1I2XzT/NndD/NG7SmrZD0I4DP5LO7TUv4UueB4qz/MRwE/o=\n" +
                "=LbbV\n" +
                "-----END PGP SIGNATURE-----\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(1));
        assertThat(paragraphs.get(0).getContent(), is("" +
                "mntner:      SHRYANE-MNT\n" +
                "descr:       description\n" +
                "admin-c:     AA1-TEST\n" +
                "upd-to:      eshryane@ripe.net\n" +
                "auth:        MD5-PW $1$8Lm6as7E$ZwbUWIP3BfNAHjhS/RGHi.\n" +
                "auth:        PGPKEY-28F6CD6C\n" +
                "mnt-by:      SHRYANE-MNT\n" +
                "remarks:     3\n" +
                "source:      RIPE"));

        assertThat(paragraphs.get(0).getCredentials().all(), hasSize(1));
        assertThat(paragraphs.get(0).getCredentials().all(),
                containsInAnyOrder(PgpCredential.createOfferedCredential(content)));
    }

    @Test
    public void double_signed_message() {
        final String content = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
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
                "source:  TEST\n" +
                "- -----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJVXy2AAAoJELvMuy1XY5UN2EIH/2zL1L/d86Eoh7bZOSvO/lgk\n" +
                "v0TgTnvYAC/KFK+hL5CMe9bpxoChTDiVTQ4WOD+7dvkFbrXE+bG6rBfxuGz4eANz\n" +
                "24Wck/6e1OMLtuQsinkjsc7j7QfkldMF3wqHpQQyX2TpOi0zdn5XMXc5vC5KpeOX\n" +
                "R1XlE2Jr/8WWraOYfJS8PSfsenDjbIUtLABNS/5xXHbthh7Hn+4SSgNlsPS11pxj\n" +
                "pl+Xl0XLyUYJs6/Eq7mbsjfk29fYl+ESNKUPzLMc0LTTqgRUtL3Z8EVcgxZOZrz1\n" +
                "pjbi+CjLqMgrCS7krYvCcA60R6mO/ag+zC1OjQUvn38VDiDWg3zSiS1NCQyVeZU=\n" +
                "=xeQe\n" +
                "- -----END PGP SIGNATURE-----\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJVXy2EAAoJELvMuy1XY5UNbNQH/i4ZeTQ27IcYQ7CSyaUEil1p\n" +
                "WUpExiVZt/cXFtZVpDDVQVkPf9jBYB7Y06k70/4QD+ItOsL6m+JJMvbUAbHgpDfC\n" +
                "fLB6OjgUbY4qhlW3a1QRDza+CNAOC9ldVaVcXs3LZJr9WLYwHPMfFha/Ar3RtOeo\n" +
                "tQd99ZsiQ5HswjmtrL+sHHzJ3VnT0FyjMskpE6yk+5szp389KjFw87HT0jvGT5zj\n" +
                "ysunmUoq8b253oeeWvM3mLhgDPFRlGloOJGwVYFNz/HuxukKtm6LGCi3GBORX12q\n" +
                "DpeIJbppxGpcCHaesC1KMReltUjun/AKaxd92anuwYPe6kAUk6QeCuy6JmvF7Pc=\n" +
                "=imcM\n" +
                "-----END PGP SIGNATURE-----\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(1));
        assertThat(paragraphs.get(0).getContent(), is("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "source:  TEST"));

        assertThat(paragraphs.get(0).getCredentials().ofType(PgpCredential.class), hasSize(2));
    }

    @Test
    public void triple_signed_message() {
        final String content = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
                "- -----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +
                "\n" +
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
                "source:  TEST\n" +
                "- - -----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJVXy2AAAoJELvMuy1XY5UN2EIH/2zL1L/d86Eoh7bZOSvO/lgk\n" +
                "v0TgTnvYAC/KFK+hL5CMe9bpxoChTDiVTQ4WOD+7dvkFbrXE+bG6rBfxuGz4eANz\n" +
                "24Wck/6e1OMLtuQsinkjsc7j7QfkldMF3wqHpQQyX2TpOi0zdn5XMXc5vC5KpeOX\n" +
                "R1XlE2Jr/8WWraOYfJS8PSfsenDjbIUtLABNS/5xXHbthh7Hn+4SSgNlsPS11pxj\n" +
                "pl+Xl0XLyUYJs6/Eq7mbsjfk29fYl+ESNKUPzLMc0LTTqgRUtL3Z8EVcgxZOZrz1\n" +
                "pjbi+CjLqMgrCS7krYvCcA60R6mO/ag+zC1OjQUvn38VDiDWg3zSiS1NCQyVeZU=\n" +
                "=xeQe\n" +
                "- - -----END PGP SIGNATURE-----\n" +
                "- -----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJVXy2EAAoJELvMuy1XY5UNbNQH/i4ZeTQ27IcYQ7CSyaUEil1p\n" +
                "WUpExiVZt/cXFtZVpDDVQVkPf9jBYB7Y06k70/4QD+ItOsL6m+JJMvbUAbHgpDfC\n" +
                "fLB6OjgUbY4qhlW3a1QRDza+CNAOC9ldVaVcXs3LZJr9WLYwHPMfFha/Ar3RtOeo\n" +
                "tQd99ZsiQ5HswjmtrL+sHHzJ3VnT0FyjMskpE6yk+5szp389KjFw87HT0jvGT5zj\n" +
                "ysunmUoq8b253oeeWvM3mLhgDPFRlGloOJGwVYFNz/HuxukKtm6LGCi3GBORX12q\n" +
                "DpeIJbppxGpcCHaesC1KMReltUjun/AKaxd92anuwYPe6kAUk6QeCuy6JmvF7Pc=\n" +
                "=imcM\n" +
                "- -----END PGP SIGNATURE-----\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +
                "\n" +
                "iQEcBAEBAgAGBQJVXy2yAAoJELvMuy1XY5UNo9MH/iQhTzsiY0Z66e3f0YKQNq2y\n" +
                "wws1+eh1cICMnRBioPyY6CoHzfiTiQRIz7ctFHTg2Nylmn9cz54CZLqZlPM9RFEQ\n" +
                "g/dV+CwVwNWGAJDq/krrvjN7dae4Kb0Kkf3sy+YbIXPVooVVVcoDZRRsB8yJHLm+\n" +
                "zHqhq7fdCvgbSigr+uKV73QwPbZA3/h1WMG+kWvONxOQhWkEVuR1QWi/YI8uTHjt\n" +
                "1+5YTsZkvC/skjIvIO7iIFhxSLR8mq3kdIkyhVOTpORtfGuuTf5idUnu2pad5Y9u\n" +
                "U6PD8iB1dCatjlsUhqMzunNa0sbbizDZPSvSKlPQ+CJAHrGglfd1hgqFKJxmqEY=\n" +
                "=aaHZ\n" +
                "-----END PGP SIGNATURE-----\n";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(1));
        assertThat(paragraphs.get(0).getContent(), is("" +
                "person:  Admin Person\n" +
                "address: Admin Road\n" +
                "address: Town\n" +
                "address: UK\n" +
                "phone:   +44 282 411141\n" +
                "nic-hdl: TEST-RIPE\n" +
                "mnt-by:  ADMIN-MNT\n" +
                "source:  TEST"));

        assertThat(paragraphs.get(0).getCredentials().ofType(PgpCredential.class), hasSize(3));
    }

    @Test
    public void malformed_pgp_signed_message() {
        final String content = "" +
                "-----BEGIN PGP SIGNED MESSAGE-----\n" +
                "Hash: SHA1\n" +                       // no empty line between header and content
                "mntner:      SHRYANE-MNT\n" +
                "descr:       description\n" +
                "admin-c:     AA1-TEST\n" +
                "upd-to:      eshryane@ripe.net\n" +
                "auth:        MD5-PW $1$8Lm6as7E$ZwbUWIP3BfNAHjhS/RGHi.\n" +
                "auth:        PGPKEY-28F6CD6C\n" +
                "mnt-by:      SHRYANE-MNT\n" +
                "remarks:     3\n" +
                "source:      RIPE\n" +
                "-----BEGIN PGP SIGNATURE-----\n" +
                "Version: GnuPG v1\n" +
                "Comment: GPGTools - http://gpgtools.org\n" +   // no empty line after headers
                "iQEcBAEBAgAGBQJVXytBAAoJELvMuy1XY5UNcSkH/3FzhXBSdyYLK0CQjUOAJ5Te\n" +
                "xR5h69m1MMvBJXfFxpZN34renHxrQKypN0L09wVEKfvUYRPB6u1d/LqATjEOd5cV\n" +
                "Li6V4AvOx+Kd2IEpWUaXw2rO/v02Iw3d6rXMy3dnl8XN0xFDkGxMXO1jPpGmfL8j\n" +
                "WXTtKxt9Vcpp9WRkFix0jtMPlvNId4Gy3NOEm70v25fm8DO+X8Lp+GU9Ko4u5VC1\n" +
                "nPPgO0EH4eWtpaJFQAIFrHzQRa8HxFNsXzjYuFV4B5WO2aGTgGD3eexRAFPGczMG\n" +
                "z8paI/+V51hgi7uF1I2XzT/NndD/NG7SmrZD0I4DP5LO7TUv4UueB4qz/MRwE/o=\n" +
                "=LbbV\n" +
                "-----END PGP SIGNATURE-----";

        final List<Paragraph> paragraphs = subject.createParagraphs(new ContentWithCredentials(content), updateContext);

        assertThat(paragraphs, hasSize(1));
        assertThat(paragraphs.get(0).getContent(), is(content));
        assertThat(paragraphs.get(0).getCredentials().all(), hasSize(0));
    }
}
