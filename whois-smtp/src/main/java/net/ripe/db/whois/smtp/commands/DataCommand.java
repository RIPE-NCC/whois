package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class DataCommand extends SmtpCommand {

    private static Pattern DATA_PATTERN = Pattern.compile("(?i)^(DATA)$");

    DataCommand(final String value) {
        super(DATA_PATTERN, value);
    }

    static boolean matches(final String value) {
        return DATA_PATTERN.matcher(value).find();
    }
}
