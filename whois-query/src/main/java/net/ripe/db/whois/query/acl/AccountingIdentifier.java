package net.ripe.db.whois.query.acl;

import java.net.InetAddress;

public record AccountingIdentifier(InetAddress remoteAddress, String userName, String ssoToken) {
}
