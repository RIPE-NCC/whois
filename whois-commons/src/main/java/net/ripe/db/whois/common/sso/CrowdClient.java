package net.ripe.db.whois.common.sso;

public interface CrowdClient {

    public String getUuid(String username);

    public String getUsername(String uuid);
}
