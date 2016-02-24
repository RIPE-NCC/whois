package net.ripe.db.whois.api.transfer.logic.asn;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import net.ripe.commons.ip.Asn;
import net.ripe.db.whois.api.rest.client.RestClientException;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.transfer.logic.AuthoritativeResourceService;
import net.ripe.db.whois.api.transfer.logic.Transfer;
import net.ripe.db.whois.api.transfer.logic.TransferStage;
import net.ripe.db.whois.api.transfer.logic.asn.stages.*;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.RpslObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import java.util.Collections;
import java.util.List;

@Component
public class AsnTransferLogic {

    public static final String ASN_TRANSFER_SERVICE = "asnTransferService";
    private static final Logger LOGGER = LoggerFactory.getLogger(AsnTransferLogic.class);
    private final RpslObjectDao rpslObjectDao;
    private final AuthoritativeResourceService authoritativeResourceService;
    private final String source;

    private final TransferStage transferPipeline;

    @Autowired
    public AsnTransferLogic(final RpslObjectDao rpslObjectDao,
                            final AuthoritativeResourceService authoritativeResourceService,
                            final @Value("${whois.source}") String source) {
        this.rpslObjectDao = rpslObjectDao;
        this.authoritativeResourceService = authoritativeResourceService;
        this.source = source;

        LOGGER.info("AsnTransfersHandler: Using source " + source);

        this.transferPipeline = new DeleteOriginalBlockStage(source)
                .next(new ShrinkBlockStage(source))
                .next(new SplitBlockStage(source))
                .next(new DeleteDestinationBlockStage(source))
                .next(new ExtendPrecedingBlockStage(source))
                .next(new ExtendFollowingBlockStage(source))
                .next(new CreateNewPrecedingBlockStage(source))
                .next(new CreateNewFollowingBlockStage(source))
                .next(new MergeSurroundingBlocksStage(source));
    }

    public List<ActionRequest> transferInSteps(final String stringAutNum) {

        validateAutnumInput(stringAutNum);

        final Transfer<Asn> transfer = AsnTransfer.buildIncoming(stringAutNum);
        final Optional<RpslObject> originalAsBlock = findBlock(transfer.getResource());

        validateTargetBlock(transfer, originalAsBlock);

        if (AsnTransfer.isRipeBlock(originalAsBlock.get())) {
            return Collections.emptyList();
        }

        return getTransferSteps(transfer, originalAsBlock.get());
    }

    public List<ActionRequest> getTransferOutSteps(final String stringAutNum) {

        validateAutnumInput(stringAutNum);

        final Transfer<Asn> transfer = AsnTransfer.buildOutgoing(stringAutNum);
        final Optional<RpslObject> originalAsBlock = findBlock(transfer.getResource());

        validateTargetBlock(transfer, originalAsBlock);

        if (AsnTransfer.isNonRipeBlock(originalAsBlock.get())) {
            return Collections.emptyList();
        }

        return getTransferSteps(transfer, originalAsBlock.get());
    }

