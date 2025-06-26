package net.ripe.db.whois.update.handler;

import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.UpdateLockDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.iptree.IpTreeUpdater;
import net.ripe.db.whois.common.rpsl.AttributeSanitizer;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.ObjectTemplate;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectFilter;
import net.ripe.db.whois.update.authentication.Authenticator;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Keyword;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.OverrideOptions;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.generator.AttributeGenerator;
import net.ripe.db.whois.update.handler.transform.Transformer;
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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@Component
public class SingleUpdateHandler {
    private final AttributeSanitizer attributeSanitizer;
    private final List<AttributeGenerator> attributeGenerators;
    private final Transformer[] transformers;
    private final RpslObjectDao rpslObjectDao;
    private final UpdateLockDao updateLockDao;
    private final Authenticator authenticator;
    private final UpdateObjectHandler updateObjectHandler;
    private final IpTreeUpdater ipTreeUpdater;
    private final SsoTranslator ssoTranslator;

    // TODO: [ES] make these fields final and assign in the constructor

    @Value("#{T(net.ripe.db.whois.common.domain.CIString).ciString('${whois.source}')}")
    private CIString source;

    @Value("#{T(net.ripe.db.whois.common.domain.CIString).ciString('${whois.nonauth.source}')}")
    private CIString nonAuthSource;

    @Value("${max.references:0}")
    private int maxReferences;

