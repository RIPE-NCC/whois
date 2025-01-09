package net.ripe.db.whois.query.acl;

import java.net.InetAddress;

public class AccountingIdentifier {

    final private InetAddress remoteAddress;
    final private String userName;

    public AccountingIdentifier(InetAddress remoteAddress, String userName) {
        this.remoteAddress = remoteAddress;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public InetAddress getRemoteAddress() {
        return remoteAddress;
    }
}
