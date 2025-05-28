package net.ripe.db.whois.update.handler.validator.maintainer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.MaintainerSyncStatusDao;
import net.ripe.db.whois.common.domain.CIString;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

//TODO: [MA] differentiate between different trusted addresses. Currently,  we treat an internal user on the VPN the same as Controlroom or the LIR Portal
@Component
public class MaintainerLirSyncValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.MODIFY);
    private static final ImmutableList<ObjectType> TYPES = ImmutableList.of(ObjectType.MNTNER);

    private static final Pattern SSO_AUTH_PATTERN = Pattern.compile("SSO\\s+(.*\\S)");
    private static final String REST_API_ORIGIN = "rest api";

    private final MaintainerSyncStatusDao maintainerSyncStatusDao;
    private final IpRanges ipranges;

    @Autowired
    public MaintainerLirSyncValidator(final MaintainerSyncStatusDao maintainerSyncStatusDao, final IpRanges ipranges) {
        this.maintainerSyncStatusDao = maintainerSyncStatusDao;
        this.ipranges = ipranges;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final Origin origin = updateContext.getOrigin(update);
        final List<Message> messages = Lists.newArrayList();

        if(origin == null || StringUtils.isEmpty(origin.getFrom())) {
            messages.add(UpdateMessages.originIsMissing());
        }

        if(!isChangingSsoAuthAttribute(update)) {
            return messages;
        }

        final RpslObject updatedObject = update.getUpdatedObject();
        if(!maintainerSyncStatusDao.isSyncEnabled(updatedObject.getKey())) {
            return messages;
        }

        //origin check to allow only rest api (validation fail for sync updates and mail updates)
        //trusted Ip check to allow change of sso attribute through ripe portal/controlroom via Whois-internal
        if(REST_API_ORIGIN.equals(origin.getName()) && ipranges.isTrusted(IpInterval.parse(origin.getFrom()))) {
            return messages;
        }

        messages.add(UpdateMessages.updatingRipeMaintainerSSOForbidden());

        return messages;
    }

    private boolean isChangingSsoAuthAttribute(final PreparedUpdate update) {
        return update.getDifferences(AttributeType.AUTH).stream().anyMatch(this::isSsoAuthAttribute);
    }

    private boolean isSsoAuthAttribute(final CIString value) {
        return SSO_AUTH_PATTERN.matcher(value).matches();
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
