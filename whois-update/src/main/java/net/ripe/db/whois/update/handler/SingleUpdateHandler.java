package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.autokey.AutoKeyResolver;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.OverrideOptions;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateStatus;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.sso.SsoTranslator;
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
public class SingleUpdateHandler {
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
    private final SsoTranslator ssoTranslator;
    private CIString source;

    @Value("${whois.source}")
    void setSource(final String source) {
        this.source = ciString(source);
    }

    @Autowired
    public SingleUpdateHandler(final AutoKeyResolver autoKeyResolver,
                               final AttributeGenerator attributeGenerator,
                               final AttributeSanitizer attributeSanitizer,
                               final UpdateLockDao updateLockDao,
                               final LoggerContext loggerContext,
                               final Authenticator authenticator,
                               final UpdateObjectHandler updateObjectHandler,
                               final RpslObjectDao rpslObjectDao,
                               final RpslObjectUpdateDao rpslObjectUpdateDao,
                               final IpTreeUpdater ipTreeUpdater,
                               final PendingUpdateHandler pendingUpdateHandler,
                               final SsoTranslator ssoTranslator) {
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
        this.ssoTranslator = ssoTranslator;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRES_NEW)
    public void handle(final Origin origin, final Keyword keyword, final Update update, final UpdateContext updateContext) {
        updateLockDao.setUpdateLock();
        ipTreeUpdater.updateCurrent();

        if (updateContext.isDryRun()) {
            updateContext.addMessage(update, UpdateMessages.dryRunNotice());
        }

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);
        RpslObject updatedObject = getUpdatedObject(update, updateContext, keyword);
        final RpslObject originalObject = getOriginalObject(updatedObject, update, updateContext, overrideOptions);
        final Action action = getAction(originalObject, updatedObject, update, updateContext, keyword);
        updateContext.setAction(update, action);

        checkForUnexpectedModification(update);

        if (action == Action.NOOP){
            updatedObject = originalObject;
        }
        PreparedUpdate preparedUpdate = new PreparedUpdate(update, originalObject, updatedObject, action, overrideOptions);
        updateContext.setPreparedUpdate(preparedUpdate);

        if (updateContext.hasErrors(preparedUpdate)) {
            throw new UpdateFailedException();
        }

        if (Action.DELETE.equals(preparedUpdate.getAction()) && !preparedUpdate.hasOriginalObject()) {
            updateContext.addMessage(preparedUpdate, UpdateMessages.objectNotFound(preparedUpdate.getFormattedKey()));
            throw new UpdateFailedException();
        }

        // TODO: [AH] Hard to follow code from here. Refactor on next change.
        final RpslObject objectWithResolvedKeys = autoKeyResolver.resolveAutoKeys(updatedObject, update, updateContext, action);
        preparedUpdate = new PreparedUpdate(update, originalObject, objectWithResolvedKeys, action, overrideOptions);

        loggerContext.logPreparedUpdate(preparedUpdate);
        authenticator.authenticate(origin, preparedUpdate, updateContext);
        final boolean businessRulesOk = updateObjectHandler.validateBusinessRules(preparedUpdate, updateContext);
        final boolean pendingAuthentication = UpdateStatus.PENDING_AUTHENTICATION.equals(updateContext.getStatus(preparedUpdate));

        if ((pendingAuthentication && !businessRulesOk) || (!pendingAuthentication && updateContext.hasErrors(update))) {
            throw new UpdateFailedException();
        }

        updateContext.setPreparedUpdate(preparedUpdate);

        if (updateContext.isDryRun()) {
            throw new UpdateAbortedException();
        } else if (pendingAuthentication) {
            pendingUpdateHandler.handle(preparedUpdate, updateContext);
        } else {
            updateObjectHandler.execute(preparedUpdate, updateContext);
        }
    }

    @CheckForNull
    private RpslObject getOriginalObject(final RpslObject updatedObject, final Update update, final UpdateContext updateContext, final OverrideOptions overrideOptions) {
        RpslObject originalObject;
        if (overrideOptions.isObjectIdOverride()) {
            final int objectId = overrideOptions.getObjectId();
            try {
                originalObject = rpslObjectDao.getById(objectId);

                final ObjectType objectType = update.getType();
                final CIString key = update.getSubmittedObject().getKey();
                if (!objectType.equals(originalObject.getType()) || !key.equals(originalObject.getKey())) {
                    updateContext.addMessage(update, UpdateMessages.overrideOriginalMismatch(objectId, objectType, key));
                }
            } catch (EmptyResultDataAccessException e) {
                updateContext.addMessage(update, UpdateMessages.overrideOriginalNotFound(objectId));
                return null;
            }
        } else {
            final CIString key = updatedObject.getKey();

            try {
                originalObject = rpslObjectDao.getByKey(updatedObject.getType(), key);
            } catch (EmptyResultDataAccessException e) {
                return null;
            } catch (IncorrectResultSizeDataAccessException e) {
                throw new IllegalStateException(String.format("Invalid number of results for %s", key), e);
            }
        }
        originalObject = ssoTranslator.translateFromCacheAuthToUsername(updateContext, originalObject);
        originalObject = attributeSanitizer.sanitize(originalObject, new ObjectMessages());
        return originalObject;
    }

    private RpslObject getUpdatedObject(final Update update, final UpdateContext updateContext, final Keyword keyword) {
        RpslObject updatedObject = update.getSubmittedObject();

        if (RpslObjectFilter.isFiltered(updatedObject)) {
            updateContext.addMessage(update, UpdateMessages.filteredNotAllowed());
        }

        final CIString objectSource = updatedObject.getValueOrNullForAttribute(AttributeType.SOURCE);
        if (objectSource != null && !source.equals(objectSource)) {
            updateContext.addMessage(update, UpdateMessages.unrecognizedSource(objectSource.toUpperCase()));
        }

        if (Operation.DELETE.equals(update.getOperation())) {
            if (Keyword.NEW.equals(keyword)) {
                updateContext.addMessage(update, UpdateMessages.operationNotAllowedForKeyword(keyword, update.getOperation()));
            }

            if (update.getDeleteReasons().size() > 1) {
                updateContext.addMessage(update, UpdateMessages.multipleReasonsSpecified(update.getOperation()));
            }
        } else {
            updatedObject = attributeSanitizer.sanitize(updatedObject, updateContext.getMessages(update));
            updatedObject = attributeGenerator.generateAttributes(updatedObject, update, updateContext);

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

    // TODO: [AH] Replace with versioning
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
