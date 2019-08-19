package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.MaintainerSyncStatusDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Principal;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;


@Component
public class MaintainerLirSyncValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.MNTNER);

    private final MaintainerSyncStatusDao maintainerSyncStatusDao;

    @Autowired
    public MaintainerLirSyncValidator(final MaintainerSyncStatusDao maintainerSyncStatusDao) {
        this.maintainerSyncStatusDao = maintainerSyncStatusDao;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        if (updateContext.getSubject(update).hasPrincipal(Principal.OVERRIDE_MAINTAINER)) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();

        if(!maintainerSyncStatusDao.isSyncEnabled(updatedObject.getKey())) {
            return;
        }

        if(update.getDifferences(AttributeType.AUTH).stream().anyMatch(auth -> Pattern.compile("SSO\\s+(.*\\S)").matcher(auth.toString()).matches())) {
            updateContext.addMessage(update, UpdateMessages.updatingRipeMaintainerSSOForbidden());
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
