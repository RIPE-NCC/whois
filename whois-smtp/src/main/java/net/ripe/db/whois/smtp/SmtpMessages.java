package net.ripe.db.whois.smtp;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.domain.Hosts;

import java.time.LocalDateTime;

public class SmtpMessages {

    private SmtpMessages() {
        // do not instantiate
    }

    public static Message banner(final String applicationVersion) {
        return new Message(Messages.Type.INFO, "220 %s SMTP Whois %s %s", Hosts.getInstanceName(), applicationVersion, LocalDateTime.now());
    }

    public static Message hello(final String value) {
        return new Message(Messages.Type.INFO, "250 %s Hello %s", Hosts.getInstanceName(), value);
    }

    public static Message extendedHello(final String value) {
        return new Message(Messages.Type.INFO,
            "250-%s Hello %s\n" +
//                "250-SIZE 52428800\n" +       // TODO
//                "250-8BITMIME\n" +            // TODO
                "250 HELP", Hosts.getInstanceName(), value);
    }

    public static Message invalidHello(final String value) {
        return new Message(Messages.Type.ERROR, "501 Syntactically invalid %s argument(s)", value);
    }

    public static Message help() {
        return new Message(Messages.Type.INFO,
            "214-Commands supported:\n" +
                "214 HELO EHLO MAIL RCPT DATA NOOP HELP RSET QUIT");
    }

    public static Message goodbye() {
        return new Message(Messages.Type.INFO, "221 %s closing connection", Hosts.getInstanceName());
    }

    public static Message ok() {
        return new Message(Messages.Type.INFO, "250 OK");
    }

    public static Message okId(final String value) {
        return new Message(Messages.Type.INFO, "250 OK id=%s", value);
    }

    public static Message accepted() {
        return new Message(Messages.Type.INFO, "250 Accepted");
    }

    public static Message enterMessage() {
        return new Message(Messages.Type.INFO, "354 Enter message, ending with \".\" on a line by itself");
    }

    public static Message rcptBeforeData() {
        return new Message(Messages.Type.ERROR, "503 valid RCPT command must precede DATA");
    }

    public static Message senderAddressDomain(final String value) {
        return new Message(Messages.Type.ERROR, "501 %s: sender address must contain a domain", value);
    }

    public static Message unrecognisedCommand() {
        return new Message(Messages.Type.ERROR, "500 Unrecognised command");
    }

    public static Message internalError() {
        return new Message(Messages.Type.ERROR, "500 internal error occurred.");
    }

    public static Message timeout() {
        return new Message(Messages.Type.ERROR, "421 %s: SMTP command timeout - closing connection", Hosts.getInstanceName());
    }
}
