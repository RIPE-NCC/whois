package net.ripe.db.whois.update.handler;


import com.google.common.collect.Sets;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.log.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.CheckForNull;
import java.util.Set;

@Component
class PendingUpdateHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingUpdateHandler.class);

    private final PendingUpdateDao pendingUpdateDao;
    private final Authenticator authenticator;
    private final UpdateObjectHandler updateObjectHandler;
    private final DateTimeProvider dateTimeProvider;
    private final LoggerContext loggerContext;

    @Autowired
    public PendingUpdateHandler(final PendingUpdateDao pendingUpdateDao, final Authenticator authenticator, UpdateObjectHandler updateObjectHandler, DateTimeProvider dateTimeProvider, LoggerContext loggerContext) {
        this.pendingUpdateDao = pendingUpdateDao;
        this.authenticator = authenticator;
        this.updateObjectHandler = updateObjectHandler;
        this.dateTimeProvider = dateTimeProvider;
        this.loggerContext = loggerContext;
    }

    public void handle(final PreparedUpdate preparedUpdate, final UpdateContext updateContext) {
        final RpslObject rpslObject = preparedUpdate.getUpdatedObject();
        final PendingUpdate pendingUpdate = find(rpslObject);
        final Set<String> passedAuthentications = updateContext.getSubject(preparedUpdate).getPassedAuthentications();

        if (pendingUpdate == null) {
            loggerContext.log(new Message(Messages.Type.INFO, "No pending updates found; storing in DB"));
            pendingUpdateDao.store(new PendingUpdate(passedAuthentications, rpslObject, dateTimeProvider.getCurrentDateTime()));
        } else {
            final Set<String> allPassedAuthentications = Sets.newHashSet();
            allPassedAuthentications.addAll(pendingUpdate.getPassedAuthentications());
            allPassedAuthentications.addAll(passedAuthentications);

            if (authenticator.isAuthenticationForTypeComplete(rpslObject.getType(), allPassedAuthentications)) {
                loggerContext.log(new Message(Messages.Type.INFO, "Pending update found and completes authentication; dropping from DB"));
                pendingUpdateDao.remove(pendingUpdate);

                updateContext.prepareForReattempt(preparedUpdate);
                updateContext.addMessage(preparedUpdate, UpdateMessages.updateConcludesPendingUpdate(preparedUpdate.getUpdatedObject()));

                updateObjectHandler.execute(preparedUpdate, updateContext);
            } else {
                updateContext.addMessage(preparedUpdate, UpdateMessages.updateAlreadyPendingAuthentication());
            }
        }
    }

    @CheckForNull
    private PendingUpdate find(final RpslObject object) {
        for (final PendingUpdate update : pendingUpdateDao.findByTypeAndKey(object.getType(), object.getKey().toString())) {
            if (object.equals(update.getObject())) {
                return update;
            }
        }
        return null;
    }
}
