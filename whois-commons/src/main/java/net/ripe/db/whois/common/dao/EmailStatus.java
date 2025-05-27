package net.ripe.db.whois.common.dao;

import net.ripe.db.whois.common.mail.EmailStatusType;

import java.time.LocalDateTime;

public record EmailStatus(String email, EmailStatusType emailStatus, LocalDateTime createAt) {

}
