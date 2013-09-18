package net.ripe.db.whois.scheduler.task.update;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.ripe.db.whois.api.whois.InternalJob;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.PendingUpdate;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.scheduler.DailyScheduledTask;
import net.ripe.db.whois.update.dao.PendingUpdateDao;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.response.ResponseFactory;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.log.UpdateLog;
import net.ripe.db.whois.update.mail.MailGateway;
import org.joda.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class PendingUpdatesCleanup implements DailyScheduledTask {
    private static final Logger LOGGER = LoggerFactory.getLogger(PendingUpdatesCleanup.class);

    private static final String ID = "PendingUpdatesCleanup";
    private static final int CLEANUP_THRESHOLD_DAYS = 7;

    private final PendingUpdateDao pendingUpdateDao;
    private final RpslObjectDao rpslObjectDao;
    private final DateTimeProvider dateTimeProvider;
    private final ResponseFactory responseFactory;
    private final MailGateway mailGateway;
    private final UpdateLog updateLog;
    private final LoggerContext loggerContext;

    @Autowired
    public PendingUpdatesCleanup(final PendingUpdateDao pendingUpdateDao,
                                 final RpslObjectDao rpslObjectDao,
                                 final DateTimeProvider dateTimeProvider,
                                 final ResponseFactory responseFactory,
                                 final MailGateway mailGateway,
                                 final UpdateLog updateLog,
                                 final LoggerContext loggerContext) {
        this.pendingUpdateDao = pendingUpdateDao;
        this.rpslObjectDao = rpslObjectDao;
        this.dateTimeProvider = dateTimeProvider;
        this.responseFactory = responseFactory;
        this.mailGateway = mailGateway;
        this.updateLog = updateLog;
        this.loggerContext = loggerContext;
    }

    @Override
    public void run() {
        final LocalDateTime before = dateTimeProvider.getCurrentDateTime().minusDays(CLEANUP_THRESHOLD_DAYS);
        LOGGER.debug("Removing pending updates before {}", before);

        for (PendingUpdate pendingUpdate : pendingUpdateDao.findBeforeDate(before)) {
            try {
                removeAndNotify(pendingUpdate);
            } catch (RuntimeException e) {
                LOGGER.warn("Pending update threw exception", e);
            }
        }
    }

    @Transactional
    private void removeAndNotify(PendingUpdate pendingUpdate) {
        pendingUpdateDao.remove(pendingUpdate);
        logUpdateAndSendNotifications(pendingUpdate);
    }

    private void logUpdateAndSendNotifications(final PendingUpdate pendingUpdate) {
        loggerContext.init(String.format("%s_%d", ID, pendingUpdate.getId()));
        try {
            final Origin origin = new InternalJob(ID);
            final RpslObject rpslObject = pendingUpdate.getObject();
            final String updateMessage = rpslObject.toString();
            final Update update = new Update(new Paragraph(updateMessage), Operation.UNSPECIFIED, Lists.<String>newArrayList(), rpslObject);
            final UpdateContext updateContext = new UpdateContext(loggerContext);
            updateContext.setAction(update, Action.CREATE);
            updateContext.status(update, UpdateStatus.FAILED_AUTHENTICATION);
            updateContext.setPreparedUpdate(new PreparedUpdate(update, rpslObject, rpslObject, Action.CREATE));
            final UpdateRequest updateRequest = new UpdateRequest(origin, Keyword.NONE, updateMessage, ImmutableList.of(update));

            updateLog.logUpdateResult(updateRequest, updateContext, update, (new Stopwatch()).start());

            final Set<CIString> recipients = Sets.newHashSet();
            for (RpslObject mntner : rpslObjectDao.getByKeys(ObjectType.MNTNER, rpslObject.getValuesForAttribute(AttributeType.MNT_BY))) {
                recipients.addAll(mntner.getValuesForAttribute(AttributeType.UPD_TO));
            }

            final ResponseMessage responseMessage = responseFactory.createPendingUpdateTimeout(updateContext, origin, rpslObject, CLEANUP_THRESHOLD_DAYS);
            for (CIString recipient : recipients) {
                mailGateway.sendEmail(recipient.toString(), responseMessage);
            }
        } finally {
            loggerContext.remove();
        }
    }
}