    private void validateAutnumInput(final String rawInput) {
        try {
            Asn.of(rawInput);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    private void validateTargetBlock(final Transfer<Asn> transfer, final Optional<RpslObject> originalAsBlock) {

        Preconditions.checkArgument(transfer != null);
        Preconditions.checkArgument(originalAsBlock != null);

        LOGGER.debug("Target exists {}", originalAsBlock.isPresent());

        if (originalAsBlock.isPresent() == false) {
            throw new NotFoundException("Block not found");
        }

        if (!AsnTransfer.isRipeBlock(originalAsBlock.get()) &&
                !AsnTransfer.isNonRipeBlock(originalAsBlock.get()) &&
                !AsnTransfer.isIanaBlock(originalAsBlock.get())) {
            throw new BadRequestException("Block is not recognizable: " + originalAsBlock.get().getKey());
        }

        if (AsnTransfer.isIanaBlock(originalAsBlock.get())) {
            throw new BadRequestException("IANA blocks are not available for transfers: " + transfer.getResource());
        }
    }

    private List<ActionRequest> getTransferSteps(final Transfer<Asn> transfer, final RpslObject originalAsBlock) {
        LOGGER.debug("Start performing {}", transfer);

        Preconditions.checkArgument(transfer != null);
        Preconditions.checkArgument(originalAsBlock != null);

        // get neighbours or absent when to direct neighbour is found
        Optional<RpslObject> precedingBlock = getLeftDirectNeighbour(transfer, originalAsBlock);
        Optional<RpslObject> followingBlock = getRightDirectNeighbour(transfer, originalAsBlock);

        // Push the context through all stages of the pipeline
        final List<ActionRequest> requests = transferPipeline.doTransfer(
                transfer, precedingBlock, originalAsBlock, followingBlock);

        logSteps(requests);

        return requests;
    }

    private void logSteps(final List<ActionRequest> requests) {
        LOGGER.info("Asn-transfer-in tasks:{}", requests.size());
        for (ActionRequest req : requests) {
            LOGGER.info("action:{} {}", req.getAction(), req.getRpslObject().getFormattedKey());
        }
    }

    private Optional<RpslObject> getLeftDirectNeighbour(final Transfer<Asn> transfer, final RpslObject originalAsBlock) {
        return getDirectNeighbour(transfer, originalAsBlock, true);
    }

    private Optional<RpslObject> getRightDirectNeighbour(final Transfer<Asn> transfer, final RpslObject originalAsBlock) {
        return getDirectNeighbour(transfer, originalAsBlock, false);
    }

    private Optional<RpslObject> getDirectNeighbour(final Transfer<Asn> transfer, final RpslObject originalAsBlock, boolean left) {
        Preconditions.checkArgument(transfer != null);
        Preconditions.checkArgument(originalAsBlock != null);

        Optional<RpslObject> neighbour;
        if (left) {
            neighbour = findBlock(transfer.getResource().previous());
        } else {
            neighbour = findBlock(transfer.getResource().next());
        }
        if (neighbour.isPresent()) {
            if (neighbour.get().getKey().equals(originalAsBlock.getKey())) {
                // not other block next door left
                neighbour = Optional.absent();
            } else if (AsnTransfer.isIanaBlock(neighbour.get())) {
                // ignore previous iana block
                neighbour = Optional.absent();
            }
            if (AsnTransfer.belongToSameRegion(originalAsBlock, neighbour)) {
                // This situation should not occur and requires manual fixing first
                final String errorMsg =
                        String.format("Adjacent block %s should be merged with current block %s first",
                                neighbour.get().getKey(),
                                originalAsBlock.getKey());
                LOGGER.warn("{}: {}", transfer, errorMsg);
                throw new BadRequestException(errorMsg);
            }
        }

        return neighbour;
    }

    private String parseErrors(final RestClientException e) {
        final StringBuilder builder = new StringBuilder();

        for (ErrorMessage errorMessage : e.getErrorMessages()) {
            if ("Error".equals(errorMessage.getSeverity())) {
                builder.append(errorMessage.toString());
                builder.append('\n');
            }
        }

        return builder.toString();
    }

    private Optional<RpslObject> findBlock(Asn asn) {
        final long begin, end;
        begin = end = asn.asBigInteger().longValue();
        RpslObject obj = rpslObjectDao.findAsBlock(begin, end);
        if (obj == null) {
            return Optional.absent();
        }

        return Optional.of(obj);
    }

    public void updateAuthoritativeResources(final List<ActionRequest> actionRequests) {
        try {
            for (ActionRequest actionRequest : actionRequests) {
                if (AsnTransfer.isAsBlock(actionRequest.getRpslObject())) {
                    switch (actionRequest.getAction()) {
                        case DELETE:
                            LOGGER.info("Updating authoritativeResources: {} {}",actionRequest.getAction(),actionRequest.getRpslObject().getFormattedKey() );
                            authoritativeResourceService.transferOutAsBlock(actionRequest.getRpslObject().getKey().toString());
                            break;
                        case CREATE:
                            if (AsnTransfer.isAsBlockInPrimarySource(actionRequest.getRpslObject())) {
                                LOGGER.info("Updating authoritativeResources: {} {}",actionRequest.getAction(),actionRequest.getRpslObject().getFormattedKey() );
                                authoritativeResourceService.transferInAsBlock(actionRequest.getRpslObject().getKey().toString());
                            }
                            break;
                        default:
                            // ignore
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to update authoritative resources due to {}", e.getMessage());
        }
    }
}


