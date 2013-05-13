package net.ripe.db.whois.common.support;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryLogEntry {
    private static final Pattern ENTRY_PATTERN = Pattern.compile("^(\\d+) +([0-9:]+) +(?:\\d+) (.+).* \\<(\\d+)\\+(\\d+)\\+(\\d+)\\> +(?:[A-Za-z_-]+ +)?(\\d+\\.\\d+)s.*-- ?(.*)$");

    private final int personalObjects;
    private final int nonPersonalObjects;
    private final long executionTime;
    private final String queryString;

    public static QueryLogEntry parse(final String entry) {
        final Matcher matcher = ENTRY_PATTERN.matcher(entry.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Unreadable log entry: " + entry);
        }

        int personalObjects = Integer.parseInt(matcher.group(4));
        int nonPersonalObjects = Integer.parseInt(matcher.group(5));
        long executionTime = (long) (Double.parseDouble(matcher.group(7)) * 1000);
        String queryString = matcher.group(8);

        return new QueryLogEntry(personalObjects, nonPersonalObjects, executionTime, queryString);
    }

    public QueryLogEntry(final int personalObjects, final int nonPersonalObjects, final long executionTime, final String queryString) {
        this.personalObjects = personalObjects;
        this.nonPersonalObjects = nonPersonalObjects;
        this.executionTime = executionTime;
        this.queryString = queryString;
    }

    public int getPersonalObjects() {
        return personalObjects;
    }

    public int getNonPersonalObjects() {
        return nonPersonalObjects;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getQueryString() {
        return queryString.trim();
    }
}




