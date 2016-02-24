package net.ripe.db.whois.api.transfer.logic.asn.stages;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.api.transfer.logic.TransferStage;
import net.ripe.db.whois.api.transfer.logic.asn.AsnTransfer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public abstract class AsnTransferStage extends TransferStage<Asn> {
    protected static final String NON_RIPE_AS_BLOCK_TEMPLATE =
            "as-block:        %s\n" +
            "descr:           " + AsnTransfer.NON_RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
            "remarks:         ------------------------------------------------------\n" +
            "remarks:         \n" +
            "remarks:         aut-num objects within this block represent routing\n" +
            "remarks:         policy published in the RIPE Database\n" +
            "remarks:         \n" +
            "remarks:         For registration information,\n" +
            "remarks:         you can find the whois server to query, or the\n" +
            "remarks:         IANA registry to query on this web page:\n" +
            "remarks:         http://www.iana.org/assignments/ipv4-address-space\n" +
            "remarks:         \n" +
            "remarks:         You can access databases of other RIR's at:\n" +
            "remarks:         \n" +
            "remarks:         AFRINIC (Africa)\n" +
            "remarks:         http://www.afrinic.net/ whois.afrinic.net\n" +
            "remarks:         \n" +
            "remarks:         APNIC (Asia Pacific)\n" +
            "remarks:         http://www.apnic.net/ whois.apnic.net\n" +
            "remarks:         \n" +
            "remarks:         ARIN (Northern America)\n" +
            "remarks:         http://www.arin.net/ whois.arin.net\n" +
            "remarks:         \n" +
            "remarks:         LACNIC (Latin America and the Carribean)\n" +
            "remarks:         http://www.lacnic.net/ whois.lacnic.net\n" +
            "remarks:         \n" +
            "remarks:         ------------------------------------------------------\n" +
            "mnt-by:          RIPE-DBM-MNT\n" +
            "mnt-lower:       RIPE-NCC-RPSL-MNT\n" +
            "source:          %s";
    protected static final String RIPE_AS_BLOCK_TEMPLATE =
            "as-block:        %s\n" +
            "descr:           " + AsnTransfer.RIPE_NCC_ASN_BLOCK_DESCR + "\n" +
            "remarks:         These AS Numbers are assigned to network operators in the RIPE NCC service region.\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "source:          %s";
    private static final Logger LOGGER = LoggerFactory.getLogger(AsnTransferStage.class);

    public AsnTransferStage(String source) {
        super(source);
    }

    protected boolean blockEndsWith(final Asn asn, final AsBlockRange asBlockRange) {
        return asBlockRange.getEnd() == asn.asBigInteger().longValue();
    }

    protected boolean blockStartsWith(final Asn asn, final AsBlockRange asBlockRange) {
        return asBlockRange.getBegin() == asn.asBigInteger().longValue();
    }

    protected RpslObject createAsBlock(final long begin, final long end, final String template) {
        final String range = String.format("AS%s - AS%s", begin, end);
        return RpslObject.parse(String.format(template, range, source));
    }

    public List<ActionRequest> doTransfer(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final RpslObject originalAsBlock, final Optional<RpslObject> followingAsBlock) {
        final List<ActionRequest> requests = Lists.newArrayList();
        final AsBlockRange originalAsBlockRange = AsBlockRange.parse(originalAsBlock.getKey().toString());

        LOGGER.debug("Execute stage '{}' for {} with prev:{}, current: {} and next: {}",
                getName(),
                transfer,
                precedingAsBlock.isPresent() ? precedingAsBlock.get().getKey() : "n/a",
                originalAsBlock.getKey(),
                followingAsBlock.isPresent() ? followingAsBlock.get().getKey() : "n/a");

        if (shouldExecute(transfer, precedingAsBlock, originalAsBlockRange, followingAsBlock)) {
            List<ActionRequest> stageRequests = createRequests(transfer, precedingAsBlock, originalAsBlockRange, followingAsBlock);
            for (ActionRequest ar : stageRequests) {
                LOGGER.debug("Stage '{}' resulting action: {} on object: {} with descr: {}",
                        getName(),
                        ar.getAction(),
                        ar.getRpslObject().getKey(),
                        ar.getRpslObject().getValueOrNullForAttribute(AttributeType.DESCR));
            }
            requests.addAll(stageRequests);
        } else {
            LOGGER.debug("No work to be done for stage '{}'", getName());
        }

        return doNextTransferStep(transfer, precedingAsBlock, originalAsBlock, followingAsBlock, requests);
    }

    protected abstract List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock);

    protected abstract boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock);

}
