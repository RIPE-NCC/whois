package net.ripe.db.whois.update.handler;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.authentication.Subject;
import net.ripe.db.whois.update.authentication.strategy.RouteAutnumAuthentication;
import net.ripe.db.whois.update.authentication.strategy.RouteIpAddressAuthentication;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.mail.MailGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class UpdateNotifier {
    private final RpslObjectDao rpslObjectDao;
    private final ResponseFactory responseFactory;
    private final MailGateway mailGateway;

    @Autowired
    public UpdateNotifier(final RpslObjectDao rpslObjectDao, final ResponseFactory responseFactory, final MailGateway mailGateway) {
        this.rpslObjectDao = rpslObjectDao;
        this.responseFactory = responseFactory;
        this.mailGateway = mailGateway;
    }

    public void sendNotifications(final UpdateRequest updateRequest, final UpdateContext updateContext) {
        final Map<CIString, Notification> notifications = Maps.newHashMap();

        for (final Update update : updateRequest.getUpdates()) {
            final PreparedUpdate preparedUpdate = updateContext.getPreparedUpdate(update);
            if (preparedUpdate != null) {
                final OverrideOptions overrideOptions = preparedUpdate.getOverrideOptions();
                if (!overrideOptions.isNotifyOverride() || overrideOptions.isNotify()) {
                    addNotifications(notifications, preparedUpdate, updateContext);
                }
            }
        }

        for (final Notification notification : notifications.values()) {
            final ResponseMessage responseMessage = responseFactory.createNotification(updateContext, updateRequest.getOrigin(), notification);
            mailGateway.sendEmail(notification.getEmail(), responseMessage);
        }
    }

    private void addNotifications(final Map<CIString, Notification> notifications, final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject object = update.getReferenceObject();

        final UpdateStatus status = updateContext.getStatus(update);
        switch (status) {
            case SUCCESS:
                add(notifications, update, Notification.Type.SUCCESS, Collections.singletonList(object), AttributeType.NOTIFY);
                add(notifications, update, Notification.Type.SUCCESS, rpslObjectDao.getByKeys(ObjectType.MNTNER, object.getValuesForAttribute(AttributeType.MNT_BY)), AttributeType.MNT_NFY);
                add(notifications, update, Notification.Type.SUCCESS_REFERENCE, rpslObjectDao.getByKeys(ObjectType.ORGANISATION, update.getDifferences(AttributeType.ORG)), AttributeType.REF_NFY);
                add(notifications, update, Notification.Type.SUCCESS_REFERENCE, rpslObjectDao.getByKeys(ObjectType.IRT, update.getDifferences(AttributeType.MNT_IRT)), AttributeType.IRT_NFY);
                //TODO [AS] specialcase for completing pending update
                break;

            case FAILED_AUTHENTICATION:
                add(notifications, update, Notification.Type.FAILED_AUTHENTICATION, rpslObjectDao.getByKeys(ObjectType.MNTNER, object.getValuesForAttribute(AttributeType.MNT_BY)), AttributeType.UPD_TO);
                break;

            case PENDING_AUTHENTICATION:
                for (final RpslObject typeObject : findTypeObjects(update, updateContext)) {
                    add(notifications, update, Notification.Type.PENDING_UPDATE, rpslObjectDao.getByKeys(ObjectType.MNTNER, typeObject.getValuesForAttribute(AttributeType.MNT_BY)), AttributeType.UPD_TO);
                }
                break;

            default:
                break;
        }
    }

    private void add(final Map<CIString, Notification> notifications, final PreparedUpdate update, final Notification.Type type, final Iterable<RpslObject> objects, final AttributeType attributeType) {
        for (final RpslObject object : objects) {
            for (final CIString email : object.getValuesForAttribute(attributeType)) {
                Notification notification = notifications.get(email);
                if (notification == null) {
                    notification = new Notification(email.toString());
                    notifications.put(email, notification);
                }

                notification.add(type, update);
            }
        }
    }

    private Set<RpslObject> findTypeObjects(final PreparedUpdate update, final UpdateContext updateContext) {
        final RpslObject rpslObject = update.getUpdatedObject();
        CIString key = null;
        ObjectType soughtObjectType = null;
        final Subject subject = updateContext.getSubject(update);
        final Set<String> failedAuthentications = subject.getFailedAuthentications();
        final Set<RpslObject> typeObjects = Sets.newHashSet();

        for (final String failed : failedAuthentications) {
            if (failed.equals(RouteAutnumAuthentication.class.getSimpleName())) {
                key = rpslObject.getValueForAttribute(AttributeType.ORIGIN);
                soughtObjectType = ObjectType.AUT_NUM;

            } else if (failed.equals(RouteIpAddressAuthentication.class.getSimpleName())) {
                key = rpslObject.getValueForAttribute(AttributeType.ROUTE);
                soughtObjectType = ObjectType.INETNUM;
                if (rpslObject.getType() == ObjectType.ROUTE6) {
                    key = rpslObject.getValueForAttribute(AttributeType.ROUTE6);
                    soughtObjectType = ObjectType.INET6NUM;
                }
            }
            typeObjects.add(rpslObjectDao.getByKey(soughtObjectType, key));
        }
        return typeObjects;
    }
}
