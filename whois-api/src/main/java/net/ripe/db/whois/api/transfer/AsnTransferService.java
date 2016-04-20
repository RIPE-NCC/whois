package net.ripe.db.whois.api.transfer;

import net.ripe.db.whois.api.rest.InternalUpdatePerformer;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
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
    public String transferIn(final HttpServletRequest request,
                             final String stringAutNum,
                             final String override) {

        try {

            // use master database for search
            sourceContext.setCurrentSourceToWhoisMaster();

            // No synchronisation of in memory-tree needed for aut-nums

            // Acquire lock to guarantee sequential processing of transfers
            transferUpdateLockDao.acquireUpdateLock();

            // Determine all individual steps needed to perform transfer
            List<ActionRequest> requests = this.asnTransfersHandler.transferInSteps(stringAutNum);
            if (requests.size() == 0) {
                return String.format("Resource %s is already RIPE", stringAutNum);
            }

            // batch update
            performUpdates(request, requests, override);

            this.asnTransfersHandler.updateAuthoritativeResources(requests);

            return String.format("Successfully transferred in aut-num %s", stringAutNum);

        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public String transferOut(HttpServletRequest request,
                              final String stringAutNum,
                              final String override) {
        try {
            // use master database also for search
            sourceContext.setCurrentSourceToWhoisMaster();

            // No synchronisation of in memory-tree needed for aut-nums

            // Acquire lock to guarantee sequential processing of transfers
            transferUpdateLockDao.acquireUpdateLock();

            // Determine all individual steps needed to perform transfer
            List<ActionRequest> requests = this.asnTransfersHandler.getTransferOutSteps(stringAutNum);
            if (requests.size() == 0) {
                return String.format("Resource %s is already non-RIPE", stringAutNum);
            }

            // batch update
            performUpdates(request, requests, override);
            this.asnTransfersHandler.updateAuthoritativeResources(requests);

            return String.format("Successfully transferred out aut-num %s", stringAutNum);

        } finally {
            sourceContext.removeCurrentSource();
        }
    }

}


