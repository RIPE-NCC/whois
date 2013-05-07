package net.ripe.db.whois.api.mail;

import net.ripe.db.whois.update.domain.ContentWithCredentials;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Origin;

import java.util.Iterator;
import java.util.List;

public class MailMessage implements Origin {
    private final String id;
    private final String from;
    private final String subject;
    private final String date;
    private final String replyTo;
    private final Keyword keyword;
    private final List<ContentWithCredentials> contentWithCredentials;

    public MailMessage(final String id, final String from, final String subject, final String date, final String replyTo, final Keyword keyword, List<ContentWithCredentials> contentWithCredentials) {
        this.id = id;
        this.from = from;
        this.subject = subject;
        this.date = date;
        this.replyTo = replyTo;
        this.keyword = keyword;
        this.contentWithCredentials = contentWithCredentials;
    }

    @Override
    public boolean isDefaultOverride() {
        return false;
    }

    @Override
    public boolean allowRipeOperations() {
        return false;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }

    public String getDate() {
        return date;
    }

    public String getReplyTo() {
        return replyTo;
    }

    public String getUpdateMessage() {
        StringBuilder builder = new StringBuilder();

        Iterator<ContentWithCredentials> iterator = contentWithCredentials.iterator();
        while (iterator.hasNext()) {
            builder.append(iterator.next().getContent());
            if (iterator.hasNext()) {
                builder.append("\n\n");
            }
        }
        return builder.toString();
    }

    public List<ContentWithCredentials> getContentWithCredentials() {
        return contentWithCredentials;
    }

    @Override
    public String getFrom() {
        return from;
    }

    public Keyword getKeyword() {
        return keyword;
    }

    @Override
    public String getResponseHeader() {
        return String.format("" +
                ">  From:       %s\n" +
                ">  Subject:    %s\n" +
                ">  Date:       %s\n" +
                ">  Reply-To:   %s\n" +
                ">  Message-ID: %s",
                from,
                subject,
                date,
                replyTo,
                id);
    }

    @Override
    public String getNotificationHeader() {
        return String.format("" +
                "- From:      %s\n" +
                "- Date/Time: %s\n",
                from,
                date);
    }

    @Override
    public String getName() {
        return "email update";
    }

    @Override
    public String toString() {
        return "Mail(" + getId() + ")";
    }
}