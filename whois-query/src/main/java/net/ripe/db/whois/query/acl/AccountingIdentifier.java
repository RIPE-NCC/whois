package net.ripe.db.whois.query.acl;

import java.net.InetAddress;

public class AccountingIdentifier {

    final private InetAddress remoteAddress;
    final private String userName;
    private final String ssoToken;

    public AccountingIdentifier(InetAddress remoteAddress, String userName, String ssoToken) {
        this.remoteAddress = remoteAddress;
        this.userName = userName;
        this.ssoToken = ssoToken;
    }

    public String getUserName() {
        return userName;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getSsoToken() {
        return ssoToken;
    }
}
