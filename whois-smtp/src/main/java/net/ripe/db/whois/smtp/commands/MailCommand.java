package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class MailCommand extends SmtpCommand {

    private static Pattern MAIL_FROM_PATTERN = Pattern.compile("(?i)MAIL FROM: (.+)");

    MailCommand(final String value) {
        super(MAIL_FROM_PATTERN, value);
    }

    static boolean matches(final String value) {
        return MAIL_FROM_PATTERN.matcher(value).find();
    }
}
