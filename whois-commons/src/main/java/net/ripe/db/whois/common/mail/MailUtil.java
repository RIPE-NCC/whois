package net.ripe.db.whois.common.mail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MailUtil {

    private final static Pattern BETWEEN_ANGLE_BRACKETS_REGEX = Pattern.compile("<([^<>]+)>");

    public static String extractContentBetweenAngleBrackets(final String from){
        final Matcher match = BETWEEN_ANGLE_BRACKETS_REGEX.matcher(from);
        return match.find() ? match.group(1) : from;
    }
}
