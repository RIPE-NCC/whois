package net.ripe.db.whois.api.mail;

import com.google.common.collect.Lists;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Keyword;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MailMessageTest {
    @Test
    public void getUpdateMessage() {
        List<ContentWithCredentials> contentWithCredentialsList = Lists.newArrayList();
        contentWithCredentialsList.add(new ContentWithCredentials("password: some password\nmntner: TST-MNT"));
        contentWithCredentialsList.add(new ContentWithCredentials("password: another password\nmntner: TST2-MNT"));

        final MailMessage subject = new MailMessage("id", "from", "subject", "date", "replyTo", "replyToEmail", Keyword.NONE, contentWithCredentialsList);

        assertThat(subject.getUpdateMessage(), is("" +
                "password: some password\n" +
                "mntner: TST-MNT\n" +
                "\n" +
                "password: another password\n" +
                "mntner: TST2-MNT"));
    }
}
