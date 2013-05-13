package net.ripe.db.whois.update.handler.validator;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

import java.util.List;

public interface BusinessRuleValidator {
    List<Action> getActions();

    List<ObjectType> getTypes();

    void validate(PreparedUpdate update, UpdateContext updateContext);
}
