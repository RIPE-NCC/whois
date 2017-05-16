package net.ripe.db.whois.api.transfer.logic.asn.stages;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;

import java.util.List;

public class DeleteDestinationBlockStage extends AsnTransferStage {

    public DeleteDestinationBlockStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return DeleteDestinationBlockStage.class.getSimpleName();
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        final List<ActionRequest> requests = Lists.newArrayList();

        final Optional<RpslObject> destinationAsBlock;

        if (blockEndsWith(transfer.getResource(), originalAsBlockRange)) {
            destinationAsBlock = followingAsBlock;
        } else {
            destinationAsBlock = precedingAsBlock;
        }

        if (destinationAsBlock.isPresent()) {
            final AsBlockRange asBlockRange = AsBlockRange.parse(destinationAsBlock.get().getKey().toString());

            final Asn resource = transfer.getResource();
            if (blockStartsWith(resource.next(), asBlockRange) || blockEndsWith(resource.previous(), asBlockRange)) {
                requests.add(new ActionRequest(destinationAsBlock.get(), Action.DELETE));
            }
        }

        return requests;
    }

    @Override
    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return originalAsBlockRange.getBegin() != originalAsBlockRange.getEnd();
    }
}
