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

    public static final String TEMPLATE = "" +
            "inetnum:         %s\n" +
            "netname:         " + InetnumTransfer.NON_RIPE_NETNAME + "\n" +
            "descr:           IPv4 address block not managed by the RIPE NCC\n" +
            "remarks:         ------------------------------------------------------\n" +
            "remarks:         \n" +
            "remarks:         You can find the whois server to query, or the\n" +
            "remarks:         IANA registry to query on this web page:\n" +
            "remarks:         http://www.iana.org/assignments/ipv4-address-space\n" +
            "remarks:         \n" +
            "remarks:         You can access databases of other RIR's at:\n" +
            "remarks:         \n" +
            "remarks:         AFRINIC (Africa)\n" +
            "remarks:         http://www.afrinic.net/ whois.afrinic.net\n" +
            "remarks:         \n" +
            "remarks:         APNIC (Asia Pacific)\n" +
            "remarks:         http://www.apnic.net/ whois.apnic.net\n" +
            "remarks:         \n" +
            "remarks:         ARIN (Northern America)\n" +
            "remarks:         http://www.arin.net/ whois.arin.net\n" +
            "remarks:         \n" +
            "remarks:         LACNIC (Latin America and the Carribean)\n" +
            "remarks:         http://www.lacnic.net/ whois.lacnic.net\n" +
            "remarks:         \n" +
            "remarks:         ------------------------------------------------------\n" +
            "country:         EU # Country is really world wide\n" +
            "org:             ORG-IANA1-RIPE\n" +
            "admin-c:         IANA1-RIPE\n" +
            "tech-c:          IANA1-RIPE\n" +
            "status:          ALLOCATED UNSPECIFIED\n" +
            "mnt-by:          RIPE-NCC-HM-MNT\n" +
            "mnt-lower:       RIPE-NCC-HM-MNT\n" +
            "mnt-routes:      RIPE-NCC-RPSL-MNT\n" +
            "source:          %s";

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

    private ActionRequest createObject(Transfer<Ipv4Range> transfer) {
        Preconditions.checkArgument(transfer != null);
        final RpslObject rpslObject = RpslObject.parse(String.format(TEMPLATE, transfer.getResource().toStringInRangeNotation(), source));
        return new ActionRequest(rpslObject, Action.CREATE);
    }

    private boolean shouldMergeWithObject(Optional<RpslObject> rpslObject, Ipv4Range resource) {

        if (rpslObject.isPresent()) {
            final Ipv4Range rangeObject = Ipv4Range.parse(rpslObject.get().getKey().toString());
            if (rangeObject.isConsecutive(resource)) {
                final Ipv4Range mergedRange = resource.merge(rangeObject);

                final Ipv4 startLowerBoundForPrefix = mergedRange.start().lowerBoundForPrefix(8);
                final Ipv4 endLowerBoundForPrefix = mergedRange.end().lowerBoundForPrefix(8);

                return startLowerBoundForPrefix.equals(endLowerBoundForPrefix);
            }
        }

        return false;
    }

    private List<ActionRequest> merge(RpslObject precedingObject, RpslObject followingObject, Ipv4Range resource) {
        final List<ActionRequest> requests = Lists.newArrayList();

        Preconditions.checkArgument(precedingObject != null);
        Preconditions.checkArgument(followingObject != null);
        Preconditions.checkArgument(resource != null);

        final Ipv4Range precedingRange = Ipv4Range.parse(precedingObject.getKey().toString());
        final Ipv4Range followingRange = Ipv4Range.parse(followingObject.getKey().toString());

        final Ipv4Range mergedRange = precedingRange.merge(resource).merge(followingRange);

        final RpslObject rpslObject = RpslObject.parse(String.format(TEMPLATE, mergedRange.toStringInRangeNotation(), source));

        requests.add(new ActionRequest(precedingObject, Action.DELETE));
        requests.add(new ActionRequest(followingObject, Action.DELETE));
        requests.add(new ActionRequest(rpslObject, Action.CREATE));

        return requests;
    }

    private List<ActionRequest> merge(RpslObject leftOrRightNeighbour, Ipv4Range resource) {
        final List<ActionRequest> requests = Lists.newArrayList();

        Preconditions.checkArgument(leftOrRightNeighbour != null);
        Preconditions.checkArgument(resource != null);

        final RpslObject neighbour = leftOrRightNeighbour;

        final Ipv4Range originalRange = Ipv4Range.parse(neighbour.getKey().toString());
        final Ipv4Range mergedRange = originalRange.merge(resource);

        final RpslObject rpslObject = RpslObject.parse(String.format(TEMPLATE, mergedRange.toStringInRangeNotation(), source));
        requests.add(new ActionRequest(neighbour, Action.DELETE));
        requests.add(new ActionRequest(rpslObject, Action.CREATE));

        return requests;
    }
}
