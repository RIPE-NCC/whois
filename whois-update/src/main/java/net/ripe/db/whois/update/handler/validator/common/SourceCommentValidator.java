package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;

@Component
public class SourceCommentValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final Pattern REMARK_PATTERN = Pattern.compile(".*#.*");

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final String source = update.getUpdatedObject().findAttribute(SOURCE).getValue();

        if (REMARK_PATTERN.matcher(source).matches()) {
            updateContext.addMessage(update, UpdateMessages.commentInSourceNotAllowed());
        }
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
