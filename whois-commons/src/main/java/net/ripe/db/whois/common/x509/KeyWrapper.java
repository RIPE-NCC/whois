package net.ripe.db.whois.common.x509;

import java.util.List;

public interface KeyWrapper {
    String getMethod();

    List<String> getOwners();

    String getFingerprint();
}
