package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.rpsl.AttributeType.SOURCE;

@Component
public class SourceCommentValidator implements BusinessRuleValidator {
    private static final Pattern REMARK_PATTERN = Pattern.compile(".*#.*");

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
        final String source = update.getUpdatedObject().findAttribute(SOURCE).getValue();

        if (REMARK_PATTERN.matcher(source).matches()) {
            updateContext.addMessage(update, UpdateMessages.commentInSourceNotAllowed());
        }
    }
}
