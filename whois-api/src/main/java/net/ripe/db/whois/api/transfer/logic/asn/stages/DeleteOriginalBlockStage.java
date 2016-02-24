package net.ripe.db.whois.api.transfer.logic.asn.stages;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.rpsl.AttributeType;
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

        LOGGER.debug("Execute stage '{}' for {} with prev: {}, current: {} and next: {}",
                getName(),
                transfer,
                precedingAsBlock.isPresent() ? precedingAsBlock.get().getKey() : "n/a",
                originalAsBlock.getKey(),
                followingAsBlock.isPresent() ? followingAsBlock.get().getKey() : "n/a");

        ActionRequest ar = new ActionRequest(originalAsBlock, Action.DELETE);

        LOGGER.debug("Stage '{}' resulting action: {} on object: {} with descr: {}",
                getName(),
                ar.getAction(),
                ar.getRpslObject().getKey(),
                ar.getRpslObject().getValueOrNullForAttribute(AttributeType.DESCR));

        requests.add(ar);

        return doNextTransferStep(transfer, precedingAsBlock, originalAsBlock, followingAsBlock, requests);
    }

    @Override
    protected List<ActionRequest> createRequests(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return Lists.newArrayList(); //I don't care. The doTransfer is doing the job.
    }

    @Override
    protected boolean shouldExecute(final Transfer<Asn> transfer, final Optional<RpslObject> precedingAsBlock, final AsBlockRange originalAsBlockRange, final Optional<RpslObject> followingAsBlock) {
        return true;
    }

}
