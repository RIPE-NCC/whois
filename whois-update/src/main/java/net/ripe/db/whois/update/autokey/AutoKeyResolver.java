package net.ripe.db.whois.update.autokey;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.collect.CollectionHelper;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.*;
import net.ripe.db.whois.update.domain.*;
import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class AutoKeyResolver {
    private final Map<AttributeType, AutoKeyFactory> factoryByKeyMap;

    @Autowired
    public AutoKeyResolver(final AutoKeyFactory... autoKeyFactories) {
        factoryByKeyMap = Maps.newEnumMap(AttributeType.class);

        for (final AutoKeyFactory autoKeyFactory : autoKeyFactories) {
            final AttributeType attributeType = autoKeyFactory.getAttributeType();
            final AutoKeyFactory previousForKey = factoryByKeyMap.put(attributeType, autoKeyFactory);
            Validate.isTrue(previousForKey == null, "Multiple factories for: " + attributeType);
        }
    }

    public RpslObject resolveAutoKeys(final RpslObject object, final Update update, final UpdateContext updateContext, final Action action) {
        final Map<RpslAttribute, RpslAttribute> attributesToReplace = Maps.newHashMap();

        if (Action.CREATE.equals(action)) {
            claimOrGenerateAutoKeys(update, object, updateContext, attributesToReplace);
        }

        resolveAutoKeyReferences(update, object, updateContext, attributesToReplace);

        if (attributesToReplace.isEmpty()) {
            return object;
        }

        return new RpslObjectBuilder(object).replaceAttributes(attributesToReplace).get();
    }

    private void claimOrGenerateAutoKeys(final Update update, final RpslObject object, final UpdateContext updateContext, final Map<RpslAttribute, RpslAttribute> attributesToReplace) {
        final Set<AttributeType> keyAttributeTypes = ObjectTemplate.getTemplate(object.getType()).getKeyAttributes();
        if (keyAttributeTypes.size() != 1) {
            return;
        }

        final AttributeType keyAttributeType = keyAttributeTypes.iterator().next();
        final AutoKeyFactory autoKeyFactory = factoryByKeyMap.get(keyAttributeType);
        if (autoKeyFactory == null) {
            return;
        }

        final RpslAttribute keyAttribute = object.findAttribute(keyAttributeType);
        final CIString value = object.getValueForAttribute(keyAttributeType);

        if (autoKeyFactory.isApplicableFor(object)) {
            if (autoKeyFactory.isKeyPlaceHolder(value)) {
                final CIString keyPlaceHolder = autoKeyFactory.getKeyPlaceholder(value);

                final GeneratedKey existing = updateContext.getGeneratedKey(keyPlaceHolder);
                if (existing != null) {
                    updateContext.addMessage(update, keyAttribute, UpdateMessages.autokeyAlreadyDefined(keyPlaceHolder));
                } else {
                    final AutoKey autoKey = autoKeyFactory.generate(value.toString(), object);
                    updateContext.addGeneratedKey(update, keyPlaceHolder, new GeneratedKey(object, autoKey));
                    attributesToReplace.put(keyAttribute, new RpslAttribute(keyAttributeType, autoKey.toString()));
                }
            } else {
                try {
                    autoKeyFactory.claim(value.toString());
                } catch (ClaimException e) {
                    updateContext.addMessage(update, keyAttribute, e.getErrorMessage());
                }
            }
        }
    }

    private void resolveAutoKeyReferences(final Update update, final RpslObject object, final UpdateContext updateContext, final Map<RpslAttribute, RpslAttribute> attributesToReplace) {
        for (final RpslAttribute attribute : object.getAttributes()) {
            final AttributeType attributeType = attribute.getType();
            if (attributeType == null) {
                continue;
            }

            final AutoKeyFactory autoKeyFactory = getAutoKeyFactory(attributeType);
            if (autoKeyFactory == null) {
                continue;
            }

            for (final CIString value : attribute.getCleanValues()) {
                if (autoKeyFactory.isKeyPlaceHolder(value.toString())) {
                    final CIString keyPlaceHolder = autoKeyFactory.getKeyPlaceholder(value);
                    final GeneratedKey generatedKey = updateContext.getGeneratedKey(keyPlaceHolder);
                    if (generatedKey == null) {
                        updateContext.addMessage(update, attribute, UpdateMessages.referenceNotFound(keyPlaceHolder));
                    } else {
                        final Set<ObjectType> references = attributeType.getReferences(value);
                        final ObjectType objectType = generatedKey.getObject().getType();
                        if (!references.contains(objectType)) {
                            updateContext.addMessage(update, attribute, UpdateMessages.invalidReference(objectType, generatedKey.getAutoKey().toString()));
                        } else {
                            attributesToReplace.put(attribute, new RpslAttribute(attributeType, generatedKey.getAutoKey().toString()));
                        }
                    }

                    break;
                }
            }
        }
    }

    private AutoKeyFactory getAutoKeyFactory(final AttributeType attributeType) {
        final Set<AutoKeyFactory> autoKeyFactories = Sets.newHashSet();

        final Set<ObjectType> references = attributeType.getReferences();
        for (final ObjectType referenceObjectTypes : references) {
            final ObjectTemplate referenceObjectSpecs = ObjectTemplate.getTemplate(referenceObjectTypes);

            for (final AttributeType referenceObjectKeyAttributes : referenceObjectSpecs.getKeyAttributes()) {
                final AutoKeyFactory autoKeyFactory = factoryByKeyMap.get(referenceObjectKeyAttributes);
                if (autoKeyFactory != null) {
                    autoKeyFactories.add(autoKeyFactory);
                }
            }
        }

        return CollectionHelper.uniqueResult(autoKeyFactories);
    }
}
