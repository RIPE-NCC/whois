package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.autokey.AutoKeyResolver;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.log.LoggerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Component
class TransactionalSingleUpdateHandler implements SingleUpdateHandler {
    private final AutoKeyResolver autoKeyResolver;
    private final AttributeSanitizer attributeSanitizer;
    private final AttributeGenerator attributeGenerator;
    private final RpslObjectDao rpslObjectDao;
    private final RpslObjectUpdateDao rpslObjectUpdateDao;
    private final UpdateLockDao updateLockDao;
    private final LoggerContext loggerContext;
    private final Authenticator authenticator;
    private final UpdateObjectHandler updateObjectHandler;
    private final IpTreeUpdater ipTreeUpdater;
    private final PendingUpdateHandler pendingUpdateHandler;
    private CIString source;

    @Value("${whois.source}")
    void setSource(final String source) {
        this.source = ciString(source);
    }

    @Autowired
    public TransactionalSingleUpdateHandler(final AutoKeyResolver autoKeyResolver, final AttributeGenerator attributeGenerator, final AttributeSanitizer attributeSanitizer, final UpdateLockDao updateLockDao, final LoggerContext loggerContext, final Authenticator authenticator, final UpdateObjectHandler updateObjectHandler, final RpslObjectDao rpslObjectDao, final RpslObjectUpdateDao rpslObjectUpdateDao, final IpTreeUpdater ipTreeUpdater, final PendingUpdateHandler pendingUpdateHandler) {
        this.autoKeyResolver = autoKeyResolver;
        this.attributeGenerator = attributeGenerator;
        this.attributeSanitizer = attributeSanitizer;
        this.rpslObjectDao = rpslObjectDao;
        this.rpslObjectUpdateDao = rpslObjectUpdateDao;
        this.updateLockDao = updateLockDao;
        this.loggerContext = loggerContext;
        this.authenticator = authenticator;
        this.updateObjectHandler = updateObjectHandler;
        this.ipTreeUpdater = ipTreeUpdater;
        this.pendingUpdateHandler = pendingUpdateHandler;
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void handle(final Origin origin, final Keyword keyword, final Update update, final UpdateContext updateContext) {
        updateLockDao.setUpdateLock();
        ipTreeUpdater.updateCurrent();

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);
        final RpslObject updatedObject = getUpdatedObject(update, updateContext, keyword);
        final RpslObject originalObject = getOriginalObject(updatedObject, update, updateContext, overrideOptions);
        final Action action = getAction(originalObject, updatedObject, update, updateContext, keyword);
        updateContext.setAction(update, action);

        checkForUnexpectedModification(update);

        PreparedUpdate preparedUpdate = new PreparedUpdate(update, originalObject, updatedObject, action, overrideOptions);
        updateContext.setPreparedUpdate(preparedUpdate);

        if (updateContext.hasErrors(preparedUpdate)) {
            throw new UpdateFailedException();
        }

        if (Action.DELETE.equals(preparedUpdate.getAction()) && !preparedUpdate.hasOriginalObject()) {
            updateContext.addMessage(preparedUpdate, UpdateMessages.objectNotFound(preparedUpdate.getKey()));
            throw new UpdateFailedException();
        }

        final RpslObject objectWithResolvedKeys = autoKeyResolver.resolveAutoKeys(updatedObject, update, updateContext, action);
        preparedUpdate = new PreparedUpdate(update, originalObject, objectWithResolvedKeys, action, overrideOptions);

        loggerContext.logPreparedUpdate(preparedUpdate);
        authenticator.authenticate(origin, preparedUpdate, updateContext);
        final boolean businessRulesOk = updateObjectHandler.validateBusinessRules(preparedUpdate, updateContext);
        final boolean pendingAuthentication = UpdateStatus.PENDING_AUTHENTICATION.equals(updateContext.getStatus(preparedUpdate));

        if ((pendingAuthentication && !businessRulesOk) || (!pendingAuthentication && updateContext.hasErrors(update))) {
            throw new UpdateFailedException();
        }

        if (pendingAuthentication) {
            updateContext.addMessage(preparedUpdate, UpdateMessages.updatePendingAuthentication());
            updateContext.addMessage(preparedUpdate, UpdateMessages.updatePendingAuthenticationSaved(preparedUpdate.getUpdatedObject()));
            pendingUpdateHandler.handle(preparedUpdate, updateContext);
        } else {
            updateObjectHandler.execute(preparedUpdate, updateContext);
        }

        updateContext.setPreparedUpdate(preparedUpdate);
    }

