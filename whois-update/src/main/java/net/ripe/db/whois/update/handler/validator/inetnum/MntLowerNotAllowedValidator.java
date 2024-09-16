package net.ripe.db.whois.update.handler.validator.inetnum;

import com.google.common.collect.ImmutableList;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.attrs.Inet6numStatus;
import net.ripe.db.whois.common.rpsl.attrs.InetnumStatus;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateMessages;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

import static net.ripe.db.whois.update.handler.validator.inetnum.InetStatusHelper.getStatus;

@Component
public class MntLowerNotAllowedValidator extends MntLowerValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY);
    private static final List<InetnumStatus> VALIDATED_INETNUM_STATUSES = ImmutableList.of(InetnumStatus.ASSIGNED_PI, InetnumStatus.ASSIGNED_ANYCAST);
    private static final List<Inet6numStatus> VALIDATED_INET6NUM_STATUSES = ImmutableList.of(Inet6numStatus.ASSIGNED_PI, Inet6numStatus.ASSIGNED, Inet6numStatus.ASSIGNED_ANYCAST);

    @Override
    protected List<Message> addErrorMessage(final PreparedUpdate update) {
        final RpslObject rpslObject = update.getUpdatedObject();
        if (rpslObject != null && rpslObject.containsAttribute(AttributeType.MNT_LOWER)){
            return List.of(UpdateMessages.attributeNotAllowedWithStatus(AttributeType.MNT_LOWER, rpslObject.getValueForAttribute(AttributeType.STATUS)));
        }
        return Collections.emptyList();
    }

    @Override
    protected boolean isInvalidInetnumStatus(final PreparedUpdate update){
        return !VALIDATED_INETNUM_STATUSES.contains(getStatus(update));
    }

    @Override
    protected boolean isInvalidInet6numStatus(final PreparedUpdate update){
        return !VALIDATED_INET6NUM_STATUSES.contains(getStatus(update));
    }

    @Override
    public ImmutableList<Action> getActions() {
        return ACTIONS;
    }

}
