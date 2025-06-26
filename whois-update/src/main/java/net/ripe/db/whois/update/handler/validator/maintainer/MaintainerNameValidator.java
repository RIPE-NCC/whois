package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static net.ripe.db.whois.common.domain.CIString.ciSet;

@Component
public class MaintainerNameValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.MNTNER);
    private static final  String MNT_NAME_SUFFIX = "-MNT";

    static final Set<CIString> INVALID_NAMES = ciSet(
            "ASNEW-MNT", "AUTO-1", "BLUELIGHT-MNT", "EXAMPLE-MNT", "GOODY2SHOES-MNT",
            "MNT-1", "MNT-2", "MNT-3", "MNT-4", "MNT-5", "MNT-6", "MNT-7", "MNT-8", "MNT-9",
            "MNT-BY", "MNT-DOMAIN", "MNT-DOMAINS", "MNT-LOWER", "MNT-ROUTE", "MNT-ROUTES",
            "MNTR-1", "MNTR-2", "MNTR-3", "MNTR-4", "MNTR-5", "MNTR-6", "MNTR-7", "MNTR-8",
            "MNTR-9", "NEW-MNT", "RIPE-NCC-NONE-MNT", "SANTA-MNT"
    );

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject updatedObject = update.getUpdatedObject();
        List<Message> messages = Lists.newArrayList();

        if (Action.CREATE.equals(update.getAction()) && !updatedObject.getKey().endsWith(MNT_NAME_SUFFIX)) {
            messages.add(UpdateMessages.invalidMaintainerName(updatedObject.getAttributes().get(0)));
        }
        if (INVALID_NAMES.contains(updatedObject.getKey())) {
           messages.add(UpdateMessages.reservedNameUsed(updatedObject.getAttributes().get(0)));
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
