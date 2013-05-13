package net.ripe.db.whois.update.keycert;

public interface KeyWrapper {
    String getMethod();

    String getOwner();

    String getFingerprint();
}
