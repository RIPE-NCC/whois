package net.ripe.db.whois.api.transfer.logic.inetnum;

import net.ripe.commons.ip.Ipv4Range;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;

public class InetnumTransfer extends Transfer<Ipv4Range> {

    public static final String NON_RIPE_NETNAME = "NON-RIPE-NCC-MANAGED-ADDRESS-BLOCK";
    public static final String IANA_NETNAME = "IETF-RESERVED-ADDRESS-BLOCK";

    private InetnumTransfer(final Ipv4Range ipv4Range, final boolean income) {
        super(ipv4Range, income);
    }

    public static Transfer buildOutgoing(final String inetnum) {
        return new InetnumTransfer(Ipv4Range.parse(inetnum), false);
    }

    public static Transfer buildIncoming(final String inetnum) {
        return new InetnumTransfer(Ipv4Range.parse(inetnum), true);
    }

    public static boolean isIanaResource(final RpslObject resource) {
        final RpslAttribute netname = resource.findAttribute(AttributeType.NETNAME);
        return (netname != null) && IANA_NETNAME.equals(netname.getValue().trim());
    }

    public static boolean isNonRipeResource(final RpslObject resource) {
        final RpslAttribute netname = resource.findAttribute(AttributeType.NETNAME);
        return (netname != null) && NON_RIPE_NETNAME.equals(netname.getValue().trim());
    }

    public String toString() {
        return String.format("Transfer %s of inetnum %s",
                (isIncoming() ? "in" : "out"),
                this.getResource());
    }

}
