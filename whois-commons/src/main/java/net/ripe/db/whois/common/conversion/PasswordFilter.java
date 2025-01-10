package net.ripe.db.whois.common.conversion;

import com.google.common.base.Splitter;
import org.apache.commons.lang.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PasswordFilter {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();
    private static final Splitter ENCODED_COMMA_SPLITTER = Splitter.on("%2C").trimResults();
    private static final Splitter AMPERSAND_SPLITTER = Splitter.on('&').omitEmptyStrings();
    private static final Splitter EQUALS_SPLITTER = Splitter.on('=').omitEmptyStrings();

    private static final String PASSWORD_LOWER_STRING = "password";
    private static final String OVERRIDE_LOWER_STRING = "override";

    //from logsearch tweaked
    private static final Pattern PASSWORD_PATTERN_FOR_CONTENT = Pattern.compile("(?im)^(override|password)(:|%3A)\\s*(.+)\\s*$");
    private static final Pattern BASIC_AUTH_HEADER_PATTERN_FOR_CONTENT = Pattern.compile("(?im)^Header: Authorization=Basic *([^ ]+) *$", Pattern.CASE_INSENSITIVE);

    private static final Pattern URI_PASSWORD_PATTERN_PASSWORD_FOR_URL = Pattern.compile("(?<=)(password|override)(:|=|%3A)([^&^\\s]*)", Pattern.CASE_INSENSITIVE);

    public static String filterPasswordsInContents(final String contents) {
        if(StringUtils.isEmpty(contents)) {
            return contents;
        }

        final String filteredContent = replaceBasicAuthHeader(BASIC_AUTH_HEADER_PATTERN_FOR_CONTENT.matcher(contents));
        return replacePassword(PASSWORD_PATTERN_FOR_CONTENT.matcher(filteredContent));
    }

    private static String replaceBasicAuthHeader(final Matcher matcher)  {
        final StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(result, String.format("%s%s", StringUtils.substringBefore(matcher.group(0), matcher.group(1)),"FILTERED"));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public static String filterPasswordsInUrl(final String url) {
        if (url == null) {
            return null;
        }
        final String lurl = url.toLowerCase();
        if (!lurl.contains(PASSWORD_LOWER_STRING) && !lurl.contains(OVERRIDE_LOWER_STRING)) {
            return url;
        }
        final Matcher matcher = URI_PASSWORD_PATTERN_PASSWORD_FOR_URL.matcher(url);
        return replacePassword(matcher);
    }

    public static String removePasswordsInUrl(final String url) {
        final StringBuilder builder = new StringBuilder();
        String separator = "";
        for (String next : AMPERSAND_SPLITTER.split(url)) {
            final Iterator<String> iterator = EQUALS_SPLITTER.split(next).iterator();
            if (iterator.hasNext() && iterator.next().equalsIgnoreCase(PASSWORD_LOWER_STRING)) {
                continue;
            }
            builder.append(separator).append(next);
            separator = "&";
        }

        return builder.toString();
    }

    private static String replacePassword(final Matcher matcher) {
        final StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            final String match = matcher.group(1).toLowerCase();
            if (match.endsWith(PASSWORD_LOWER_STRING)) {
                matcher.appendReplacement(result, matcher.group(1) + matcher.group(2) + "FILTERED");
            } else if (match.endsWith(OVERRIDE_LOWER_STRING)) {
                // try comma
                List<String> override = COMMA_SPLITTER.splitToList(matcher.group(3));
                if (override.size() <= 1) {
                    // try url-encoded comma
                    override = ENCODED_COMMA_SPLITTER.splitToList(matcher.group(3));
                }
                switch (override.size()) {
                    case 3:
                        matcher.appendReplacement(result, String.format("%s%s%s,FILTERED,%s", matcher.group(1), matcher.group(2), override.get(0), override.get(2)));
                        break;
                    case 2:
                        matcher.appendReplacement(result, String.format("%s%s%s,FILTERED", matcher.group(1), matcher.group(2), override.get(0)));
                        break;
                    default:
                        matcher.appendReplacement(result, String.format("%s%sFILTERED", matcher.group(1), matcher.group(2)));
                }
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
