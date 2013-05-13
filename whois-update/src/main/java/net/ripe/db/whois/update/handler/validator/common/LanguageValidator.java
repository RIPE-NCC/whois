package net.ripe.db.whois.update.handler.validator.common;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class LanguageValidator implements BusinessRuleValidator {
    final private LanguageCodeRepository languageRepository;

    @Autowired
    public LanguageValidator(final LanguageCodeRepository repository) {
        this.languageRepository = repository;
    }

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.CREATE, Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.values());
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (!updatedObject.containsAttribute(AttributeType.LANGUAGE)) {
            return;
        }

        final Set<CIString> languageCodes = languageRepository.getLanguageCodes();
        for (final RpslAttribute attribute : updatedObject.findAttributes(AttributeType.LANGUAGE)) {
            if (!languageCodes.contains(attribute.getCleanValue())) {
                updateContext.addMessage(update, UpdateMessages.languageNotRecognised(attribute.getCleanValue()));
            }
        }
    }
}
