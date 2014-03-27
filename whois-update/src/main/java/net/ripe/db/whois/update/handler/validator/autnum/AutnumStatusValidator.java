package net.ripe.db.whois.update.handler.validator.autnum;

import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;

import java.util.List;

public class AutnumStatusValidator implements BusinessRuleValidator {

    @Override
    public List<Action> getActions() {
        return Lists.newArrayList(Action.MODIFY);
    }

    @Override
    public List<ObjectType> getTypes() {
        return Lists.newArrayList(ObjectType.AUT_NUM);
    }

    @Override
    public void validate(PreparedUpdate update, UpdateContext updateContext) {
        // TODO: autnum status validator
    }
}
