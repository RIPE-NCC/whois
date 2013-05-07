package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.dao.CountryCodeRepository;
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
public class CountryValidator implements BusinessRuleValidator {
    final private CountryCodeRepository countryCodeRepository;

    @Autowired
    public CountryValidator(final CountryCodeRepository countryCodeRepository) {
        this.countryCodeRepository = countryCodeRepository;
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
    public void validate(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (!updatedObject.containsAttribute(AttributeType.COUNTRY)) {
            return;
        }

        final Set<CIString> countryCodes = countryCodeRepository.getCountryCodes();
        for (final RpslAttribute attribute : updatedObject.findAttributes(AttributeType.COUNTRY)) {
            if (!countryCodes.contains(attribute.getCleanValue())) {
                updateContext.addMessage(update, attribute, UpdateMessages.countryNotRecognised(attribute.getCleanValue()));
            }
        }
    }
}
