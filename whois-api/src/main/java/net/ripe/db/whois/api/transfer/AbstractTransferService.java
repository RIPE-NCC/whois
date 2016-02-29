package net.ripe.db.whois.api.transfer;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.HttpRequestMessage;
import net.ripe.db.whois.api.rest.InternalUpdatePerformer;
import net.ripe.db.whois.api.rest.StreamingHelper;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.api.transfer.lock.TransferUpdateLockDao;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

public abstract class AbstractTransferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransferService.class);

    protected final SourceContext sourceContext;
    protected final LoggerContext loggerContext;
    protected final InternalUpdatePerformer updatePerformer;
    protected final TransferUpdateLockDao transferUpdateLockDao;

    public AbstractTransferService(final SourceContext sourceContext, final InternalUpdatePerformer updatePerformer,
                                   final TransferUpdateLockDao updateLockDao, final LoggerContext loggerContext) {
        this.sourceContext = sourceContext;
        this.updatePerformer = updatePerformer;
        this.transferUpdateLockDao = updateLockDao;
        this.loggerContext = loggerContext;
    }

    protected WhoisResources performUpdates(final HttpServletRequest request,
                                            final List<ActionRequest> actionRequests,
                                            final String override) {
        try {
            final Origin origin = updatePerformer.createOrigin(request);
            final UpdateContext updateContext = updatePerformer.initContext(origin, null);
            updateContext.batchUpdate();
            auditlogRequest(request);

            final List<Update> updates = Lists.newArrayList();
            for (ActionRequest actionRequest : actionRequests) {
                final String deleteReason = Action.DELETE.equals(actionRequest.getAction()) ? "--" : null;

                final RpslObject rpslObject = actionRequest.getRpslObject();
                updates.add(updatePerformer.createUpdate(
                        updateContext,
                        rpslObject,
                        Collections.emptyList(),
                        deleteReason,
                        override));
            }
            final WhoisResources whoisResources = updatePerformer.performUpdates(updateContext, origin, updates, Keyword.NONE, request);

            for (Update update : updates) {
                final UpdateStatus status = updateContext.getStatus(update);

                if (status == UpdateStatus.SUCCESS) {
                    // continue
                } else {
                   final String msg = String.format("Error performing %s on %s: status: %s",
                           update.getOperation(),
                           update.getSubmittedObject().getFormattedKey(),
                           status);
                    LOGGER.info(msg);
                    if (status == UpdateStatus.FAILED_AUTHENTICATION) {
                        throw new TransferFailedException(Response.Status.UNAUTHORIZED, msg);
                    } else if (status == UpdateStatus.EXCEPTION) {
                        throw new TransferFailedException(Response.Status.INTERNAL_SERVER_ERROR, msg);
                    } else if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
                        throw new TransferFailedException(Response.Status.CONFLICT, msg);
                    } else {
                        throw new TransferFailedException(Response.Status.BAD_REQUEST, msg);
                    }
                }
            }

            return whoisResources;

        } catch (TransferFailedException e) {
            throw e;
        } catch (Exception e) {
            updatePerformer.logError(e);
            throw e;
        } finally {
            updatePerformer.closeContext();
        }
    }

    private void auditlogRequest(final HttpServletRequest request) {
        loggerContext.log(new HttpRequestMessage(request));
    }

}
