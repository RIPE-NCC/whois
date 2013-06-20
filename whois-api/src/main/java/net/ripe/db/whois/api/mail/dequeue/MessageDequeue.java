package net.ripe.db.whois.api.mail.dequeue;

import net.ripe.db.whois.api.UpdatesParser;
import net.ripe.db.whois.api.mail.MailMessage;
import net.ripe.db.whois.api.mail.dao.MailMessageDao;
import net.ripe.db.whois.common.ApplicationService;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.update.domain.*;
import net.ripe.db.whois.update.handler.UpdateRequestHandler;
import net.ripe.db.whois.update.log.LoggerContext;
import net.ripe.db.whois.update.mail.MailGateway;
import net.ripe.db.whois.update.mail.MailMessageLogCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageDequeue implements ApplicationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDequeue.class);

    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("^<(.+?)(@.*)?>$");

    private final MailGateway mailGateway;
    private final MailMessageDao mailMessageDao;
    private final MessageFilter messageFilter;
    private final MessageParser messageParser;
    private final UpdatesParser updatesParser;
    private final UpdateRequestHandler messageHandler;
    private final LoggerContext loggerContext;

    private volatile boolean running;
    private final AtomicInteger freeThreads = new AtomicInteger();

    private ExecutorService handlerExecutor;
    private ExecutorService pollerExecutor;

    private int nrThreads;

    @Value("${mail.dequeue.threads}")
    void setNrThreads(final int nrThreads) {
        this.nrThreads = nrThreads;
    }

    private int intervalMs;

    @Value("${mail.dequeue.interval}")
    public void setIntervalMs(final int intervalMs) {
        this.intervalMs = intervalMs;
    }

    @Autowired
    public MessageDequeue(final MailGateway mailGateway, final MailMessageDao mailMessageDao, final MessageFilter messageFilter, final MessageParser messageParser, final UpdatesParser updatesParser, final UpdateRequestHandler messageHandler, final LoggerContext loggerContext) {
        this.mailGateway = mailGateway;
        this.mailMessageDao = mailMessageDao;
        this.messageFilter = messageFilter;
        this.messageParser = messageParser;
        this.updatesParser = updatesParser;
        this.messageHandler = messageHandler;
        this.loggerContext = loggerContext;
    }

    @Override
    public void start() {
        if (running) {
            throw new IllegalStateException("Already started");
        }

        running = true;
        freeThreads.set(nrThreads);

        handlerExecutor = Executors.newFixedThreadPool(nrThreads);
        pollerExecutor = Executors.newSingleThreadExecutor();
        pollerExecutor.submit(new MessagePoller());

        LOGGER.info("Message dequeue started");
    }

    @Override
    public void stop() {
        stop(false);
    }

    @PreDestroy
    public void forceStopNow() {
        stop(true);
    }

    private void stop(final boolean force) {
        if (!running) {
            return;
        }

        running = false;

        LOGGER.info("Message dequeue stopping (force: {})", force);

        if (force) {
            pollerExecutor.shutdownNow();
            handlerExecutor.shutdownNow();
        } else {
            pollerExecutor.shutdown();
            handlerExecutor.shutdown();
        }

        try {
            pollerExecutor.awaitTermination(2, TimeUnit.HOURS);
            handlerExecutor.awaitTermination(2, TimeUnit.HOURS);
        } catch (Exception e) {
            LOGGER.error("Awaiting termination", e);
        }

        pollerExecutor = null;
        handlerExecutor = null;

        LOGGER.info("Message dequeue stopped (force: {})", force);
    }

    class MessagePoller implements Runnable {
        @Override
        public void run() {
            while (running) {
                try {
                    final String messageId = mailMessageDao.claimMessage();
                    if (messageId == null) {
                        LOGGER.debug("No more messages");
                        Thread.sleep(intervalMs);
                        continue;
                    }

                    LOGGER.debug("Queue {}", messageId);
                    freeThreads.decrementAndGet();
                    handlerExecutor.submit(new MessageHandler(messageId));

                    while (freeThreads.get() == 0 && running) {
                        LOGGER.debug("Postpone message claiming until free thread is available");
                        Thread.sleep(intervalMs);
                    }
                } catch (InterruptedException ignored) {
                } catch (RuntimeException e) {
                    LOGGER.error("Unexpected", e);
                }
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
            loggerContext.init(getMessageIdLocalPart(message));
            try {
                handleMessageInContext(messageId, message);
            } finally {
                loggerContext.remove();
            }
        } catch (MessagingException e) {
            LOGGER.error("Handle message", e);
        } catch (IOException e) {
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

        return "No-Message-Id." + System.nanoTime();
    }

    private void handleMessageInContext(final String messageId, final MimeMessage message) throws MessagingException, IOException {
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

        final UpdateRequest updateRequest = new UpdateRequest(mailMessage, mailMessage.getKeyword(), mailMessage.getUpdateMessage(), updates);
        final UpdateResponse response = messageHandler.handle(updateRequest, updateContext);
        mailGateway.sendEmail(mailMessage.getReplyToEmail(), response.getStatus() + ": " + mailMessage.getSubject(), response.getResponse());
    }
}
