package net.ripe.db.whois.api.transfer.logic.inetnum.stages;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;

public class DeleteOriginalInetnumStage extends InetnumTransferStage {

    public DeleteOriginalInetnumStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return DeleteOriginalInetnumStage.class.getSimpleName();
    }

    @Override
    public List<ActionRequest> doTransfer(final Transfer<Ipv4Range> transfer, final Optional<RpslObject> precedingObject, final RpslObject originalObject, final Optional<RpslObject> followingObject) {
        final List<ActionRequest> requests = Lists.newArrayList();

        requests.add(new ActionRequest(originalObject, Action.DELETE));

        return doNextTransferStep(transfer, precedingObject, originalObject, followingObject, requests);
    }


}
