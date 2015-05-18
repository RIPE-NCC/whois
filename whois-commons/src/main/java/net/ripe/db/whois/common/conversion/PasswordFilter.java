package net.ripe.db.whois.common.conversion;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordFilter {

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

    private static final Pattern URI_PASSWORD_PATTERN_PASSWORD_FOR_URL = Pattern.compile("(?<=)(password|override)(:|=|%3A)([^&]*)");
    public static String filterPasswordsInUrl(final String url) {
        String result = url;
        if( url != null ) {
            final Matcher matcher = URI_PASSWORD_PATTERN_PASSWORD_FOR_URL.matcher(url);
            result = replacePassword(matcher);
        }
        return result;
    }

    //from logfilesearch.filterContents tweaked
    private static String replacePassword(final Matcher matcher) {

        final StringBuffer result = new StringBuffer();
        while(matcher.find())  {
            if (matcher.group(1).endsWith("password") || matcher.group(1).endsWith("PASSWORD")) {
                matcher.appendReplacement(result, matcher.group(1) + matcher.group(2) + "FILTERED");
            } else if(matcher.group(1).endsWith("override") || matcher.group(1).endsWith("OVERRIDE")) {
                // try comma
                String[] override = StringUtils.split(matcher.group(3), ',');
                if (override == null || override.length <= 1) {
                    // try url-encoded comma
                    override = StringUtils.splitByWholeSeparator(matcher.group(3), "%2C");
                }
                if (override != null) {
                    if (override.length == 3) {
                        matcher.appendReplacement(result, String.format("%s%s%s,FILTERED,%s", matcher.group(1), matcher.group(2), override[0], override[2]));
                    } else if (override.length == 2) {
                        matcher.appendReplacement(result, String.format("%s%s%s,FILTERED", matcher.group(1), matcher.group(2), override[0]));
                    } else {
                        matcher.appendReplacement(result, String.format("%s%sFILTERED", matcher.group(1), matcher.group(2)));
                    }
                }
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }

}
