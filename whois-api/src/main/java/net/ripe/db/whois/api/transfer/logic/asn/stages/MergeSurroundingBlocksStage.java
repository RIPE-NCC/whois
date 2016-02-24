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

public class MergeSurroundingBlocksStage extends AsnTransferStage {
    public MergeSurroundingBlocksStage(String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return MergeSurroundingBlocksStage.class.getSimpleName();
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {

        final List<ActionRequest> requests = Lists.newArrayList();
        final String blockTemplate;
        if (transfer.isIncome()) {
            blockTemplate = RIPE_AS_BLOCK_TEMPLATE;
        } else {
            blockTemplate = NON_RIPE_AS_BLOCK_TEMPLATE;
        }

        final AsBlockRange leftAsBlockRange;
        if (precedingAsBlock.isPresent()) {
            requests.add(new ActionRequest(precedingAsBlock.get(), Action.DELETE));
            leftAsBlockRange = AsBlockRange.parse(precedingAsBlock.get().getKey().toString());
        } else {
            leftAsBlockRange = originalAsBlockRange;
        }

        final AsBlockRange rightAsBlockRange;
        if (followingAsBlock.isPresent()) {
            requests.add(new ActionRequest(followingAsBlock.get(), Action.DELETE));
            rightAsBlockRange = AsBlockRange.parse(followingAsBlock.get().getKey().toString());
        } else {
            rightAsBlockRange = originalAsBlockRange;
        }

        final RpslObject extendAsBlock = createAsBlock(leftAsBlockRange.getBegin(), rightAsBlockRange.getEnd(), blockTemplate);
        requests.add(new ActionRequest(extendAsBlock, Action.CREATE));

        return requests;
    }

    @Override
    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return originalAsBlockRange.getBegin() == originalAsBlockRange.getEnd();
    }
}
