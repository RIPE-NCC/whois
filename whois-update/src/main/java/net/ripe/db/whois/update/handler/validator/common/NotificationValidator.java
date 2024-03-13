package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Notification;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.handler.UpdateNotifier;
import net.ripe.db.whois.update.handler.validator.BusinessRuleValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class NotificationValidator implements BusinessRuleValidator {

    private static final ImmutableList<Action> ACTIONS = ImmutableList.of(Action.CREATE, Action.MODIFY, Action.DELETE);

    private static final ImmutableList<ObjectType> TYPES = ImmutableList.copyOf(ObjectType.values());
    private final EmailStatusDao emailStatusDao;

    private final UpdateNotifier updateNotifier;

    @Autowired
    public NotificationValidator(final EmailStatusDao emailStatusDao, final UpdateNotifier updateNotifier){
        this.emailStatusDao = emailStatusDao;
        this.updateNotifier = updateNotifier;
    }

    @Override
    public List<Message> performValidation(final PreparedUpdate update, final UpdateContext updateContext) {
        final PreparedUpdate preparedUpdate = updateContext.getPreparedUpdate(update);
        final Map<CIString, Notification> notifications = Maps.newHashMap();
        if (preparedUpdate != null && !updateNotifier.notificationsDisabledByOverride(preparedUpdate)) {
            updateNotifier.addNotificationsWithoutVersioning(notifications, preparedUpdate, updateContext, update.getReferenceObject());
        }

        final List<Message> messages = Lists.newArrayList();
        for (final Notification notification : notifications.values()) {
            final String emailStatus = emailStatusDao.getEmailStatus(notification.getEmail());
            messages.add(UpdateMessages.emailCanNotBeSent(notification.getEmail(), emailStatus));
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
