package net.ripe.db.whois.api.transfer.logic.inetnum.stages;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.api.transfer.logic.inetnum.InetnumTransfer;
import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.List;

public class CreatePlaceholderForInetnumStage extends InetnumTransferStage {

    private static final String TEMPLATE = "" +
            "inetnum:         %s\n" +
            "netname:         " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:          IPv4 address block not managed by the RIPE NCC\n" +
            "remarks:        ------------------------------------------------------\n" +
            "remarks:\n" +
            "remarks:        For registration information,\n" +
            "remarks:        you can consult the following sources:\n" +
            "remarks:\n" +
            "remarks:        IANA\n" +
            "remarks:        http://www.iana.org/assignments/ipv4-address-space\n" +
            "remarks:        http://www.iana.org/assignments/iana-ipv4-special-registry\n" +
            "remarks:        http://www.iana.org/assignments/ipv4-recovered-address-space\n" +
            "remarks:\n" +
            "remarks:        AFRINIC (Africa)\n" +
            "remarks:        http://www.afrinic.net/ whois.afrinic.net\n" +
            "remarks:\n" +
            "remarks:        APNIC (Asia Pacific)\n" +
            "remarks:        http://www.apnic.net/ whois.apnic.net\n" +
            "remarks:\n" +
            "remarks:        ARIN (Northern America)\n" +
            "remarks:        http://www.arin.net/ whois.arin.net\n" +
            "remarks:\n" +
            "remarks:        LACNIC (Latin America and the Carribean)\n" +
            "remarks:        http://www.lacnic.net/ whois.lacnic.net\n" +
            "remarks:\n" +
            "remarks:        ------------------------------------------------------\n" +
            "country:        EU # Country is really world wide\n" +
            "admin-c:        IANA1-RIPE\n" +
            "tech-c:         IANA1-RIPE\n" +
            "status:         ALLOCATED UNSPECIFIED\n" +
            "mnt-by:         RIPE-NCC-HM-MNT\n" +
            "source:         %s";

    public CreatePlaceholderForInetnumStage(final String source) {
        super(source);
    }

    @Override
    protected String getName() {
        return CreatePlaceholderForInetnumStage.class.getSimpleName();
    }

    @Override
    public List<ActionRequest> doTransfer(final Transfer<Ipv4Range> transfer, final Optional<RpslObject> precedingObject, final RpslObject originalObject, final Optional<RpslObject> followingObject) {
        final List<ActionRequest> requests = Lists.newArrayList();

        final Ipv4Range resource = transfer.getResource();

        if (shouldMergeWithObject(precedingObject, resource) && shouldMergeWithObject(followingObject, resource)) {
            requests.addAll(merge(precedingObject.get(), followingObject.get(), resource));
        } else if (shouldMergeWithObject(precedingObject, resource)) {
            requests.addAll(merge(precedingObject.get(), resource));
        } else if (shouldMergeWithObject(followingObject, resource)) {
            requests.addAll(merge(followingObject.get(), resource));
        } else {
            requests.add(createObject(transfer));
        }

        return doNextTransferStep(transfer, precedingObject, originalObject, followingObject, requests);
    }

    private ActionRequest createObject(final Transfer<Ipv4Range> transfer) {
        Preconditions.checkArgument(transfer != null);

        final RpslObject inetnum = RpslObject.parse(String.format(TEMPLATE, transfer.getResource().toStringInRangeNotation(), source));
        return new ActionRequest(inetnum, Action.CREATE);
    }

    private boolean shouldMergeWithObject(final Optional<RpslObject> rpslObject, final Ipv4Range resource) {
        if (!rpslObject.isPresent()) {
            return false;
        }

        final Ipv4Range rangeObject = Ipv4Range.parse(rpslObject.get().getKey().toString());
        if (!rangeObject.isConsecutive(resource)) {
            return false;
        }

        final Ipv4Range mergedRange = resource.merge(rangeObject);

        final Ipv4 startLowerBoundForPrefix = mergedRange.start().lowerBoundForPrefix(8);
        final Ipv4 endLowerBoundForPrefix = mergedRange.end().lowerBoundForPrefix(8);

        return startLowerBoundForPrefix.equals(endLowerBoundForPrefix);
    }

    private List<ActionRequest> merge(final RpslObject precedingObject, final RpslObject followingObject, final Ipv4Range resource) {
        final List<ActionRequest> requests = Lists.newArrayList();

        Preconditions.checkArgument(precedingObject != null);
        Preconditions.checkArgument(followingObject != null);
        Preconditions.checkArgument(resource != null);

        final Ipv4Range precedingRange = Ipv4Range.parse(precedingObject.getKey().toString());
        final Ipv4Range followingRange = Ipv4Range.parse(followingObject.getKey().toString());

        final Ipv4Range mergedRange = precedingRange.merge(resource).merge(followingRange);

        final RpslObject inetnum = RpslObject.parse(String.format(TEMPLATE, mergedRange.toStringInRangeNotation(), source));

        requests.add(new ActionRequest(precedingObject, Action.DELETE));
        requests.add(new ActionRequest(followingObject, Action.DELETE));
        requests.add(new ActionRequest(inetnum, Action.CREATE));

        return requests;
    }

    private List<ActionRequest> merge(final RpslObject leftOrRightNeighbour, final Ipv4Range resource) {
        final List<ActionRequest> requests = Lists.newArrayList();

        Preconditions.checkArgument(leftOrRightNeighbour != null);
        Preconditions.checkArgument(resource != null);

        final Ipv4Range originalRange = Ipv4Range.parse(leftOrRightNeighbour.getKey().toString());
        final Ipv4Range mergedRange = originalRange.merge(resource);

        final RpslObject inetnum = RpslObject.parse(String.format(TEMPLATE, mergedRange.toStringInRangeNotation(), source));
        requests.add(new ActionRequest(leftOrRightNeighbour, Action.DELETE));
        requests.add(new ActionRequest(inetnum, Action.CREATE));

        return requests;
    }
}
