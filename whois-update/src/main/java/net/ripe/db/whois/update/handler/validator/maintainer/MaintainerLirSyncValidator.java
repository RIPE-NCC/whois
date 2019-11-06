package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.dao.MaintainerSyncStatusDao;
import net.ripe.db.whois.common.domain.IpRanges;
import net.ripe.db.whois.common.ip.IpInterval;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Origin;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

//TODO: [MA] differentiate between different trusted addresses. Currently,  we treat an internal user on the VPN the same as Controlroom or the LIR Portal
@Component
public class MaintainerLirSyncValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.MNTNER);
    public static final String REST_API = "rest api";

    private final MaintainerSyncStatusDao maintainerSyncStatusDao;
    private final IpRanges ipranges;

    @Autowired
    public MaintainerLirSyncValidator(final MaintainerSyncStatusDao maintainerSyncStatusDao, final IpRanges ipranges) {
        this.maintainerSyncStatusDao = maintainerSyncStatusDao;
        this.ipranges = ipranges;
    }

    @Override
    public void validate(final PreparedUpdate update, final UpdateContext updateContext) {
        final Origin origin = updateContext.getOrigin(update);
        if(origin == null || StringUtils.isEmpty(origin.getFrom())) {
            updateContext.addMessage(update, UpdateMessages.originIsMissing());
        }

        if(!isChangingSSOAttribute(update)) {
            return;
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        if(!maintainerSyncStatusDao.isSyncEnabled(updatedObject.getKey())) {
            return;
        }

        //origin check to allow only rest api (validation fail for sync updates and mail updates)
        //trusted Ip check to allow change of sso attribute through ripe portal/controlroom via Whois-internal
        if(REST_API.equals(origin.getName()) && ipranges.isTrusted(IpInterval.parse(origin.getFrom()))) {
            return;
        }

        updateContext.addMessage(update, UpdateMessages.updatingRipeMaintainerSSOForbidden());
    }

    private boolean isChangingSSOAttribute(PreparedUpdate update) {
        return update.getDifferences(AttributeType.AUTH).stream().anyMatch(auth -> Pattern.compile("SSO\\s+(.*\\S)").matcher(auth.toString()).matches());
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
