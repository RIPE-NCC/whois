package net.ripe.db.whois.update.handler;


import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
class PendingUpdateHandler {
    private final PendingUpdateDao pendingUpdateDao;
    private final Authenticator authenticator;
    private final UpdateObjectHandler updateObjectHandler;
    private final DateTimeProvider dateTimeProvider;
    private final LoggerContext loggerContext;

    @Autowired
    public PendingUpdateHandler(final PendingUpdateDao pendingUpdateDao, final Authenticator authenticator, final UpdateObjectHandler updateObjectHandler, final DateTimeProvider dateTimeProvider, final LoggerContext loggerContext) {
        this.pendingUpdateDao = pendingUpdateDao;
        this.authenticator = authenticator;
        this.updateObjectHandler = updateObjectHandler;
        this.dateTimeProvider = dateTimeProvider;
        this.loggerContext = loggerContext;
    }

    public void handle(final PreparedUpdate preparedUpdate, final UpdateContext updateContext) {
        final RpslObject rpslObject = preparedUpdate.getUpdatedObject();
        final PendingUpdate pendingUpdate = updateContext.getPendingUpdate(preparedUpdate);
        final Set<String> passedAuthentications = updateContext.getSubject(preparedUpdate).getPassedAuthentications();

        if (pendingUpdate == null) {
            loggerContext.log(new Message(Messages.Type.INFO, "No pending updates found; storing in DB"));
            pendingUpdateDao.store(new PendingUpdate(passedAuthentications, rpslObject, dateTimeProvider.getCurrentDateTime()));
            updateContext.addMessage(preparedUpdate, UpdateMessages.updatePendingAuthentication());
            updateContext.addMessage(preparedUpdate, UpdateMessages.updatePendingAuthenticationSaved(preparedUpdate.getUpdatedObject()));
        } else {
            final PendingUpdate updatedPendingUpdate = pendingUpdate.addPassedAuthentications(passedAuthentications);

            if (authenticator.isAuthenticationForTypeComplete(rpslObject.getType(), updatedPendingUpdate)) {
                loggerContext.log(new Message(Messages.Type.INFO, "Pending update found and completes authentication; dropping from DB"));
                pendingUpdateDao.remove(pendingUpdate);

                updateContext.prepareForReattempt(preparedUpdate);
                updateContext.addMessage(preparedUpdate, UpdateMessages.updateConcludesPendingUpdate(preparedUpdate.getUpdatedObject()));

                final PreparedUpdate freshPreparedUpdate = new PreparedUpdate(preparedUpdate.getUpdate(), null, pendingUpdate.getObject(), Action.CREATE);
                updateContext.setPreparedUpdate(freshPreparedUpdate);
                updateContext.setAction(freshPreparedUpdate, Action.CREATE);

                updateObjectHandler.execute(freshPreparedUpdate, updateContext);
            } else {
                loggerContext.log(new Message(Messages.Type.INFO, String.format("Pending update found but still doesn't complete authentication; updating DB: %s", updatedPendingUpdate.getPassedAuthentications().toString())));

                if (updatedPendingUpdate.getPassedAuthentications().size() > pendingUpdate.getPassedAuthentications().size()) {
                    pendingUpdateDao.updatePassedAuthentications(updatedPendingUpdate);
                } else {
                    updateContext.setAction(preparedUpdate, Action.NOOP);
                }
            }
        }
    }
}
