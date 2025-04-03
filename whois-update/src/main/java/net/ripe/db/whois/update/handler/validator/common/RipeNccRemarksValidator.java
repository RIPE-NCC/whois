package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.rpsl.AttributeType.REMARKS;

@Component
public class RipeNccRemarksValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());

    private static final String RIPE_NCC_REMARKS = "Remark added by the RIPE NCC";

    @Override
    public List<Message> performValidation(PreparedUpdate update, UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(Principal.RS_MAINTAINER)) {
            return Collections.emptyList();
        }

        final Set<CIString> remarksDiff = update.getDifferences(REMARKS);
        if(remarksDiff.isEmpty()) {
            return Collections.emptyList();
        }

        if(remarksDiff.stream().anyMatch( remark -> remark.startsWith(RIPE_NCC_REMARKS))) {
            return List.of(UpdateMessages.cantAddorRemoveRipeNccRemarks());
        }

        return Collections.emptyList();
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

    @Override
    public ImmutableList<ObjectType> getTypes() {
        return TYPES;
    }

    @Override
    public boolean isSkipForOverride() {
        return true;
    }
}
