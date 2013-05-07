package net.ripe.db.whois.api.mail.dao;

import net.ripe.db.whois.update.domain.DequeueStatus;

import javax.mail.internet.MimeMessage;

public interface MailMessageDao {
    String claimMessage();

    void addMessage(MimeMessage message);

    MimeMessage getMessage(String messageUuid);

    void deleteMessage(String messageUuid);

    void setStatus(String messageUuid, DequeueStatus status);
}
