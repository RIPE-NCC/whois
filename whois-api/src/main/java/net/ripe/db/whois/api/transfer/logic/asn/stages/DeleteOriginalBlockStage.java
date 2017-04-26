package net.ripe.db.whois.api.transfer.logic.asn.stages;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AsBlockRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DeleteOriginalBlockStage extends AsnTransferStage {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteOriginalBlockStage.class);

    public DeleteOriginalBlockStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return DeleteOriginalBlockStage.class.getSimpleName();
    }

    @Override
    public List<ActionRequest> doTransfer(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final RpslObject originalAsBlock, final Optional<RpslObject> followingAsBlock) {
        final List<ActionRequest> requests = Lists.newArrayList();

        requests.add(new ActionRequest(originalAsBlock, Action.DELETE));

        return doNextTransferStep(transfer, precedingAsBlock, originalAsBlock, followingAsBlock, requests);
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        // I don't care. The doTransfer is doing the job.
        return Lists.newArrayList();
    }

    @Override
    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return true;
    }

}
