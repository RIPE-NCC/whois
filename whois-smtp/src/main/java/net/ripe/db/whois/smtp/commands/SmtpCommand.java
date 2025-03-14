package net.ripe.db.whois.smtp.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class SmtpCommand {

    final String value;

    public SmtpCommand(final Pattern pattern, final String value) {
        this.value = parse(pattern, value);
    }

    public String getValue() {
        return value;
    }

    private static String parse(final Pattern pattern, final String value) {
        final Matcher matcher = pattern.matcher(value);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid SMTP command: " + value);
        } else {
            return matcher.group(1);
        }
    }
}
