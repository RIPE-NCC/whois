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

public class ShrinkBlockStage extends AsnTransferStage {

    public ShrinkBlockStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return ShrinkBlockStage.class.getSimpleName();
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        final List<ActionRequest> requests = Lists.newArrayList();

        final String blockTemplate;
        if (transfer.isIncome()) {
            blockTemplate = NON_RIPE_AS_BLOCK_TEMPLATE;
        } else {
            blockTemplate = RIPE_AS_BLOCK_TEMPLATE;
        }

        RpslObject shrunkBlock;
        if (blockEndsWith(transfer.getResource(), originalAsBlockRange)) {
            final long begin = originalAsBlockRange.getBegin();
            final long end = transfer.getResource().previous().asBigInteger().longValue();

            shrunkBlock = createAsBlock(begin, end, blockTemplate);
            requests.add(new ActionRequest(shrunkBlock, Action.CREATE));
        }

        if (blockStartsWith(transfer.getResource(), originalAsBlockRange)) {
            final long begin = transfer.getResource().next().asBigInteger().longValue();
            final long end = originalAsBlockRange.getEnd();

            shrunkBlock = createAsBlock(begin, end, blockTemplate);
            requests.add(new ActionRequest(shrunkBlock, Action.CREATE));
        }

        return requests;
    }

    @Override
    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return originalAsBlockRange.getBegin() != originalAsBlockRange.getEnd();
    }

}
