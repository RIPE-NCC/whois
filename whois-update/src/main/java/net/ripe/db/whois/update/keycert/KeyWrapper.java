package net.ripe.db.whois.update.keycert;

import java.util.List;

public interface KeyWrapper {
    String getMethod();

    List<String> getOwners();

    String getFingerprint();
}