    @CheckForNull
    private RpslObject getOriginalObject(final RpslObject updatedObject, final Update update, final UpdateContext updateContext, final OverrideOptions overrideOptions) {
        if (overrideOptions.isObjectIdOverride()) {
            final int objectId = overrideOptions.getObjectId();
            try {
                final RpslObject rpslObject = rpslObjectDao.getById(objectId);

                final ObjectType objectType = update.getType();
                final CIString key = update.getSubmittedObject().getKey();
                if (!objectType.equals(rpslObject.getType()) || !key.equals(rpslObject.getKey())) {
                    updateContext.addMessage(update, UpdateMessages.overrideOriginalMismatch(objectId, objectType, key));
                }

                return attributeSanitizer.sanitize(rpslObject, new ObjectMessages());
            } catch (EmptyResultDataAccessException e) {
                updateContext.addMessage(update, UpdateMessages.overrideOriginalNotFound(objectId));
                return null;
            }
        } else {
            final CIString key = updatedObject.getKey();

            try {
                final RpslObject originalObject = rpslObjectDao.getByKey(updatedObject.getType(), key);
                return attributeSanitizer.sanitize(originalObject, new ObjectMessages());
            } catch (EmptyResultDataAccessException e) {
                return null;
            } catch (IncorrectResultSizeDataAccessException e) {
                throw new IllegalStateException(String.format("Invalid number of results for %s", key), e);
            }
        }
    }

    private RpslObject getUpdatedObject(final Update update, final UpdateContext updateContext, final Keyword keyword) {
        RpslObject updatedObject = attributeSanitizer.sanitize(update.getSubmittedObject(), updateContext.getMessages(update));

        if (!Operation.DELETE.equals(update.getOperation())) {
            updatedObject = attributeGenerator.generateAttributes(updatedObject, update, updateContext);
        }

        if (RpslObjectFilter.isFiltered(updatedObject)) {
            updateContext.addMessage(update, UpdateMessages.filteredNotAllowed());
        }

        final CIString objectSource = new RpslObjectFilter(updatedObject).getSource();
        if (!source.equals(objectSource)) {
            updateContext.addMessage(update, UpdateMessages.unrecognizedSource(objectSource));
        }

        if (Operation.DELETE.equals(update.getOperation())) {
            if (Keyword.NEW.equals(keyword)) {
                updateContext.addMessage(update, UpdateMessages.operationNotAllowedForKeyword(keyword, update.getOperation()));
            }

            if (update.getDeleteReasons().size() > 1) {
                updateContext.addMessage(update, UpdateMessages.multipleReasonsSpecified(update.getOperation()));
            }
        } else {
            final ObjectMessages messages = ObjectTemplate.getTemplate(updatedObject.getType()).validate(updatedObject);
            if (messages.hasMessages()) {
                updateContext.addMessages(update, messages);
            }
        }

        return updatedObject;
    }

    private Action getAction(@Nullable final RpslObject originalObject, final RpslObject updatedObject, final Update update, final UpdateContext updateContext, final Keyword keyword) {
        if (Operation.DELETE.equals(update.getOperation())) {
            return Action.DELETE;
        }

        if (Keyword.NEW.equals(keyword) || originalObject == null) {
            return Action.CREATE;
        }

        if (originalObject.equals(updatedObject) && !updateContext.hasErrors(update)) {
            return Action.NOOP;
        }

        return Action.MODIFY;
    }

    // `TODO: [AH] once the modify REST call is processed here, this can be dropped
    private void checkForUnexpectedModification(final Update update) {
        if (update.getSubmittedObjectInfo() != null) {
            final RpslObjectUpdateInfo latestUpdateInfo = rpslObjectUpdateDao.lookupObject(
                    update.getSubmittedObject().getType(),
                    update.getSubmittedObject().getKey().toString());

            if (latestUpdateInfo.getSequenceId() != update.getSubmittedObjectInfo().getSequenceId()) {
                throw new IllegalStateException("Object was modified unexpectedly");
            }
        }
    }
}
