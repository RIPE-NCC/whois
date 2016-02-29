package net.ripe.db.whois.api.transfer;


import net.ripe.db.whois.api.rest.InternalUpdatePerformer;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.transfer.lock.TransferUpdateLockDao;
import net.ripe.db.whois.api.transfer.logic.AuthoritativeResourceService;
import net.ripe.db.whois.api.transfer.logic.inetnum.InetnumTransfersLogic;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.Response.Status.OK;

@Component
public class InetnumTransfersService extends AbstractTransferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(InetnumTransfersService.class);
    private final IpTreeUpdater ipTreeUpdater;
    private final InetnumTransfersLogic inetnumTransfersLogic;
    private final AuthoritativeResourceService authoritativeResourceService;

    @Autowired
    public InetnumTransfersService(final SourceContext sourceContext,
                                   final InternalUpdatePerformer updatePerformer,
                                   final TransferUpdateLockDao updateLockDao,
                                   final LoggerContext loggerContext,
                                   final IpTreeUpdater ipTreeUpdater,
                                   final InetnumTransfersLogic inetnumTransfersHandler,
                                   final AuthoritativeResourceService authoritativeResourceService) {
        super(sourceContext, updatePerformer, updateLockDao, loggerContext);
        this.ipTreeUpdater = ipTreeUpdater;
        this.inetnumTransfersLogic = inetnumTransfersHandler;
        this.authoritativeResourceService = authoritativeResourceService;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public String transferOut(final HttpServletRequest request,
                                final String inetnum,
                                final String override) {
        try {
            // use master database: also for search
            sourceContext.setCurrentSourceToWhoisMaster();

            // Acquire lock to guarantee sequential processing of individual transfers
            transferUpdateLockDao.acquireUpdateLock();

            // make sure the ip-tree is in sync with the master database
            ipTreeUpdater.updateTransactional();

            validateInput(inetnum);

            // collect the individual steps that make a transfer
            final List<ActionRequest> requests = inetnumTransfersLogic.getTransferOutActions(inetnum);
            if (requests.size() == 0) {
                return String.format("Inetnum %s is already non-RIPE.", inetnum);
            }

            // perform the actual batch update
            final WhoisResources whoisResources = performUpdates(request, requests, override);

            authoritativeResourceService.transferOutIpv4Block(inetnum);

            return String.format( "Successfully transferred out inetnum %s", inetnum);

        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public String transferIn(final HttpServletRequest request,
                               final String inetnum,
                               final String override) {
        try {
            // use master database: also for search
            sourceContext.setCurrentSourceToWhoisMaster();

            // Acquire lock to guarantee sequential processing of individual transfers
            transferUpdateLockDao.acquireUpdateLock();

            // make sure the ip-tree is in sync with the master database
            ipTreeUpdater.updateTransactional();

            validateInput(inetnum);

            // collect the individual steps that make a transfer
            final List<ActionRequest> requests = inetnumTransfersLogic.getTransferInActions(inetnum);
            if (requests.size() == 0) {
                return String.format( "Inetnum %s is already RIPE.", inetnum);
            }

            // perform the actual batch update
            final WhoisResources whoisResources = performUpdates(request, requests, override);

            authoritativeResourceService.transferInIpv4Block(inetnum);

            return String.format( "Successfully transferred in inetnum %s", inetnum);

        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    private void validateInput(final String inetnum) {
        final RpslAttribute attr = new RpslAttribute(AttributeType.INETNUM, inetnum);
        final ObjectMessages msgs = new ObjectMessages();
        attr.validateSyntax(ObjectType.INETNUM, msgs);
        if (msgs.hasErrors()) {
            LOGGER.info("Inetnum {} has invalid syntax: {}", inetnum, msgs.toString());
            throw new BadRequestException("Inetnum " + inetnum + " has invalid syntax.");
        }
    }
}
