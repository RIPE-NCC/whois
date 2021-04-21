package net.ripe.db.whois.common.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Whois query log parser.
 *
 */
public class QueryLogEntry {

    private static final Pattern ENTRY_PATTERN = Pattern.compile(
                        "^(\\d+) " +                                // 1. date
                        "+([0-9:]+)\\s+" +                          // 2. timestamp
                        "+(?:[-]?\\d+) " +                          //    channel id (skipped)
                        "PW-(.*)-INFO " +                           // 3. API
                        "\\<(\\d+)\\+(\\d+)\\+0\\> " +              // 4. personal, 5. non-personal objects
                        "(BLOCKED|DISCONNECTED|PARAMETER_ERROR|PROXY_NOT_ALLOWED|UNSUPPORTED_QUERY|REJECTED|EXCEPTION|) " +     // 6. query completion info
                        "(.*s) " +                                  // 7. duration
                        "\\[(.*)\\] " +                             // 8. IP address
                        "--\\s*" +                                  //    separator (skipped)
                        "?(.*)$");                                  // 9. query string

    private static final Pattern QUERY_FILE_PATTERN = Pattern.compile("qrylog\\.\\d{8}");
    private static final Pattern BZIP2_FILE_PATTERN =  Pattern.compile(".*\\.bz2");

    private final String address;
    private final String api;
    private final int personalObjects;
    private final int nonPersonalObjects;
    private final String executionTime;
    private final String queryString;

    private QueryLogEntry(
            final String address,
            final String api,
            final int personalObjects,
            final int nonPersonalObjects,
            final String executionTime,
            final String queryString) {
        this.address = address;
        this.api = api;
        this.personalObjects = personalObjects;
        this.nonPersonalObjects = nonPersonalObjects;
        this.executionTime = executionTime;
        this.queryString = queryString;
    }

    public String getAddress() {
        return address;
    }

    public String getApi() {
        return api;
    }

    public int getPersonalObjects() {
        return personalObjects;
    }

    public int getNonPersonalObjects() {
        return nonPersonalObjects;
    }

    public String getExecutionTime() {
        return executionTime;
    }

    public String getQueryString() {
        return queryString.trim();
    }

    public static QueryLogEntry parse(final String entry) {
        final Matcher matcher = ENTRY_PATTERN.matcher(entry.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unreadable log entry: " + entry);
        }

        return new QueryLogEntry(
            matcher.group(8),                   // address
            matcher.group(3),                   // api
            Integer.parseInt(matcher.group(4)), // personal objects
            Integer.parseInt(matcher.group(5)), // non-personal objects
            matcher.group(7),                   // execution time
            matcher.group(9));                  // query string
    }

    public static boolean isQryLog(final String filename) {
        return QUERY_FILE_PATTERN.matcher(filename).find();
    }

    public static boolean isBZip2(final String filename) {
        return BZIP2_FILE_PATTERN.matcher(filename).matches();
    }
}




