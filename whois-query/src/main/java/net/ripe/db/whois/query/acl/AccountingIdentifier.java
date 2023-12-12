package net.ripe.db.whois.query.acl;

import java.net.InetAddress;

public class AccountingIdentifier {

    final private InetAddress remoteAddress;
    final private String ssoToken;

    public AccountingIdentifier(InetAddress remoteAddress, String ssoToken) {
        this.remoteAddress = remoteAddress;
        this.ssoToken = ssoToken;
    }

    public String getSsoToken() {
        return ssoToken;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }
}
