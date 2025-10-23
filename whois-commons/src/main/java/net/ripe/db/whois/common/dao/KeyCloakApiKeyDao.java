package net.ripe.db.whois.common.dao;

import java.time.LocalDate;
import java.util.Map;

public interface KeyCloakApiKeyDao {
    Map<String, LocalDate> getAllKeyIdWithExpiry();
}
