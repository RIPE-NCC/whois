package net.ripe.db.whois.api.transfer;

import net.ripe.db.whois.api.rest.InternalUpdatePerformer;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.transfer.lock.TransferUpdateLockDao;
import net.ripe.db.whois.api.transfer.logic.asn.AsnTransferLogic;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.log.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;
import java.util.List;

@Component
public class AsnTransferService extends AbstractTransferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsnTransfersRestService.class);
    private AsnTransferLogic asnTransfersHandler;

    @Autowired
    public AsnTransferService(final SourceContext sourceContext,
                              final TransferUpdateLockDao updateLockDao,
                              final LoggerContext loggerContext,
                              final InternalUpdatePerformer updatePerformer,
                              final AsnTransferLogic asnTransfersHandler) {
        super(sourceContext, updatePerformer, updateLockDao, loggerContext);
        this.asnTransfersHandler = asnTransfersHandler;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Response transferIn(final HttpServletRequest request,
                               final String stringAutNum,
                               final String override) {

        try {

            // use master database for search
            sourceContext.setCurrentSourceToWhoisMaster();

            // No synchronisation of in memory-tree needed for aut-nums

            // Acquire lock to guarantee sequential processing of transfers
            transferUpdateLockDao.acquireUpdateLock();

            // Determine all steps for transfer
            List<ActionRequest> requests = this.asnTransfersHandler.transferInSteps(stringAutNum);
            if (requests.size() == 0) {
                return createResponse(request, String.format("Resource %s is already RIPE", stringAutNum), Response.Status.OK);
            }

            // batch update
            final WhoisResources whoisResources = performUpdates(request, requests, override);

            this.asnTransfersHandler.updateAuthoritativeResources(requests);

            return createResponse(request, whoisResources, Response.Status.OK);

        } catch (ClientErrorException exc) {
            LOGGER.info("ClientErrorException: {}", exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.fromStatusCode(exc.getResponse().getStatus()));
        } catch (TransferFailedException exc) {
            LOGGER.info("TransferFailedException {}", exc.getWhoisResources().getErrorMessages());
            return createResponse(request, exc.getWhoisResources(), exc.getStatus());
        } catch (Exception exc) {
            LOGGER.info("Exception: {}", exc.getMessage());
            return createResponse(request, "", Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public Response transferOut(HttpServletRequest request,
                                final String stringAutNum,
                                final String override) {
        try {
            // use master database also for search
            sourceContext.setCurrentSourceToWhoisMaster();

            // No synchronisation of in memory-tree needed for aut-nums

            // Acquire lock to guarantee sequential processing of transfers
            transferUpdateLockDao.acquireUpdateLock();

            // Determine all steps for transfer
            List<ActionRequest> requests = this.asnTransfersHandler.getTransferOutSteps(stringAutNum);
            if (requests.size() == 0) {
                return createResponse(request, String.format("Resource %s is already non-RIPE", stringAutNum), Response.Status.OK);
            }

            // batch update
            final WhoisResources whoisResources = performUpdates(request, requests, override);
            this.asnTransfersHandler.updateAuthoritativeResources(requests);

            return createResponse(request, whoisResources, Response.Status.OK);

        } catch (ClientErrorException exc) {
            LOGGER.info("ClientErrorException:{}",exc.getMessage());
            return createResponse(request, exc.getMessage(), Response.Status.fromStatusCode(exc.getResponse().getStatus()));
        } catch (TransferFailedException exc) {
            LOGGER.info("TransferFailedException:{}",exc.getMessage());
            return createResponse(request, exc.getMessage(), exc.getStatus());
        } catch (Exception exc) {
            LOGGER.info("Exception:{}",exc.getMessage());
            return createResponse(request, "", Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

}


