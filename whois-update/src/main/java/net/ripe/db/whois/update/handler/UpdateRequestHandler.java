package net.ripe.db.whois.update.handler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.dns.DnsChecker;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.log.LogCallback;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

@Component
public class UpdateRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRequestHandler.class);

    private final SourceContext sourceContext;
    private final ResponseFactory responseFactory;
    private final SingleUpdateHandler singleUpdateHandler;
    private final LoggerContext loggerContext;
    private final DnsChecker dnsChecker;
    private final UpdateNotifier updateNotifier;
    private final UpdateLog updateLog;

    @Autowired
    public UpdateRequestHandler(final SourceContext sourceContext,
                                final ResponseFactory responseFactory,
                                final SingleUpdateHandler singleUpdateHandler,
                                final LoggerContext loggerContext,
                                final DnsChecker dnsChecker,
                                final UpdateNotifier updateNotifier,
                                final UpdateLog updateLog) {
        this.sourceContext = sourceContext;
        this.responseFactory = responseFactory;
        this.singleUpdateHandler = singleUpdateHandler;
        this.loggerContext = loggerContext;
        this.dnsChecker = dnsChecker;
        this.updateNotifier = updateNotifier;
        this.updateLog = updateLog;
    }

    public UpdateResponse handle(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        UpdateResponse updateResponse;
        try {
            updateResponse = handleUpdateRequest(updateRequest, updateContext);
        } catch (RuntimeException e) {
            LOGGER.error("Handling update request", e);
            updateResponse = new UpdateResponse(UpdateStatus.EXCEPTION, responseFactory.createExceptionResponse(updateContext, updateRequest.getOrigin()));
        }

        return updateResponse;
    }

    private UpdateResponse handleUpdateRequest(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        final Keyword keyword = updateRequest.getKeyword();
        if (Keyword.HELP.equals(keyword) || Keyword.HOWTO.equals(keyword)) {
            return new UpdateResponse(UpdateStatus.SUCCESS, responseFactory.createHelpResponse(updateContext, updateRequest.getOrigin()));
        }

        final List<Update> updates = updateRequest.getUpdates();
        if (updateContext.isDryRun() && updates.size() > 1) {
            for (final Update update : updates) {
                updateContext.failedUpdate(update, UpdateMessages.dryRunOnlySupportedOnSingleUpdate());
            }

            return createUpdateResponse(updateRequest, updateContext);
        }

        try {
            sourceContext.setCurrentSourceToWhoisMaster();
            return handleUpdates(updateRequest, updateContext);
        } finally {
            sourceContext.removeCurrentSource();
        }
    }

    private UpdateResponse handleUpdates(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        dnsChecker.checkAll(updateRequest, updateContext);

        processUpdateQueue(updateRequest, updateContext);

        // Create update response before sending notifications, so in case of an exception
        // while creating the response we didn't send any notifications
        final UpdateResponse updateResponse = createUpdateResponse(updateRequest, updateContext);

        if (updateRequest.isNotificationsEnabled()) {
            updateNotifier.sendNotifications(updateRequest, updateContext);
        }

        return updateResponse;
    }

    private UpdateResponse createUpdateResponse(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        final Ack ack = updateContext.createAck();
        final String ackResponse = responseFactory.createAckResponse(updateContext, updateRequest.getOrigin(), ack);
        loggerContext.log("ack.txt", new LogCallback() {
            @Override
            public void log(final OutputStream outputStream) throws IOException {
                outputStream.write(ackResponse.getBytes());
            }
        });

        return new UpdateResponse(ack.getUpdateStatus(), ackResponse);
    }

    private void processUpdateQueue(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        List<Update> updates = updateRequest.getUpdates();

        while (!updates.isEmpty()) {
            final List<Update> reattemptQueue = Lists.newArrayList();

            for (final Update update : updates) {
                final Stopwatch stopwatch = new Stopwatch().start();

                try {
                    loggerContext.logUpdateStarted(update);
                    dnsChecker.check(update, updateContext);
                    singleUpdateHandler.handle(updateRequest.getOrigin(), updateRequest.getKeyword(), update, updateContext);
                    loggerContext.logUpdateCompleted(update);
                } catch (UpdateAbortedException e) {
                    loggerContext.logUpdateCompleted(update);
                } catch (UpdateFailedException e) {
                    updateContext.failedUpdate(update);
                    reattemptQueue.add(update);
                    loggerContext.logUpdateCompleted(update);
                } catch (RuntimeException e) {
                    updateContext.failedUpdate(update, UpdateMessages.unexpectedError());
                    loggerContext.logUpdateFailed(update, e);
                    LOGGER.error("Updating {}", update.getSubmittedObject().getFormattedKey(), e);
                } finally {
                    updateLog.logUpdateResult(updateRequest, updateContext, update, stopwatch.stop());
                }
            }

            updates = reattemptQueue.size() < updates.size() ? reattemptQueue : Collections.<Update>emptyList();
            for (final Update update : updates) {
                updateContext.prepareForReattempt(update);
            }
        }
    }
}
