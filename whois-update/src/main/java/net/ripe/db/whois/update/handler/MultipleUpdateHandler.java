package net.ripe.db.whois.update.handler;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * Make multiple updates, in a single transaction.
 *
 * If any update fails, the entire transaction is rolled back.
 *
 */
@Component
public class MultipleUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultipleUpdateHandler.class);

    private final SingleUpdateHandler singleUpdateHandler;
    private final LoggerContext loggerContext;
    private final UpdateLog updateLog;

    @Autowired
    public MultipleUpdateHandler(final SingleUpdateHandler singleUpdateHandler,
                                 final LoggerContext loggerContext,
                                 final UpdateLog updateLog) {
        this.singleUpdateHandler = singleUpdateHandler;
        this.loggerContext = loggerContext;
        this.updateLog = updateLog;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void handle(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        Collection<Update> updates = updateRequest.getUpdates();

        if (updates.size() == 1) {
            handle(updateRequest, updateContext, updates);
        } else {
            while (!updates.isEmpty()) {
                final List<Update> reattemptQueue = handle(updateRequest, updateContext, updates);

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

    private List<Update> handle(final UpdateRequest updateRequest, final UpdateContext updateContext, final Collection<Update> updates) {
        final List<Update> reattemptQueue = Lists.newArrayList();

        for (final Update update : updates) {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                loggerContext.logUpdateStarted(update);
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

        return reattemptQueue;
    }


}
