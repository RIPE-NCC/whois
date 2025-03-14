package net.ripe.db.whois.smtp.commands;


import java.util.regex.Pattern;

public class HelloCommand extends SmtpCommand {

    private static Pattern HELLO_PATTERN = Pattern.compile("(?i)HELO (.+)");

    HelloCommand(final String value) {
        super(HELLO_PATTERN, value);
    }

    static boolean matches(final String value) {
        return HELLO_PATTERN.matcher(value).find();
    }
}
