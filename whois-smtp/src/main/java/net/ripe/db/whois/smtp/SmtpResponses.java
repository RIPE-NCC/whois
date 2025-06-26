package net.ripe.db.whois.smtp;

import io.netty.handler.codec.smtp.DefaultSmtpResponse;
import io.netty.handler.codec.smtp.SmtpResponse;
import net.ripe.db.whois.common.domain.Hosts;

import java.time.LocalDateTime;

public class SmtpResponses {

    private SmtpResponses() {
        // do not instantiate
    }

    public static SmtpResponse banner(final CharSequence applicationVersion) {
        return new DefaultSmtpResponse(220, String.format("%s SMTP Whois %s %s", Hosts.getInstanceName(), applicationVersion, LocalDateTime.now()));
    }

    public static SmtpResponse hello(final CharSequence value) {
        return new DefaultSmtpResponse(250, String.format("%s Hello %s", Hosts.getInstanceName(), value));
    }

    public static SmtpResponse extendedHello(final CharSequence domain, final Integer maximumSize) {
        if (maximumSize > 0) {
            return new DefaultSmtpResponse(
                    250,
                        String.format("%s Hello %s", Hosts.getInstanceName(), domain),
                        String.format("SIZE %d", maximumSize),
                        "8BITMIME",
                        "HELP");
        } else {
            return new DefaultSmtpResponse(
                    250,
                        String.format("%s Hello %s", Hosts.getInstanceName(), domain),
                        "8BITMIME",
                        "HELP");
        }
    }

    public static SmtpResponse invalidHello() {
        return new DefaultSmtpResponse(501, "Syntactically invalid HELO argument(s)");
    }

    public static SmtpResponse invalidEhlo() {
        return new DefaultSmtpResponse(501, "Syntactically invalid EHLO argument(s)");
    }

    public static SmtpResponse help() {
        return new DefaultSmtpResponse(214,
            "Commands supported:",
            "HELO EHLO MAIL RCPT DATA NOOP HELP RSET QUIT");
    }

    public static SmtpResponse goodbye() {
        return new DefaultSmtpResponse(221, String.format("%s closing connection", Hosts.getInstanceName()));
    }

    public static SmtpResponse ok() {
        return new DefaultSmtpResponse(250, "OK");
    }

    public static SmtpResponse okId(final String value) {
        return new DefaultSmtpResponse(250, String.format("OK id=%s", value));
    }

    public static SmtpResponse accepted() {
        return new DefaultSmtpResponse(250, "Accepted");
    }

    public static SmtpResponse enterMessage() {
        return new DefaultSmtpResponse(354, "Enter message, ending with \".\" on a line by itself");
    }

    public static SmtpResponse rcptBeforeData() {
        return new DefaultSmtpResponse(503, "valid RCPT command must precede DATA");
    }

    public static SmtpResponse invalidAddress() {
        return new DefaultSmtpResponse(501, "invalid address");
    }

    public static SmtpResponse unrecognisedCommand() {
        return new DefaultSmtpResponse(500, "unrecognised command");
    }

    public static SmtpResponse internalError() {
        return new DefaultSmtpResponse(500, "internal error occurred.");
    }

    public static SmtpResponse lineTooLong() {
        return new DefaultSmtpResponse(501, "line too long");
    }

    public static SmtpResponse refusingMessageFrom(final String value) {
        return new DefaultSmtpResponse(500, String.format("refusing to accept message from %s", value));
    }

    public static SmtpResponse timeout() {
        return new DefaultSmtpResponse(421, String.format("%s: SMTP command timeout - closing connection", Hosts.getInstanceName()));
    }

    public static SmtpResponse sizeExceeded() {
        return new DefaultSmtpResponse(523, "the total message size exceeds the server limit");
    }
}
