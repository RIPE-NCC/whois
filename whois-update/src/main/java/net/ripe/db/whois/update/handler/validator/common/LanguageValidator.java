package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dao.LanguageCodeRepository;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import net.ripe.db.whois.update.handler.validator.CustomValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class LanguageValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    final private LanguageCodeRepository languageRepository;

    @Autowired
    public LanguageValidator(final LanguageCodeRepository repository) {
        this.languageRepository = repository;
    }

    @Override
    public List<CustomValidationMessage> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (!updatedObject.containsAttribute(AttributeType.LANGUAGE)) {
            return Collections.emptyList();
        }

        final Set<CIString> languageCodes = languageRepository.getLanguageCodes();
        final List<CustomValidationMessage> customValidationMessages = Lists.newArrayList();
        for (final RpslAttribute attribute : updatedObject.findAttributes(AttributeType.LANGUAGE)) {
            if (!languageCodes.contains(attribute.getCleanValue())) {
                customValidationMessages.add(new CustomValidationMessage(UpdateMessages.languageNotRecognised(attribute.getCleanValue())));
            }
        }

        return customValidationMessages;
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }
}
