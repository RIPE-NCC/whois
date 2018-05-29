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

public class CreateNewPrecedingBlockStage extends AsnTransferStage {

    public CreateNewPrecedingBlockStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return CreateNewPrecedingBlockStage.class.getSimpleName();
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        final String blockTemplate;
        if (transfer.isIncoming()) {
            blockTemplate = RIPE_AS_BLOCK_TEMPLATE;
        } else {
            blockTemplate = NON_RIPE_AS_BLOCK_TEMPLATE;
        }

        final long asn = transfer.getResource().asBigInteger().longValue();
        final RpslObject newAsBlock = createAsBlock(asn, asn, blockTemplate);

        return Collections.singletonList(new ActionRequest(newAsBlock, Action.CREATE));
    }

    @Override
    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return originalAsBlockRange.getBegin() != originalAsBlockRange.getEnd()
                && blockStartsWith(transfer.getResource(), originalAsBlockRange)
                && !precedingAsBlock.isPresent();
    }

}
