package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class NoopCommand extends SmtpCommand {

    private static Pattern NOOP_PATTERN = Pattern.compile("(?i)^(NOOP)$");

    NoopCommand(final String value) {
        super(NOOP_PATTERN, value);
    }

    static boolean matches(final String value) {
        return NOOP_PATTERN.matcher(value).find();
    }

}
