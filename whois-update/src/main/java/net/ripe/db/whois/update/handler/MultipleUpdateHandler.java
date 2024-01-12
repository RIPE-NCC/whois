package net.ripe.db.whois.update.handler;

import com.google.common.base.Stopwatch;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
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
    private final IpTreeUpdater ipTreeUpdater;
    private final LoggerContext loggerContext;
    private final UpdateLog updateLog;

    @Autowired
    public MultipleUpdateHandler(final SingleUpdateHandler singleUpdateHandler,
                                 final IpTreeUpdater ipTreeUpdater,
                                 final LoggerContext loggerContext,
                                 final UpdateLog updateLog) {
        this.singleUpdateHandler = singleUpdateHandler;
        this.ipTreeUpdater = ipTreeUpdater;
        this.loggerContext = loggerContext;
        this.updateLog = updateLog;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void handle(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        for (final Update update : updateRequest.getUpdates()) {
            final Stopwatch stopwatch = Stopwatch.createStarted();
            try {
                loggerContext.logUpdateStarted(update);
                singleUpdateHandler.handle(updateRequest.getOrigin(), updateRequest.getKeyword(), update, updateContext);
                loggerContext.logUpdateCompleted(update);
            } catch (UpdateAbortedException e) {
                loggerContext.logUpdateCompleted(update);
                throw e;
            } catch (UpdateFailedException e) {
                ipTreeUpdater.update();
                updateContext.failedUpdate(update);
                loggerContext.logUpdateCompleted(update);
                throw e;
            } catch (RuntimeException e) {
                ipTreeUpdater.update();
                updateContext.failedUpdate(update, UpdateMessages.unexpectedError());
                loggerContext.logUpdateFailed(update, e);
                LOGGER.error("Updating {}", update.getSubmittedObject().getFormattedKey(), e);
                throw e;
            } finally {
                updateLog.logUpdateResult(updateRequest, updateContext, update, stopwatch.stop());
            }
        }

        if (updateContext.isDryRun()) {
            throw new UpdateAbortedException();
        }

    }

}
