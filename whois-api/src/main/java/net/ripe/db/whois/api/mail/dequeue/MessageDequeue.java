package net.ripe.db.whois.api.mail.dequeue;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.api.mail.EmailMessageInfo;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import net.ripe.db.whois.api.mail.exception.MailParsingException;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.MaintenanceMode;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.domain.DequeueStatus;
import net.ripe.db.whois.common.credentials.PasswordCredential;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.domain.UpdateMessages;
import net.ripe.db.whois.update.domain.UpdateRequest;
import net.ripe.db.whois.update.domain.UpdateResponse;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.mail.MailMessageLogCallback;
import net.ripe.db.whois.update.mail.WhoisMailGatewaySmtp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageDequeue implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDequeue.class);

    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("^<(.+?)(@.*)?>$");

    private final MaintenanceMode maintenanceMode;
    private final WhoisMailGatewaySmtp mailGateway;
    private final MailMessageDao mailMessageDao;
    private final MessageFilter messageFilter;
    private final MessageParser messageParser;
    private final UpdatesParser updatesParser;
    private final UpdateRequestHandler messageHandler;
    private final LoggerContext loggerContext;
    private final DateTimeProvider dateTimeProvider;
    private final MessageService messageService;

    private final AtomicInteger freeThreads = new AtomicInteger();

    private ExecutorService handlerExecutor;
    private ScheduledExecutorService pollerExecutor;

    @Value("${mail.update.threads:2}")
    private int nrThreads;

    @Value("${mail.dequeue.interval:1000}")
    private int intervalMs;

    @Value("${mailupdates.passwd.error:false}")
    private boolean errorIfPassword;

    @Autowired
    public MessageDequeue(final MaintenanceMode maintenanceMode,
                          final WhoisMailGatewaySmtp mailGateway,
                          final MailMessageDao mailMessageDao,
                          final MessageFilter messageFilter,
                          final MessageParser messageParser,
                          final UpdatesParser updatesParser,
                          final UpdateRequestHandler messageHandler,
                          final LoggerContext loggerContext,
                          final DateTimeProvider dateTimeProvider,
                          final MessageService messageService) {
        this.maintenanceMode = maintenanceMode;
        this.mailGateway = mailGateway;
        this.mailMessageDao = mailMessageDao;
        this.messageFilter = messageFilter;
        this.messageParser = messageParser;
        this.updatesParser = updatesParser;
        this.messageHandler = messageHandler;
        this.loggerContext = loggerContext;
        this.dateTimeProvider = dateTimeProvider;
        this.messageService = messageService;
    }


    @Override
    public void start() {
        if (handlerExecutor != null || pollerExecutor != null) {
            throw new IllegalStateException("Already started");
        }

        if (nrThreads > 0) {
            freeThreads.set(nrThreads);

            handlerExecutor = Executors.newFixedThreadPool(nrThreads);

            pollerExecutor = Executors.newSingleThreadScheduledExecutor();
            pollerExecutor.scheduleWithFixedDelay(new MessagePoller(), intervalMs, intervalMs, TimeUnit.MILLISECONDS);

            LOGGER.info("Message dequeue started");
        }
    }

    @Override
    public void stop(final boolean force) {
        LOGGER.info("Message dequeue stopping");

        if (stopExecutor(pollerExecutor)) {
            pollerExecutor = null;
        }

        if (stopExecutor(handlerExecutor)) {
            handlerExecutor = null;
        }

        LOGGER.info("Message dequeue stopped");
    }

    private boolean stopExecutor(ExecutorService executorService) {
        if (executorService == null) {
            return true;
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(2, TimeUnit.HOURS);
        } catch (Exception e) {
            LOGGER.error("Awaiting termination", e);
            return false;
        }
        return true;
    }

    class MessagePoller implements Runnable {
        @Override
        public void run() {
            try {
                for (; ; ) {
                    if (!maintenanceMode.allowUpdate()) {
                        return;
                    }

                    while (freeThreads.get() == 0) {
                        LOGGER.debug("Postpone message claiming until free thread is available");
                        return;
                    }

                    final String messageId = mailMessageDao.claimMessage();

                    if (messageId == null) {
                        LOGGER.debug("No more messages");
                        return;
                    }

                    LOGGER.debug("Queue {}", messageId);
                    freeThreads.decrementAndGet();
                    handlerExecutor.submit(new MessageHandler(messageId));
                }
            } catch (DataAccessException e) {
                LOGGER.warn("Unable to claim message due to {}", e.getMessage());
            } catch (RuntimeException e) {
                LOGGER.error("Unexpected", e);
            }
        }
    }

    class MessageHandler implements Runnable {
        final String messageId;

        public MessageHandler(String messageId) {
            this.messageId = messageId;
        }

        @Override
        public void run() {
            try {
                handleMessage(messageId);
            } catch (Exception e) {
                LOGGER.error("Unexpected", e);
            } finally {
                freeThreads.incrementAndGet();
            }
        }
    }

    private void handleMessage(final String messageId) {
        final MimeMessage message = mailMessageDao.getMessage(messageId);

        try {
            final EmailMessageInfo bouncedMessage = messageService.getBouncedMessageInfo(message);
            if (bouncedMessage != null) {
                messageService.verifyAndSetAsUndeliverable(bouncedMessage);
                mailMessageDao.deleteMessage(messageId);
                return;
            }

            final EmailMessageInfo unsubscribeMessage = messageService.getUnsubscribedMessageInfo(message);
            if (unsubscribeMessage != null) {
                messageService.verifyAndSetAsUnsubscribed(unsubscribeMessage);
                mailMessageDao.deleteMessage(messageId);
                return;
            }

            final EmailMessageInfo automatedFailureMessage = messageService.getAutomatedFailureMessageInfo(message);
            if (automatedFailureMessage != null) {
                // TODO: verify and set as undeliverable
                mailMessageDao.deleteMessage(messageId);
                return;
            }

        } catch (MailParsingException e){
            LOGGER.info("Error detecting bounce detection or unsubscribing for messageId {} due to {}: {} ({})",
                messageId,
                e.getClass().getSimpleName(), e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "null");
            mailMessageDao.deleteMessage(messageId);
            return;
        } catch (MessagingException ex) {
            LOGGER.error("There was some kind of error processing the message {}", messageId, ex);
        }

        try {
            loggerContext.init(getMessageIdLocalPart(message));
            try {
                handleMessageInContext(messageId, message);
            } finally {
                loggerContext.remove();
            }
        } catch (Exception e) {
            LOGGER.error("Handle message", e);
        }
    }

    String getMessageIdLocalPart(final Message message) throws MessagingException {
        final String[] headers = message.getHeader("Message-Id");
        if (headers != null && headers.length > 0) {
            Matcher matcher = MESSAGE_ID_PATTERN.matcher(headers[0]);
            if (matcher.matches()) {
                return matcher.group(1);
            }

            LOGGER.debug("Unable to parse Message-Id: {}", headers[0]);
        }

        return "No-Message-Id." + dateTimeProvider.getElapsedTime();
    }

    private void handleMessageInContext(final String messageId, final MimeMessage message) throws MessagingException {
        loggerContext.log("msg-in.txt", new MailMessageLogCallback(message));
        mailMessageDao.setStatus(messageId, DequeueStatus.LOGGED);

        final UpdateContext updateContext = new UpdateContext(loggerContext);
        final MailMessage mailMessage = messageParser.parse(message, updateContext);
        mailMessageDao.setStatus(messageId, DequeueStatus.PARSED);

        if (!messageFilter.shouldProcess(mailMessage)) {
            mailMessageDao.deleteMessage(messageId);
            return;
        }

        try {
            handleUpdates(mailMessage, updateContext);
            mailMessageDao.deleteMessage(messageId);
        } catch (RuntimeException e) {
            mailMessageDao.setStatus(messageId, DequeueStatus.FAILED);
            loggerContext.log(new net.ripe.db.whois.common.Message(Messages.Type.ERROR, "Unexpected"), e);
            LOGGER.error("Handle message in context", e);
        }
    }

    private void handleUpdates(final MailMessage mailMessage, final UpdateContext updateContext) {
        final List<Update> updates = updatesParser.parse(updateContext, mailMessage.getContentWithCredentials());
        validatePasswordCredentials(updateContext, updates);

        final UpdateRequest updateRequest = new UpdateRequest(mailMessage, mailMessage.getKeyword(), updates);
        final UpdateResponse response = messageHandler.handle(updateRequest, updateContext);
        mailGateway.sendEmail(mailMessage.getReplyToEmail(), response.getStatus() + ": " + mailMessage.getSubject(), response.getResponse(), null);
    }

    private void validatePasswordCredentials(final UpdateContext updateContext, final List<Update> updates) {
        for (Update update : updates) {
            if (!update.getCredentials().ofType(PasswordCredential.class).isEmpty()){
                updateContext.addGlobalMessage(errorIfPassword ? UpdateMessages.passwordInMailUpdateError() :
                        UpdateMessages.passwordInMailUpdateWarn());
                return;
            }
        }
    }
}
