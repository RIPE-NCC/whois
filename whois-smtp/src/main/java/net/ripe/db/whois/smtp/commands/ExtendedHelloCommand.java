package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class ExtendedHelloCommand extends SmtpCommand {

    private static Pattern EHLO_PATTERN = Pattern.compile("(?i)EHLO (.+)");

    ExtendedHelloCommand(final String value) {
        super(EHLO_PATTERN, value);
    }

    static boolean matches(final String value) {
        return EHLO_PATTERN.matcher(value).find();
    }
}
