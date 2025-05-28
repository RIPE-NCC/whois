package net.ripe.db.whois.update.handler.validator.organisation;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Disallow inconsistent formatting in the Organisation org-name attribute value.
 * If found, then the update fails with an error.
 *
 * Do NOT allow this check to be skipped by override or an RS maintainer,
 * the org-name must *always* be consistent in the database.
 *
 */
@Component
public class OrgNameFormatValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.ORGANISATION);

    private static final Pattern COMMENT_PATTERN = Pattern.compile("(?m)[#].*");
    private static final Pattern INCONSISTENT_FORMATTING_PATTERN = Pattern.compile("(?m)\\s{2,}|\t|\n");

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();

        final RpslAttribute orgNameAttribute;
        try {
            orgNameAttribute = updatedObject.findAttribute(AttributeType.ORG_NAME);
        } catch (IllegalArgumentException e) {
            // ignore no org-name (or multiple) found
            return Collections.emptyList();
        }

        final String orgNameValue = stripComments(orgNameAttribute.getValue()).trim();

        if (isMultiline(orgNameValue) || containsInconsistentFormatting(orgNameValue)) {
           return Arrays.asList(UpdateMessages.inconsistentOrgNameFormatting(orgNameAttribute));
        }

        return Collections.emptyList();
    }

     // does the attribute value run over multiple lines
    private boolean isMultiline(final String value) {
        return value.contains("\n");
    }

    // strip comment(s) from value
    private String stripComments(final String value) {
        return COMMENT_PATTERN.matcher(value).replaceAll("");
    }

    // does the attribute value contain inconsistent formatting, ignoring comments and leading or trailing spaces.
    private boolean containsInconsistentFormatting(final String value) {
         return INCONSISTENT_FORMATTING_PATTERN.matcher(value).find();
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
