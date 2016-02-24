package net.ripe.db.whois.api.transfer.logic.inetnum.stages;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;

import java.util.List;

public class CreatePlaceholderForWhatIsLeftStage extends InetnumTransferStage {
    public CreatePlaceholderForWhatIsLeftStage(String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return CreatePlaceholderForWhatIsLeftStage.class.getSimpleName();
    }

    @Override
    public List<ActionRequest> doTransfer(final Transfer<Ipv4Range> transfer, final Optional<RpslObject> precedingObject, final RpslObject originalObject, final Optional<RpslObject> followingObject) {
        final List<ActionRequest> requests = Lists.newArrayList();

        final Ipv4Range originalRange = Ipv4Range.parse(originalObject.getKey().toString());
        final List<Ipv4Range> whatIsLeft = originalRange.exclude(transfer.getResource());

        final List<RpslAttribute> originalObjectAttributes = originalObject.getAttributes();

        for (Ipv4Range range : whatIsLeft) {
            final RpslObjectBuilder whatIsLeftRpslObjectBuilder = new RpslObjectBuilder(Lists.newArrayList(originalObjectAttributes));

            for (RpslAttribute rpslAttribute : originalObjectAttributes) {
                if (rpslAttribute.getType() == AttributeType.INETNUM) {
                    final RpslAttribute newRange = new RpslAttribute(AttributeType.INETNUM, formatInetnum(range));
                    whatIsLeftRpslObjectBuilder.replaceAttribute(rpslAttribute, newRange);
                }
            }

            requests.add(new ActionRequest(whatIsLeftRpslObjectBuilder.get(), Action.CREATE));
        }

        return doNextTransferStep(transfer, precedingObject, originalObject, followingObject, requests);
    }

    private String formatInetnum(final Ipv4Range range) {
        return String.format("%s - %s", range.start().toString(), range.end().toString());
    }
}
