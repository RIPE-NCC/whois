package net.ripe.db.whois.update.handler.validator;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;

public interface BusinessRuleValidator {
    ImmutableList<Action> getActions();

    ImmutableList<ObjectType> getTypes();

    void validate(PreparedUpdate update, UpdateContext updateContext);

}
