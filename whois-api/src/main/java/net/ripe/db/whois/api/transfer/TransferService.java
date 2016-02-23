package net.ripe.db.whois.api.transfer;

import com.google.common.collect.Lists;
import net.ripe.db.whois.api.rest.HttpRequestMessage;
import net.ripe.db.whois.api.rest.InternalUpdatePerformer;
import net.ripe.db.whois.api.rest.StreamingHelper;
import net.ripe.db.whois.api.rest.domain.Action;
import net.ripe.db.whois.api.rest.domain.ActionRequest;
import net.ripe.db.whois.api.rest.domain.ErrorMessage;
import net.ripe.db.whois.api.rest.domain.WhoisResources;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.api.transfer.lock.TransferUpdateLockDao;
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

public class TransferService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransferService.class);

    protected final LoggerContext loggerContext;
    protected final InternalUpdatePerformer updatePerformer;
    protected final TransferUpdateLockDao transferUpdateLockDao;
    protected final IpTreeUpdater ipTreeUpdater;

    public TransferService(final InternalUpdatePerformer updatePerformer, IpTreeUpdater ipTreeUpdater, final TransferUpdateLockDao updateLockDao,
                           final LoggerContext loggerContext) {
        this.updatePerformer = updatePerformer;
        this.ipTreeUpdater = ipTreeUpdater;
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
                    LOGGER.info("Error performing " + update.getOperation() + " on " +
                            update.getSubmittedObject().getFormattedKey() +
                            ", status: " + status);
                    if (status == UpdateStatus.FAILED_AUTHENTICATION) {
                        throw new TransferFailedException(Response.Status.UNAUTHORIZED, whoisResources);
                    } else if (status == UpdateStatus.EXCEPTION) {
                        throw new TransferFailedException(Response.Status.INTERNAL_SERVER_ERROR, whoisResources);
                    } else if (updateContext.getMessages(update).contains(UpdateMessages.newKeywordAndObjectExists())) {
                        throw new TransferFailedException(Response.Status.CONFLICT, whoisResources);
                    } else {
                        throw new TransferFailedException(Response.Status.BAD_REQUEST, whoisResources);
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

    protected Response createResponse(final HttpServletRequest request, final WhoisResources whoisResources, final Response.Status status) {
        final Response.ResponseBuilder responseBuilder = Response.status(status);
        return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
            }
        }).build();
    }

    protected Response createResponse(final HttpServletRequest request, final String errorMessage, final Response.Status status) {
        WhoisResources whoisResources = new WhoisResources();
        Messages.Type severity = status == Response.Status.OK ? Messages.Type.INFO : Messages.Type.ERROR;
        whoisResources.setErrorMessages(Lists.newArrayList(new ErrorMessage(
                new Message(severity, errorMessage, Collections.emptyList())
        )));
        final Response.ResponseBuilder responseBuilder = Response.status(status);
        return responseBuilder.entity(new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                StreamingHelper.getStreamingMarshal(request, output).singleton(whoisResources);
            }
        }).build();
    }
}
