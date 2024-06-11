package net.ripe.db.whois.common.clientauthcertificates;

import java.util.List;

public interface KeyWrapper {
    String getMethod();

    List<String> getOwners();

    String getFingerprint();
}
