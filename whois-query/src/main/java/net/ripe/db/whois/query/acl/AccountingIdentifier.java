package net.ripe.db.whois.query.acl;

import java.net.InetAddress;

public class AccountingIdentifier {

    final private InetAddress remoteAddress;
    final private UserInfo ssoUser;

    public AccountingIdentifier(InetAddress remoteAddress, UserInfo ssoUser) {
        this.remoteAddress = remoteAddress;
        this.ssoUser = ssoUser;
    }

    public UserInfo getSsoUser() {
        return ssoUser;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public record UserInfo(String userName, String uuid) {}
}
