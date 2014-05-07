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
import net.ripe.db.whois.update.generator.AttributeGenerator;
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
    private final AttributeGenerator[] attributeGenerators;
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
                               final AttributeGenerator[] attributeGenerators,
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
        this.attributeGenerators = attributeGenerators;
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

        // FIXME: [AH] ATM nothing is setting submittedobjectinfo, rendering this call surplus
        checkForUnexpectedModification(update);

        if (updateContext.isDryRun()) {
            updateContext.addMessage(update, UpdateMessages.dryRunNotice());
        }

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);
        final RpslObject originalObject = getOriginalObject(update, updateContext, overrideOptions);
        RpslObject updatedObject = getUpdatedObject(originalObject, update, updateContext, keyword);
        Action action = getAction(originalObject, updatedObject, update, updateContext, keyword);
        updateContext.setAction(update, action);

        if (action == Action.NOOP) {
            updatedObject = originalObject;
        }

        if (action == Action.DELETE && originalObject == null) {
            updateContext.addMessage(update, UpdateMessages.objectNotFound(updatedObject.getFormattedKey()));
        }

        // up to this point, updatedObject could have structural+syntax errors (unknown attributes, etc...), bail out if so
        PreparedUpdate preparedUpdate = new PreparedUpdate(update, originalObject, updatedObject, action, overrideOptions);
        updateContext.setPreparedUpdate(preparedUpdate);
        if (updateContext.hasErrors(update)) {
            throw new UpdateFailedException();
        }

        // resolve AUTO- keys
        RpslObject updatedObjectWithAutoKeys = autoKeyResolver.resolveAutoKeys(updatedObject, update, updateContext, action);
        preparedUpdate = new PreparedUpdate(update, originalObject, updatedObjectWithAutoKeys, action, overrideOptions);

        // add authentication to context
        authenticator.authenticate(origin, preparedUpdate, updateContext);

        // attributegenerators rely on authentication info
        for (AttributeGenerator attributeGenerator : attributeGenerators) {
            updatedObjectWithAutoKeys = attributeGenerator.generateAttributes(originalObject, updatedObjectWithAutoKeys, update, updateContext);
        }

        // need to recalculate action after attributegenerators
        action = getAction(originalObject, updatedObjectWithAutoKeys, update, updateContext, keyword);
        updateContext.setAction(update, action);
        if (action == Action.NOOP) {
            updatedObjectWithAutoKeys = originalObject;
        }

        // re-generate preparedUpdate
        preparedUpdate = new PreparedUpdate(update, originalObject, updatedObjectWithAutoKeys, action, overrideOptions);

        // run business validation & pending updates hack
        final boolean businessRulesOk = updateObjectHandler.validateBusinessRules(preparedUpdate, updateContext);
        // TODO: [AH] pending updates is scattered across the code
        final boolean pendingAuthentication = UpdateStatus.PENDING_AUTHENTICATION.equals(updateContext.getStatus(preparedUpdate));

        if ((pendingAuthentication && !businessRulesOk) || (!pendingAuthentication && updateContext.hasErrors(update))) {
            throw new UpdateFailedException();
        }

        // defer setting prepared update so that on failure, we report back with the object without resolved auto keys
        // FIXME: [AH] per-attribute error messages generated up to this point will not get reported in ACK if they have been changed (by attributeGenerator or AUTO-key generator), as the report goes for the pre-auto-key-generated version of the object, in which the newly generated attributes are not present
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
    private RpslObject getOriginalObject(final Update update, final UpdateContext updateContext, final OverrideOptions overrideOptions) {
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
            final CIString key = update.getSubmittedObject().getKey();

            try {
                originalObject = rpslObjectDao.getByKey(update.getSubmittedObject().getType(), key);
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

    private RpslObject getUpdatedObject(final RpslObject originalObject, final Update update, final UpdateContext updateContext, final Keyword keyword) {
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
            final ObjectMessages messages = updateContext.getMessages(update);
            updatedObject = attributeSanitizer.sanitize(updatedObject, messages);
            ObjectTemplate.getTemplate(updatedObject.getType()).validateStructure(updatedObject, messages);
            ObjectTemplate.getTemplate(updatedObject.getType()).validateSyntax(updatedObject, messages, true);
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
