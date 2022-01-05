package net.ripe.db.whois.common.conversion;

import com.google.common.base.Splitter;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordFilter {

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').trimResults();
    private static final Splitter ENCODED_COMMA_SPLITTER = Splitter.on("%2C").trimResults();
    private static final Splitter AMPERSAND_SPLITTER = Splitter.on('&').omitEmptyStrings();
    private static final Splitter EQUALS_SPLITTER = Splitter.on('=').omitEmptyStrings();

    //from logsearch tweaked
    private static final Pattern PASSWORD_PATTERN_FOR_CONTENT = Pattern.compile("(?im)^(override|password)(:|%3A)\\s*(.+)\\s*$");
    public static String filterPasswordsInContents(final String contents) {
        String result = contents;
        if( contents != null ) {
            final Matcher matcher = PASSWORD_PATTERN_FOR_CONTENT.matcher(contents);
            result = replacePassword(matcher);
        }
        return result;
    }

    private static final Pattern URI_PASSWORD_PATTERN_PASSWORD_FOR_URL = Pattern.compile("(?<=)(password|override)(:|=|%3A)([^&]*)", Pattern.CASE_INSENSITIVE);
    public static String filterPasswordsInUrl(final String url) {
        String result = url;
        if( url != null ) {
            final Matcher matcher = URI_PASSWORD_PATTERN_PASSWORD_FOR_URL.matcher(url);
            result = replacePassword(matcher);
        }
        return result;
    }

    public static String removePasswordsInUrl(final String url) {
        final StringBuilder builder = new StringBuilder();
        String separator = "";
        for (String next : AMPERSAND_SPLITTER.split(url)) {
            final Iterator<String> iterator = EQUALS_SPLITTER.split(next).iterator();
            if (iterator.hasNext() && iterator.next().equalsIgnoreCase("password")) {
                continue;
            }

            builder.append(separator).append(next);
            separator = "&";
        }

        return builder.toString();
    }

    //from logfilesearch.filterContents tweaked
    private static String replacePassword(final Matcher matcher) {
        final StringBuffer result = new StringBuffer();
        while(matcher.find())  {
            if (matcher.group(1).endsWith("password") || matcher.group(1).endsWith("PASSWORD")) {
                matcher.appendReplacement(result, matcher.group(1) + matcher.group(2) + "FILTERED");
            } else if(matcher.group(1).endsWith("override") || matcher.group(1).endsWith("OVERRIDE")) {
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
