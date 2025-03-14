package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class QuitCommand extends SmtpCommand {

    private static Pattern QUIT_PATTERN = Pattern.compile("(?i)^(QUIT)$");

    QuitCommand(final String value) {
        super(QUIT_PATTERN, value);
    }

    static boolean matches(final String value) {
        return QUIT_PATTERN.matcher(value).find();
    }

}
