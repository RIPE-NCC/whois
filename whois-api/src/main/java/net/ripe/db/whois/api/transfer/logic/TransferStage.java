package net.ripe.db.whois.api.transfer.logic;

import com.google.common.base.Optional;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;

public abstract class TransferStage<T> {

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
        if (nextStep != null) {
            requests.addAll(nextStep.doTransfer(transfer, precedingObject, originalObject, followingObject));
        }

        return requests;
    }

    public List<ActionRequest> doTransfer(final Transfer<T> transfer) {
        return doTransfer(transfer, Optional.absent(), null, Optional.absent());
    }

    public List<ActionRequest> doTransfer(final Transfer<T> transfer, final RpslObject originalObject) {
        return doTransfer(transfer, Optional.absent(), originalObject, Optional.absent());
    }

    protected abstract String getName();

    public abstract List<ActionRequest> doTransfer(final Transfer<T> transfer, final Optional<RpslObject> precedingObject, final RpslObject originalObject, final Optional<RpslObject> followingObject);

}
