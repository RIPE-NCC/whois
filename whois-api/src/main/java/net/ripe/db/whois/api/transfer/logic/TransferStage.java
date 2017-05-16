package net.ripe.db.whois.api.transfer.logic;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class TransferStage<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferStage.class);
    protected final String source;
    protected TransferStage nextStep;

    public TransferStage(final String source) {
        this.source = source;
    }

    public TransferStage next(final TransferStage nextStep) {
        if (this.nextStep == null) {
            this.nextStep = nextStep;
        } else {
            this.nextStep.next(nextStep);
        }

        return this;
    }

    protected List<ActionRequest> doNextTransferStep(final Transfer<T> transfer, final Optional<RpslObject> precedingObject, final RpslObject originalObject, final Optional<RpslObject> followingObject, final List<ActionRequest> requests) {

        final List<ActionRequest> stepResult;
        if (nextStep == null) {
            stepResult = Lists.newArrayList();
        } else {
            LOGGER.debug("Going to execute stage <{}>", nextStep.getName());
            stepResult = nextStep.doTransfer(transfer, precedingObject, originalObject, followingObject);
        }

        requests.addAll(stepResult);

        return requests;
    }

    public List<ActionRequest> doTransfer(final Transfer<T> transfer) {
        Optional<RpslObject> preceding = Optional.absent();
        Optional<RpslObject> following = Optional.absent();
        return doTransfer(transfer, preceding, null, following);
    }

    public List<ActionRequest> doTransfer(final Transfer<T> transfer, final RpslObject originalObject) {
        Optional<RpslObject> preceding = Optional.absent();
        Optional<RpslObject> following = Optional.absent();

        return doTransfer(transfer, preceding, originalObject, following);
    }

    protected abstract String getName();

    public abstract List<ActionRequest> doTransfer(final Transfer<T> transfer, final Optional<RpslObject> precedingObject, final RpslObject originalObject, final Optional<RpslObject> followingObject);

}
