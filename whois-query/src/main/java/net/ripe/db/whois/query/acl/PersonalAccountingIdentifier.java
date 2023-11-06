package net.ripe.db.whois.query.acl;

import java.net.InetAddress;
import java.util.Objects;

public class PersonalAccountingIdentifier {

    public enum ClientIdType { IP, SSO };
    final String accountingId;
    final ClientIdType type;

    private PersonalAccountingIdentifier(final ClientIdType type, final String accountingId) {
        this.type = type;
        this.accountingId = accountingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonalAccountingIdentifier)) return false;

        final PersonalAccountingIdentifier that = (PersonalAccountingIdentifier) o;
        return accountingId.equals(that.accountingId) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountingId, type);
    }

    public static PersonalAccountingIdentifier getInstance(final InetAddress remoteAddress, final String ssoId) {
        return ssoId != null ? new PersonalAccountingIdentifier(ClientIdType.SSO, ssoId) : new PersonalAccountingIdentifier(ClientIdType.IP, remoteAddress.toString());
    }
}
