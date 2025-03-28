package net.ripe.db.whois.smtp;


import com.google.common.collect.Sets;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class MimeUtility {

    private static final Session SESSION = Session.getInstance(new Properties());

    private MimeUtility() {
        // do not instantiate
    }

    @Nullable
    public static MimeMessage parseMessage(final byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return new MimeMessage(SESSION, new ByteArrayInputStream(bytes));
        } catch (MessagingException e) {
            throw new SmtpException(SmtpResponses.internalError());
        }
    }

    public static Set<InternetAddress> getFromAddresses(final MimeMessage mimeMessage) {
        if (mimeMessage == null) {
            return Collections.emptySet();
        }

        final String[] fromHeaders;
        try {
            fromHeaders = mimeMessage.getHeader("From");
            if (fromHeaders == null || fromHeaders.length == 0) {
                return Collections.emptySet();
            }
        } catch (MessagingException e) {
            return Collections.emptySet();
        }

        final Set<InternetAddress> results = Sets.newHashSet();
        for (String fromHeader : fromHeaders) {
            final InternetAddress fromAddress = parseAddress(fromHeader);
            if ((fromAddress != null) && (fromAddress.getAddress() != null)) {
                results.add(fromAddress);
            }
        }

        return results;
    }


    @Nullable
    public static InternetAddress parseAddress(final String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        try {
            final InternetAddress[] parsed = InternetAddress.parse(address, true);
            if (parsed == null || parsed.length != 1 || parsed[0] == null) {
                return null;
            } else {
                return parsed[0];
            }
        } catch (AddressException e) {
            return null;
        }
    }





}
