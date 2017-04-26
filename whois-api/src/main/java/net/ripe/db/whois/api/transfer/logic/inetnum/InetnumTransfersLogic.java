package net.ripe.db.whois.api.transfer.logic.inetnum;


import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.db.whois.api.QueryBuilder;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.api.transfer.logic.TransferStage;
import net.ripe.db.whois.api.transfer.logic.inetnum.stages.CreatePlaceholderForInetnumStage;
import net.ripe.db.whois.api.transfer.logic.inetnum.stages.CreatePlaceholderForWhatIsLeftStage;
import net.ripe.db.whois.api.transfer.logic.inetnum.stages.DeleteOriginalInetnumStage;
import net.ripe.db.whois.common.domain.ResponseObject;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.domain.ResponseHandler;
import net.ripe.db.whois.query.executor.SearchQueryExecutor;
import net.ripe.db.whois.query.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class InetnumTransfersLogic {
    private static final Logger LOGGER = LoggerFactory.getLogger(InetnumTransfersLogic.class);

    private final String source;
    private final SearchQueryExecutor searchQueryExecutor;
    private final TransferStage transferInPipeline;
    private final TransferStage transferOutPipeline;

    @Autowired
    public InetnumTransfersLogic(final @Value("${whois.source}") String source,
                                 final SearchQueryExecutor searchQueryExecutor) {
        this.source = source;
        this.searchQueryExecutor = searchQueryExecutor;
        this.transferInPipeline = new DeleteOriginalInetnumStage(this.source)
                .next(new CreatePlaceholderForWhatIsLeftStage(this.source));
        this.transferOutPipeline = new CreatePlaceholderForInetnumStage(this.source);
    }

    public List<ActionRequest> getTransferOutActions(final String inetnum) {
        return planOutgoingTransfer(inetnum);
    }

    private List<ActionRequest> planOutgoingTransfer(final String inetnum) {
        final List<ActionRequest> requests = Lists.newArrayList();

        final Collection<RpslObject> searchResults = searchInetnumObject(inetnum, QueryFlag.ALL_LESS);
        validateSearchResults(inetnum, searchResults);
        final RpslObject matchingObject = Iterables.getLast(searchResults);

        if (InetnumTransfer.isNonRipeResource(matchingObject)) {
            // no transfer tasks to be performed
            LOGGER.warn("Inetnum {} is already non-RIPE", inetnum);
        } else {
            final Ipv4Range range = Ipv4Range.parse(inetnum);
            if (isExactMatch(range, matchingObject)) {
                throw new BadRequestException(inetnum + " is an exact match and cannot be transferred out");
            }

            final Optional<RpslObject> preceding = getPrecedingRpslObject(range);
            final Optional<RpslObject> following = getFollowingRpslObject(range);

            final Transfer transfer = InetnumTransfer.buildOutgoing(inetnum);
            requests.addAll(transferOutPipeline.doTransfer(transfer, preceding, matchingObject, following));
        }

        logSteps(requests);

        return requests;
    }

    private boolean isExactMatch(final Ipv4Range inetnumRange, final RpslObject matchingObject) {
        return Ipv4Range.parse(matchingObject.getKey().toString()).isSameRange(inetnumRange);
    }

    private Optional<RpslObject> getPrecedingRpslObject(final Ipv4Range range) {
        if (!range.start().hasPrevious()) {
            return Optional.absent();
        }

        final Ipv4 previous = range.start().previous();
        final Collection<RpslObject> parents = searchInetnumObject(previous.toString(), QueryFlag.ONE_LESS);
        return getNeighbour(parents);
    }

    private Optional<RpslObject> getFollowingRpslObject(final Ipv4Range range) {
        if (!range.end().hasNext()) {
            return Optional.absent();
        }

        final Ipv4 next = range.end().next();
        final Collection<RpslObject> parents = searchInetnumObject(next.toString(), QueryFlag.ONE_LESS);
        return getNeighbour(parents);
    }

    private Optional<RpslObject> getNeighbour(final Collection<RpslObject> parents) {
        if (parents.isEmpty()) {
            return Optional.absent();
        }

        final RpslObject reference = Iterables.getLast(parents);
        if (InetnumTransfer.isNonRipeResource(reference)) {
            return Optional.of(reference);
        } else {
            return Optional.absent();
        }
    }

    public List<ActionRequest> getTransferInActions(final String inetnum) {
        return planIncomingTransfer(inetnum);
    }

    private List<ActionRequest> planIncomingTransfer(final String inetnum) {
        final List<ActionRequest> requests = Lists.newArrayList();

        final Collection<RpslObject> searchResults = searchInetnumObject(inetnum, QueryFlag.ALL_LESS);
        validateSearchResults(inetnum, searchResults);

        final Transfer transfer = InetnumTransfer.buildIncoming(inetnum);
        Collections.reverse((List) searchResults);
        for (RpslObject rpslObject : searchResults) {
            if (InetnumTransfer.isNonRipeResource(rpslObject)) {
                requests.addAll(transferInPipeline.doTransfer(transfer, rpslObject));
                break;
            }
        }

        logSteps(requests);

        return requests;
    }

    private void logSteps(final List<ActionRequest> requests) {
        LOGGER.info("{} requests", requests.size());
        for (ActionRequest req : requests) {
            LOGGER.info("{} {}", req.getAction(), req.getRpslObject().getFormattedKey());
        }
    }

    private Collection<RpslObject> searchInetnumObject(final String inetnum, final QueryFlag level) {
        final String queryString = new QueryBuilder()
                .addCommaList(QueryFlag.SOURCES, source)
                .addCommaList(QueryFlag.SELECT_TYPES, ObjectType.INETNUM.getName())
                .addFlag(level)
                .addFlag(QueryFlag.NO_FILTERING)
                .addFlag(QueryFlag.NO_IRT)
                .addFlag(QueryFlag.REVERSE_DOMAIN)
                .addFlag(QueryFlag.NO_REFERENCED).build(inetnum);

        final Query query = Query.parse(queryString, Query.Origin.REST, true);

        final List<RpslObject> inetnums = Lists.newArrayList();
        searchQueryExecutor.execute(query, new ResponseHandler() {
            @Override
            public String getApi() {
                return "API";
            }

            @Override
            public void handle(final ResponseObject responseObject) {
                if( responseObject instanceof RpslObject ) {
                    inetnums.add((RpslObject) responseObject);
                }
            }
        });

        return inetnums;
    }

    private void validateSearchResults(final String inetnum, final Collection<RpslObject> searchResults) {
        if (searchResults.isEmpty() || searchResults.size() == 1) {
            // only /0 is returned: so requested object does not exist
            LOGGER.info("{} not found", inetnum);
            throw new NotFoundException("Inetnum " + inetnum + " not found.");
        }

        //  matching object is last
        final RpslObject matchingObject = Iterables.getLast(searchResults);
        logObject("matchingObject", matchingObject);

        if (InetnumTransfer.isIanaResource(matchingObject)) {
            LOGGER.info("{} is owned by IANA", inetnum);
            throw new BadRequestException("Inetnum " + inetnum + " is owned by IANA.");
        }
    }

    private void logObject(final String msg, final RpslObject obj) {
        final RpslAttribute netname = obj.findAttribute(AttributeType.NETNAME);
        LOGGER.debug("{}: {} {} with {}",
                msg,
                obj.getType().getName(),
                obj.getKey(),
                netname != null ? netname.getKey() : "n/a");
    }

}
