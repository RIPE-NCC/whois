package net.ripe.db.whois.update.handler;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.dao.RpslObjectUpdateInfo;
import net.ripe.db.whois.common.dao.VersionDao;
import net.ripe.db.whois.common.dao.VersionLookupResult;
import net.ripe.db.whois.common.dao.VersionVanishedException;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Notification;
import net.ripe.db.whois.update.domain.OverrideOptions;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.ResponseMessage;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.mail.MailGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.Collections;
import java.util.Map;

@Component
public class UpdateNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateNotifier.class);

    private final RpslObjectDao rpslObjectDao;
    private final ResponseFactory responseFactory;
    private final MailGateway mailGateway;
    private final VersionDao versionDao;
    private final Maintainers maintainers;

    @Autowired
    public UpdateNotifier(final RpslObjectDao rpslObjectDao,
                          final ResponseFactory responseFactory,
                          final MailGateway mailGateway,
                          final VersionDao versionDao,
                          final Maintainers maintainers) {
        this.rpslObjectDao = rpslObjectDao;
        this.responseFactory = responseFactory;
        this.mailGateway = mailGateway;
        this.versionDao = versionDao;
        this.maintainers = maintainers;
    }

    public void sendNotifications(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        if (updateContext.isDryRun()) {
            return;
        }

        final Map<CIString, Notification> notifications = Maps.newHashMap();

        for (final Update update : updateRequest.getUpdates()) {
            final PreparedUpdate preparedUpdate = updateContext.getPreparedUpdate(update);
            if (preparedUpdate != null && !notificationsDisabledByOverride(preparedUpdate)) {
                addNotifications(notifications, preparedUpdate, updateContext);
            }
        }

        for (final Notification notification : notifications.values()) {
            final ResponseMessage responseMessage = responseFactory.createNotification(updateContext, updateRequest.getOrigin(), notification);
            try {
                new InternetAddress(notification.getEmail(), true);
                mailGateway.sendEmail(notification.getEmail(), responseMessage);
            } catch (final AddressException e) {
                LOGGER.info("Failed to send notification to '{}' because it's an invalid email address", notification.getEmail());
            }
        }
    }

    private void addVersionId(PreparedUpdate preparedUpdate, UpdateContext context) {
        if (preparedUpdate.getAction() != Action.MODIFY || context.isDryRun()) {
            return;
        }

        VersionLookupResult res = versionDao.findByKey(preparedUpdate.getType(), preparedUpdate.getKey());
        if (res == null) {
            LOGGER.info("Failed to find version lookup result on update for {}", preparedUpdate.toString());
        } else {
            try {
                final RpslObjectUpdateInfo updateInfo = context.getUpdateInfo(preparedUpdate);
                final int versionId = res.getVersionIdFor(updateInfo) - 1;   // -1 as we want the previous version
                context.versionId(preparedUpdate, versionId);
            } catch (VersionVanishedException e) {  // update + delete in the same update message
            }
        }
    }

    private boolean notificationsDisabledByOverride(PreparedUpdate preparedUpdate) {
        final OverrideOptions overrideOptions = preparedUpdate.getOverrideOptions();
        return overrideOptions.isNotifyOverride() && !overrideOptions.isNotify();
    }

    private void addNotifications(final Map<CIString, Notification> notifications, final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject object = update.getReferenceObject();

            switch (updateContext.getStatus(update)) {
                case SUCCESS:
                    if (updateContext.getAction(update) != Action.NOOP) {
                        addVersionId(update, updateContext);
                        add(notifications, updateContext, update, Notification.Type.SUCCESS, Collections.singletonList(object), AttributeType.NOTIFY);
                        add(notifications, updateContext, update, Notification.Type.SUCCESS, rpslObjectDao.getByKeys(ObjectType.MNTNER, object.getValuesForAttribute(AttributeType.MNT_BY)), AttributeType.MNT_NFY);
                        add(notifications, updateContext, update, Notification.Type.SUCCESS_REFERENCE, rpslObjectDao.getByKeys(ObjectType.ORGANISATION, update.getDifferences(AttributeType.ORG)), AttributeType.REF_NFY);
                        add(notifications, updateContext, update, Notification.Type.SUCCESS_REFERENCE, rpslObjectDao.getByKeys(ObjectType.IRT, update.getDifferences(AttributeType.MNT_IRT)), AttributeType.IRT_NFY);
                    }
                    break;

                case FAILED_AUTHENTICATION:
                    add(notifications, updateContext, update, Notification.Type.FAILED_AUTHENTICATION, rpslObjectDao.getByKeys(ObjectType.MNTNER, object.getValuesForAttribute(AttributeType.MNT_BY)), AttributeType.UPD_TO);
                    break;

                case PENDING_AUTHENTICATION:
                    final Iterable<RpslObject> pendingAuthenticationCandidates = Iterables.filter(updateContext.getSubject(update).getPendingAuthenticationCandidates(), new Predicate<RpslObject>() {
                        @Override
                        public boolean apply(final RpslObject input) {
                            return !maintainers.getRsMaintainers().contains(input.getKey());
                        }
                    });
                    add(notifications, updateContext, update, Notification.Type.PENDING_UPDATE, pendingAuthenticationCandidates, AttributeType.UPD_TO);
                    break;

                default:
                    break;

        }
    }

    private void add(final Map<CIString, Notification> notifications, UpdateContext updateContext, final PreparedUpdate update, final Notification.Type type, final Iterable<RpslObject> objects, final AttributeType attributeType) {
        for (final RpslObject object : objects) {
            for (final CIString email : object.getValuesForAttribute(attributeType)) {
                Notification notification = notifications.get(email);
                if (notification == null) {
                    notification = new Notification(email.toString());
                    notifications.put(email, notification);
                }

                notification.add(type, update, updateContext);
            }
        }
    }
}
