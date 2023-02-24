package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class CountryValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    final private CountryCodeRepository countryCodeRepository;

    @Autowired
    public CountryValidator(final CountryCodeRepository countryCodeRepository) {
        this.countryCodeRepository = countryCodeRepository;
    }

    @Override
    public List<Message> performValidation(PreparedUpdate update, UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        if (!updatedObject.containsAttribute(AttributeType.COUNTRY)) {
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();

        final Set<CIString> countryCodes = countryCodeRepository.getCountryCodes();
        for (final RpslAttribute attribute : updatedObject.findAttributes(AttributeType.COUNTRY)) {
            if (!countryCodes.contains(attribute.getCleanValue())) {
                messages.add(UpdateMessages.countryNotRecognised(attribute));
            }
        }

        return messages;
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
