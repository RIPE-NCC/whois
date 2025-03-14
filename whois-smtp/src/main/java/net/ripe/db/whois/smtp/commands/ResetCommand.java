package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class ResetCommand extends SmtpCommand {

    private static Pattern RESET_PATTERN = Pattern.compile("(?i)^(RSET)$");

    ResetCommand(final String value) {
        super(RESET_PATTERN, value);
    }

    static boolean matches(final String value) {
        return RESET_PATTERN.matcher(value).find();
    }
}
