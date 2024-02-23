package net.ripe.db.whois.common.mail;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Transport;
import jakarta.mail.event.TransportEvent;
import jakarta.mail.event.TransportListener;
import jakarta.mail.internet.MimeMessage;
import net.ripe.db.whois.common.dao.BouncedMailDao;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class BounceListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(BounceListener.class);

    private static final String MESSAGE_ID_HEADER = "Message-ID";
    private final BouncedMailDao bouncedMailDao;

    @Autowired
    public BounceListener(final BouncedMailDao bouncedMailDao){
        this.bouncedMailDao = bouncedMailDao;

    }

    public void createListener(final JavaMailSender mailSender, final String from){
        try {
            final Transport transport = ((JavaMailSenderImpl) mailSender).getSession().getTransport();
            transport.addTransportListener(new TransportListener() {

                @Override
                public void messageDelivered(TransportEvent transportEvent) {
                    try {
                        final MimeMessage message = (MimeMessage) transportEvent.getMessage();
                        cleanOnGoingMessage(message);
                    } catch (MessagingException e) {
                        LOGGER.error("Error processing delivered message");
                    }
                }

                @Override
                public void messageNotDelivered(TransportEvent transportEvent) {
                    try {
                        final MimeMessage message = (MimeMessage) transportEvent.getMessage();
                        if (isValidMessage(message) && isBounced(message, transportEvent, from)) {
                            final String bouncedEmail = getClientAddress(message);
                            if (StringUtils.isEmpty(bouncedEmail)){
                                LOGGER.error("Not able to get the client address");
                                return;
                            }
                            bouncedMailDao.createBouncedEmail(bouncedEmail);
                            LOGGER.info("Bounced mail for {}", bouncedEmail);
                        }
                        cleanOnGoingMessage(message);
                    } catch (MessagingException e) {
                        LOGGER.error("Error processing not delivered message");
                    }
                }

                @Override
                public void messagePartiallyDelivered(TransportEvent transportEvent) {
                    try {
                        final MimeMessage message = (MimeMessage) transportEvent.getMessage();
                        cleanOnGoingMessage(message);
                    } catch (MessagingException e) {
                        LOGGER.error("Error processing partial delivered message");
                    }
                }
            });
        } catch (NoSuchProviderException e){
            LOGGER.error("Error creating the listener for incoming messages");
        }
    }

    public boolean checkBounced(final String to){
        return bouncedMailDao.isBouncedEmail(to);
    }

    public void generateMessageId(final MimeMessage mimeMessage, final String to) {
        try {
            final String uniqueId = "<" + System.currentTimeMillis() + "." + Math.random() + ">";
            mimeMessage.addHeader(MESSAGE_ID_HEADER, uniqueId);

            //TODO: [MH] Check if we really need to keep track of the ongoing messages?
            bouncedMailDao.createOnGoingMessageId(uniqueId, to);
        } catch (MessagingException ex){
            LOGGER.error("failing generating message Id");
        }
    }

    private void cleanOnGoingMessage(final MimeMessage message) throws MessagingException {
        final String[] messageIds = message.getHeader(MESSAGE_ID_HEADER);
        if (messageIds.length > 1){
            LOGGER.error("This is a single mail sender service, this shouldn't happen");
        }
        bouncedMailDao.deleteOnGoingMessage(messageIds[0]);
    }

    private boolean isValidMessage(final MimeMessage message) throws MessagingException{
        final String[] messageIds = message.getHeader(MESSAGE_ID_HEADER);
        if (messageIds.length > 1){
            LOGGER.error("This is a single mail sender service, this shouldn't happen");
            return false;
        }
        final String messageId = messageIds[0];
        if (!bouncedMailDao.onGoingMessageExist(messageId)){
            LOGGER.error("Received email with messageId: {} which was not sent by us", messageId);
            return false;
        }
        return true;
    }

    private boolean isBounced(final MimeMessage message, final TransportEvent transportEvent, final String from) throws MessagingException{
        // Check if the failure is due to a bounce by inspecting invalid addresses and Return-Path header
        // If there are any invalid addresses associated with the transportEvent, it indicated that
        // the email failed to be delivered to those recipients. This indicates a potential bounce
        if (transportEvent.getInvalidAddresses() == null || transportEvent.getInvalidAddresses().length <= 0) {
            return false;
        }

        final String returnPath = message.getHeader("Return-Path", null);
        //mailConfiguration.getFrom()
        return returnPath != null && returnPath.contains(from);
    }

    private String getClientAddress(final MimeMessage mimeMessage) throws MessagingException {
        final Address[] addresses = mimeMessage.getRecipients(jakarta.mail.Message.RecipientType.TO);
        if (addresses == null || addresses.length == 0) {
            return "";
        }
        return addresses[0].toString(); // Assuming there's only one address
    }
}
