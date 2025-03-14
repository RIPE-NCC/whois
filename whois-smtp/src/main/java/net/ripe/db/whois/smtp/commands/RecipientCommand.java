package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class RecipientCommand extends SmtpCommand {

    private static Pattern RCPT_TO_PATTERN = Pattern.compile("(?i)RCPT TO: (.+)");

    RecipientCommand(final String value) {
        super(RCPT_TO_PATTERN, value);
    }

    static boolean matches(final String value) {
        return RCPT_TO_PATTERN.matcher(value).find();
    }
}
