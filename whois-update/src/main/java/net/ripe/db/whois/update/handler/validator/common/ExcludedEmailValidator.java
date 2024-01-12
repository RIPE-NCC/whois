package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeParser;
import net.ripe.db.whois.common.rpsl.AttributeSyntax;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.AttributeParseException;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Do not allow any specified email addresses to be used in any attribute with email syntax.
 */
@Component
public class ExcludedEmailValidator implements BusinessRuleValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExcludedEmailValidator.class);

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final List<AttributeType> EMAIL_ATTRIBUTES = Stream.of(AttributeType.values())
        .filter(attributeType -> attributeType.getSyntax() == AttributeSyntax.EMAIL_SYNTAX)
        .collect(Collectors.toList());

    private static final AttributeParser.EmailParser EMAIL_PARSER = new AttributeParser.EmailParser();

    private final List<CIString> excludedEmailAddresses;

    @Autowired
    public ExcludedEmailValidator(
         @Value("#{'${email.excluded:}'.split(',')}") final List<String> excludedEmailAddresses) {
         this.excludedEmailAddresses = toCIStrings(excludedEmailAddresses);
     }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Subject subject = updateContext.getSubject(update);
        if (subject.hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        final List<Message> messages = Lists.newArrayList();
        final RpslObject updatedObject = update.getUpdatedObject();
        for (final RpslAttribute attribute : updatedObject.getAttributes()) {
            if (EMAIL_ATTRIBUTES.contains(attribute.getType())) {
                try {
                    final CIString address = CIString.ciString(getAddress(attribute.getValue()));
                    if (excludedEmailAddresses.contains(address)) {
                        messages.add(UpdateMessages.emailAddressCannotBeUsed(attribute, address));
                    }
                } catch (IllegalArgumentException e) {
                    // skip validation if the attribute value cannot be parsed
                    LOGGER.debug("Skipped {} attribute in {} due to: {}", attribute.getType().getName(), updatedObject.getKey(), e.getMessage());
                }
            }
        }

        return messages;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }
    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    // helper methods

    private static String getAddress(final String value) {
        try {
            return EMAIL_PARSER.parse(value).getAddress();
        } catch (AttributeParseException e) {
            throw new IllegalArgumentException(e.getMessage() + ": " + value.trim());
        }
    }

    private static List<CIString> toCIStrings(final List<String> values) {
        return values.stream().map(value -> CIString.ciString(value.trim())).collect(Collectors.toList());
    }

}
