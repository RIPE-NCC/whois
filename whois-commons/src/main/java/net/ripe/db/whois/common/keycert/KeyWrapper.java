package net.ripe.db.whois.common.keycert;

import java.util.List;

public interface KeyWrapper {
    String getMethod();

    List<String> getOwners();

    String getFingerprint();
}
