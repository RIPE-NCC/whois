package net.ripe.db.whois.api.transfer.logic.asn;

import com.google.common.base.Optional;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;

public class AsnTransfer extends Transfer<Asn> {

    public static final String NON_RIPE_NCC_ASN_BLOCK_DESCR = "ASN block not managed by the RIPE NCC";
    public static final String RIPE_NCC_ASN_BLOCK_DESCR = "RIPE NCC ASN block";
    public static final String IANA_ASN_BLOCK_DESCR = "IANA reserved ASN block";

    private AsnTransfer(final Asn asn, final boolean income) {
        super(asn, income);
    }

    public static Transfer buildOutgoing(final String autNum) {
        return new AsnTransfer(Asn.of(autNum), false);
    }

    public static Transfer buildIncoming(final String autNum) {
        return new AsnTransfer(Asn.of(autNum), true);
    }

    public static boolean isNonRipeBlock(final RpslObject rpslObject) {
        if (rpslObject == null) {
            return false;
        }

        return rpslObject.getValueForAttribute(AttributeType.DESCR).contains(NON_RIPE_NCC_ASN_BLOCK_DESCR);
    }

    public static boolean isRipeBlock(final RpslObject rpslObject) {
        if (rpslObject == null) {
            return false;
        }

        return rpslObject.getValueForAttribute(AttributeType.DESCR).contains(RIPE_NCC_ASN_BLOCK_DESCR);
    }

    public static boolean belongToSameRegion(final RpslObject target, final Optional<RpslObject> other) {
        if (!other.isPresent()) {
            return false;
        }
        return target.getValueForAttribute(AttributeType.DESCR).equals(
                other.get().getValueForAttribute(AttributeType.DESCR));
    }

    public static boolean isIanaBlock(final RpslObject rpslObject) {
        if (rpslObject == null) {
            return false;
        }

        return rpslObject.getValueForAttribute(AttributeType.DESCR).contains(IANA_ASN_BLOCK_DESCR);
    }

    public static boolean isAsBlock(final RpslObject rpslObject) {
        return ObjectType.AS_BLOCK.equals(rpslObject.getType());
    }

    public static boolean isAsBlockInPrimarySource(final RpslObject rpslObject) {
        return rpslObject.containsAttribute(AttributeType.DESCR) &&
                CIString.ciString(RIPE_NCC_ASN_BLOCK_DESCR).equals(rpslObject.getValueForAttribute(AttributeType.DESCR));
    }

    public String toString() {
        return String.format("transfer %s of aut-num %s",
                (isIncome() ? "in" : "out"),
                this.getResource());
    }
}
