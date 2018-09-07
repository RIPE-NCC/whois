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

public class ExtendPrecedingBlockStage extends AsnTransferStage {

    public ExtendPrecedingBlockStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return ExtendPrecedingBlockStage.class.getSimpleName();
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        final String blockTemplate;
        if (transfer.isIncoming()) {
            blockTemplate = RIPE_AS_BLOCK_TEMPLATE;
        } else {
            blockTemplate = NON_RIPE_AS_BLOCK_TEMPLATE;
        }

        if (precedingAsBlock.isPresent()) {
            final AsBlockRange precedingAsBlockRange = AsBlockRange.parse(precedingAsBlock.get().getKey().toString());
            final long begin = precedingAsBlockRange.getBegin();
            final long end = transfer.getResource().asBigInteger().longValue();

            final RpslObject extendAsBlock = createAsBlock(begin, end, blockTemplate);
            return Collections.singletonList(new ActionRequest(extendAsBlock, Action.CREATE));
        }

        return Collections.emptyList();
    }

    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return originalAsBlockRange.getBegin() != originalAsBlockRange.getEnd()
                && blockStartsWith(transfer.getResource(), originalAsBlockRange)
                && precedingAsBlock.isPresent();
    }
}
