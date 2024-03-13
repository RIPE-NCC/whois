package net.ripe.db.whois.update.handler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.source.SourceContext;
import net.ripe.db.whois.update.dns.DnsChecker;
import net.ripe.db.whois.update.domain.Ack;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.log.LogCallback;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import net.ripe.db.whois.update.sso.SsoTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

@Component
public class UpdateRequestHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRequestHandler.class);

    private final SourceContext sourceContext;
    private final ResponseFactory responseFactory;
    private final SingleUpdateHandler singleUpdateHandler;
    private final MultipleUpdateHandler multipleUpdateHandler;
    private final LoggerContext loggerContext;
    private final DnsChecker dnsChecker;
    private final SsoTranslator ssoTranslator;
    private final UpdateNotifier updateNotifier;
    private final UpdateLog updateLog;

    @Autowired
    public UpdateRequestHandler(final SourceContext sourceContext,
                                final ResponseFactory responseFactory,
                                final SingleUpdateHandler singleUpdateHandler,
                                final MultipleUpdateHandler multipleUpdateHandler,
                                final LoggerContext loggerContext,
                                final DnsChecker dnsChecker,
                                final SsoTranslator ssoTranslator,
                                final UpdateNotifier updateNotifier,
                                final UpdateLog updateLog) {
        this.sourceContext = sourceContext;
        this.responseFactory = responseFactory;
        this.singleUpdateHandler = singleUpdateHandler;
        this.multipleUpdateHandler = multipleUpdateHandler;
        this.loggerContext = loggerContext;
        this.dnsChecker = dnsChecker;
        this.ssoTranslator = ssoTranslator;
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

        try {
            sourceContext.setCurrentSourceToWhoisMaster();

            dnsChecker.checkAll(updateRequest, updateContext);

            for (final Update update : updateRequest.getUpdates()) {
                ssoTranslator.populateCacheAuthToUuid(updateContext, update);
            }

            final UpdateResponse updateResponse;

            if (updateContext.isBatchUpdate()) {
                processUpdateQueueBatchUpdate(updateRequest, updateContext);

                updateResponse = createUpdateResponse(updateRequest, updateContext);

                if (updateResponse.getStatus().equals(UpdateStatus.SUCCESS)) {
                    // only send notifications on complete success
                    updateNotifier.sendNotifications(updateRequest, updateContext);
                }
            } else {
                processUpdateQueueOneByOne(updateRequest, updateContext);

                // Create update response before sending notifications, so in case of an exception
                // while creating the response we didn't send any notifications
                updateResponse = createUpdateResponse(updateRequest, updateContext);

                updateNotifier.sendNotifications(updateRequest, updateContext);
            }

            return updateResponse;

        } finally {
            sourceContext.removeCurrentSource();
        }
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

    private void processUpdateQueueOneByOne(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        Collection<Update> updates = updateRequest.getUpdates();

        if (updates.size() == 1) {
            attemptUpdatesOneByOne(updateRequest, updateContext, updates);
        } else {
            while (!updates.isEmpty()) {
                final Collection<Update> reattemptQueue = attemptUpdatesOneByOne(updateRequest, updateContext, updates);

                if (reattemptQueue.size() == updates.size()) {
                    break;
                }

                updates = reattemptQueue;

                for (final Update update : updates) {
                    updateContext.prepareForReattempt(update);
                }
            }
        }
    }

    private Collection<Update> attemptUpdatesOneByOne(final UpdateRequest updateRequest, final UpdateContext updateContext, final Collection<Update> updates) {
        final Collection<Update> reattemptQueue = Lists.newArrayList();
        for (final Update update : updates) {
            final Stopwatch stopwatch = Stopwatch.createStarted();

            try {
                loggerContext.logUpdateStarted(update);
                singleUpdateHandler.handle(updateRequest.getOrigin(), updateRequest.getKeyword(), update, updateContext);
                loggerContext.logUpdateCompleted(update);
            } catch (UpdateAbortedException e) {
                loggerContext.logUpdateCompleted(update);
            } catch (DnsCheckFailedException e) {
                updateContext.failedUpdate(update);
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
        return reattemptQueue;
    }

    private void processUpdateQueueBatchUpdate(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        try {
            multipleUpdateHandler.handle(updateRequest, updateContext);
        } catch (RuntimeException e) {
            // already handled
        }
    }

}
