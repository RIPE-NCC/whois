package net.ripe.db.whois.query.dao;

import net.ripe.db.whois.common.domain.CIString;

public interface AbuseValidationStatusDao {

    boolean isSuspect(final CIString email);
}
