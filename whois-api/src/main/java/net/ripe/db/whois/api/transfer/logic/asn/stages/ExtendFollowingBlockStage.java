package net.ripe.db.whois.api.transfer.logic.asn.stages;

import com.google.common.base.Optional;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;

import java.util.Collections;
import java.util.List;

public class ExtendFollowingBlockStage extends AsnTransferStage {

    public ExtendFollowingBlockStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return ExtendFollowingBlockStage.class.getSimpleName();
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        final String blockTemplate;
        if (transfer.isIncoming()) {
            blockTemplate = AsnTransferStage.RIPE_AS_BLOCK_TEMPLATE;
        } else {
            blockTemplate = AsnTransferStage.NON_RIPE_AS_BLOCK_TEMPLATE;
        }

        if (followingAsBlock.isPresent()) {
            final long begin = transfer.getResource().asBigInteger().longValue();
            final AsBlockRange rightAsBlockRange = AsBlockRange.parse(followingAsBlock.get().getKey().toString());
            final long end = rightAsBlockRange.getEnd();

            final RpslObject extendAsBlock = createAsBlock(begin, end, blockTemplate);
            return Collections.singletonList(new ActionRequest(extendAsBlock, Action.CREATE));
        }

        return Collections.emptyList();
    }

    @Override
    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return originalAsBlockRange.getBegin() != originalAsBlockRange.getEnd()
                && blockEndsWith(transfer.getResource(), originalAsBlockRange)
                && followingAsBlock.isPresent();
    }
}
