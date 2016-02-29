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

public class SplitBlockStage extends AsnTransferStage {
    public SplitBlockStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return SplitBlockStage.class.getSimpleName();
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {

        final List<ActionRequest> requests = Lists.newArrayList();
        RpslObject preceding;
        RpslObject middle;
        RpslObject following;

        final String middleBlockTemplate;
        final String edgeBlockTemplate;
        if (transfer.isIncome()) {
            middleBlockTemplate = RIPE_AS_BLOCK_TEMPLATE;
            edgeBlockTemplate = NON_RIPE_AS_BLOCK_TEMPLATE;
        } else {
            middleBlockTemplate = NON_RIPE_AS_BLOCK_TEMPLATE;
            edgeBlockTemplate = RIPE_AS_BLOCK_TEMPLATE;
        }

        final Asn precedingAsn = transfer.getResource().previous();
        preceding = createAsBlock(originalAsBlockRange.getBegin(), precedingAsn.asBigInteger().longValue(), edgeBlockTemplate);
        requests.add(new ActionRequest(preceding, Action.CREATE));

        middle = createAsBlock(transfer.getResource().asBigInteger().longValue(), transfer.getResource().asBigInteger().longValue(), middleBlockTemplate);
        requests.add(new ActionRequest(middle, Action.CREATE));

        final Asn followingAsn = transfer.getResource().next();
        following = createAsBlock(followingAsn.asBigInteger().longValue(), originalAsBlockRange.getEnd(), edgeBlockTemplate);
        requests.add(new ActionRequest(following, Action.CREATE));

        return requests;
    }

    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return originalAsBlockRange.getBegin() != originalAsBlockRange.getEnd()
                && !blockStartsWith(transfer.getResource(), originalAsBlockRange)
                && !blockEndsWith(transfer.getResource(), originalAsBlockRange);
    }
}
