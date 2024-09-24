package net.ripe.db.whois.update.handler.validator.common;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.dao.EmailStatusDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.mail.EmailStatusType;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        if (preparedUpdate == null || updateNotifier.notificationsDisabledByOverride(preparedUpdate)) {
            return Collections.EMPTY_LIST;
        }

        final Map<CIString, Notification> notifications = Maps.newHashMap();

        updateNotifier.addNotificationForNtfyAttrs(notifications, preparedUpdate, updateContext, update.getReferenceObject());

        final Set<String> emails = notifications.values().stream().map(Notification::getEmail).collect(Collectors.toSet());

        final Map<String, EmailStatusType> emailStatus = emailStatusDao.getEmailStatusMap(emails);
        final List<Message> messages = Lists.newArrayList();

        emailStatus.forEach( (email, status) ->  messages.add(UpdateMessages.emailCanNotBeSent(email, status)));

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