    @Autowired
    public SingleUpdateHandler(final List<AttributeGenerator> attributeGenerators,
                               final Transformer[] transformers,
                               final AttributeSanitizer attributeSanitizer,
                               final UpdateLockDao updateLockDao,
                               final Authenticator authenticator,
                               final UpdateObjectHandler updateObjectHandler,
                               final RpslObjectDao rpslObjectDao,
                               final IpTreeUpdater ipTreeUpdater,
                               final SsoTranslator ssoTranslator) {
        this.attributeGenerators = attributeGenerators;
        // sort AttributeGenerators so they are executed in a predictable order
        this.attributeGenerators.sort((lhs, rhs) -> lhs.getClass().getName().compareToIgnoreCase(rhs.getClass().getName()));
        this.transformers = transformers;
        this.attributeSanitizer = attributeSanitizer;
        this.rpslObjectDao = rpslObjectDao;
        this.updateLockDao = updateLockDao;
        this.authenticator = authenticator;
        this.updateObjectHandler = updateObjectHandler;
        this.ipTreeUpdater = ipTreeUpdater;
        this.ssoTranslator = ssoTranslator;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public void handle(final Origin origin, final Keyword keyword, final Update update, final UpdateContext updateContext) {
        updateLockDao.setUpdateLock();
        ipTreeUpdater.updateTransactional();

        if (updateContext.isDryRun()) {
            updateContext.addMessage(update, UpdateMessages.dryRunNotice());
        }

        updateContext.setOrigin(update, origin);

        final OverrideOptions overrideOptions = OverrideOptions.parse(update, updateContext);
        final RpslObject originalObject = getOriginalObject(update, updateContext, overrideOptions);
        RpslObject updatedObject = getUpdatedObject(update, updateContext, keyword);

        Action action = getAction(originalObject, updatedObject, update, updateContext, keyword, overrideOptions);
        updateContext.setAction(update, action);

        if (action == Action.NOOP) {
            updatedObject = originalObject;
        }

        if (action == Action.DELETE && originalObject == null) {
            updateContext.addMessage(update, UpdateMessages.objectNotFound(update.getSubmittedObject().getFormattedKey()));
        }

        if(updateContext.hasDNSCheckFailed(update)) {
            throw new DnsCheckFailedException();
        }

        // up to this point, updatedObject could have structural+syntax errors (unknown attributes, etc...), bail out if so
        PreparedUpdate preparedUpdate = new PreparedUpdate(update, originalObject, updatedObject, action, overrideOptions);
        updateContext.setPreparedUpdate(preparedUpdate);
        if (updateContext.hasErrors(update)) {
            throw new UpdateFailedException();
        }

        // apply object transformation
        RpslObject updatedObjectWithAutoKeys = updatedObject;
        for (Transformer transformer : transformers) {
            updatedObjectWithAutoKeys = transformer.transform(updatedObjectWithAutoKeys, update, updateContext, action);
        }

        preparedUpdate = new PreparedUpdate(update, originalObject, updatedObjectWithAutoKeys, action, overrideOptions);

        // add authentication to context
        authenticator.authenticate(origin, preparedUpdate, updateContext);

        // attributegenerators rely on authentication info
        for (AttributeGenerator attributeGenerator : attributeGenerators) {
            updatedObjectWithAutoKeys = attributeGenerator.generateAttributes(originalObject, updatedObjectWithAutoKeys, update, updateContext);
        }

        // need to recalculate action after attributegenerators
        action = getAction(originalObject, updatedObjectWithAutoKeys, update, updateContext, keyword, overrideOptions);
        updateContext.setAction(update, action);
        if (action == Action.NOOP) {
            updatedObjectWithAutoKeys = originalObject;
        }

        // re-generate preparedUpdate
        preparedUpdate = new PreparedUpdate(update, originalObject, updatedObjectWithAutoKeys, action, overrideOptions);

        final boolean businessRulesOk = updateObjectHandler.validateBusinessRules(preparedUpdate, updateContext);
        if ((!businessRulesOk) || (updateContext.hasErrors(update))) {
            throw new UpdateFailedException();
        }

        // defer setting prepared update so that on failure, we report back with the object without resolved auto keys
        // FIXME: [AH] per-attribute error messages generated up to this point will not get reported in ACK
        // if they have been changed (by attributeGenerator or AUTO-key generator), as the report goes for the
        // pre-auto-key-generated version of the object, in which the newly generated attributes are not present
        updateContext.setPreparedUpdate(preparedUpdate);

        if (updateContext.isDryRun() && !updateContext.isBatchUpdate()) {
            throw new UpdateAbortedException();
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
            final CIString key = attributeSanitizer.sanitizeKey(update.getSubmittedObject());

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

    @Nonnull
    private RpslObject getUpdatedObject(final Update update, final UpdateContext updateContext, final Keyword keyword) {
        RpslObject updatedObject = update.getSubmittedObject();

        if (RpslObjectFilter.isFiltered(updatedObject)) {
            updateContext.addMessage(update, UpdateMessages.filteredNotAllowed());
        }

        if (maxReferences > 0 && countReferences(updatedObject) > maxReferences) {
            updateContext.addMessage(update, UpdateMessages.tooManyReferences());
        }

        if (Operation.DELETE.equals(update.getOperation())) {
            if (Keyword.NEW.equals(keyword)) {
                updateContext.addMessage(update, UpdateMessages.operationNotAllowedForKeyword(keyword, update.getOperation()));
            }

            if (update.getDeleteReasons().size() > 1) {
                updateContext.addMessage(update, UpdateMessages.multipleReasonsSpecified(update.getOperation()));
            }

            updatedObject = attributeSanitizer.sanitize(updatedObject, updateContext.getMessages(update));
        } else {
            final ObjectMessages messages = updateContext.getMessages(update);
            updatedObject = attributeSanitizer.sanitize(updatedObject, messages);

            ObjectTemplate.getTemplate(updatedObject.getType()).validateStructure(updatedObject, messages);
            ObjectTemplate.getTemplate(updatedObject.getType()).validateSyntax(updatedObject, messages, true);
        }

        return updatedObject;
    }

    private int countReferences(final RpslObject updatedObject) {
        int references = 0;
        for (RpslAttribute attribute : updatedObject.getAttributes()) {
            if ((attribute.getType() != null) && attribute.getType().isReference()) {
                references++;
            }
        }
        return references;
    }

    private Action getAction(@Nullable final RpslObject originalObject,
                             final RpslObject updatedObject,
                             final Update update,
                             final UpdateContext updateContext,
                             final Keyword keyword,
                             final OverrideOptions overrideOptions) {
        if (Operation.DELETE.equals(update.getOperation())) {
            return Action.DELETE;
        }

        if (Keyword.NEW.equals(keyword) || originalObject == null) {
            return Action.CREATE;
        }

        if (RpslObjectFilter.ignoreGeneratedAttributesEqual(originalObject, updatedObject)
                && !overrideOptions.isUpdateOnNoop()
                && !updateContext.hasErrors(update)) {
            return Action.NOOP;
        }

        return Action.MODIFY;
    }

}
