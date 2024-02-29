package net.ripe.db.whois.common.mail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailUtil {

    private final static Pattern EMAIL_REGEX = Pattern.compile("<([^<>]+)>");

    public static String normaliseEmail(final String from){
        final Matcher match = EMAIL_REGEX.matcher(from);
        return match.find() ? match.group(1) : from;
    }
}
