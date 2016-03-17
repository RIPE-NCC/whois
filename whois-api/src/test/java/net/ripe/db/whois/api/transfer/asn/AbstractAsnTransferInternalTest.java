package net.ripe.db.whois.api.transfer.asn;

import net.ripe.db.whois.api.transfer.AbstractTransferTest;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.api.transfer.logic.asn.AsnTransfer;


public abstract class AbstractAsnTransferInternalTest extends AbstractTransferTest {

    protected boolean isRipeAsBlock(final String primaryKey) {
        return isRipeAsBlock(lookup(ObjectType.AS_BLOCK, primaryKey));
    }

    protected boolean isNonRipeAsBlock(final String primaryKey) {
        return isNonRipeAsBlock(lookup(ObjectType.AS_BLOCK, primaryKey));
    }

    protected boolean isIanaAsBlock(final String primaryKey) {
        return AsnTransfer.isIanaBlock(lookup(ObjectType.AS_BLOCK, primaryKey));
    }

    protected boolean isRipeAsBlock(final RpslObject rpslObject) {
        return rpslObject.getValueForAttribute(AttributeType.DESCR).equals(AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR);
    }

    protected boolean isNonRipeAsBlock(final RpslObject rpslObject) {
        return rpslObject.getValueForAttribute(AttributeType.DESCR).equals(AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR);
    }

    protected boolean isMaintainedInRirSpace(final String source, final String resource) {
        return internalsTemplate.queryForObject("SELECT count(*) FROM authoritative_resource WHERE source = ? AND resource = ?", Integer.class, source, resource) > 0;
    }

    protected boolean objectExists(String id) {
        return objectExists(ObjectType.AS_BLOCK, id);
    }

}
