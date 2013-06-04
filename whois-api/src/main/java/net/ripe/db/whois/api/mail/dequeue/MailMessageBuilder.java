package net.ripe.db.whois.api.mail.dequeue;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Keyword;

import java.util.List;

public class MailMessageBuilder {
    private String id;
    private String from = "";
    private String subject = "";
    private String date = "";
    private String replyTo = "";
    private String replyToEmail = "";
    private Keyword keyword = Keyword.NONE;
    private List<ContentWithCredentials> allContentWithCredentials = Lists.newArrayList();

    public MailMessageBuilder id(final String id) {
        this.id = id;
        return this;
    }

    public MailMessageBuilder from(final String from) {
        this.from = from;
        return this;
    }

    public MailMessageBuilder subject(final String subject) {
        this.subject = subject;
        return this;
    }

    public MailMessageBuilder date(final String date) {
        this.date = date;
        return this;
    }

    public MailMessageBuilder replyTo(final String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    public MailMessageBuilder replyToEmail(final String replyToEmail) {
        this.replyToEmail = replyToEmail;
        return this;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public MailMessageBuilder keyword(final Keyword keyword) {
        this.keyword = keyword;
        return this;
    }

    public MailMessageBuilder addContentWithCredentials(final ContentWithCredentials contentWithCredentials) {
        this.allContentWithCredentials.add(contentWithCredentials);
        return this;
    }

    public MailMessage build() {
        return new MailMessage(id, from, subject == null ? "" : subject, date, replyTo, replyToEmail, keyword, allContentWithCredentials);
    }
}
