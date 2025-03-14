package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class HelpCommand extends SmtpCommand {

    private static Pattern HELP_PATTERN = Pattern.compile("(?i)^(HELP)$");

    HelpCommand(final String value) {
        super(HELP_PATTERN, value);
    }

    static boolean matches(final String value) {
        return HELP_PATTERN.matcher(value).find();
    }

}
