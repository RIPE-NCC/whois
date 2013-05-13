package net.ripe.db.whois.common.domain;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: [ES] this class is only used for testing
public class RpslObjectVersions {

    private static Pattern LINE_PATTERN = Pattern.compile("(\\d+)\\s+(.*\\s+.*)\\s+([A-Z/]+)\\s*");
    private static DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-dd-mm hh:MM");
    private static Splitter LINE_SPLITTER = Splitter.on("\n");

    private List<Entry> versions;

    private RpslObjectVersions(final List<Entry> versions) {
        this.versions = versions;
    }


    public static RpslObjectVersions parse(final String input) {
        final List<Entry> versions = Lists.newArrayList();

        for (String line : LINE_SPLITTER.split(input)) {
            Matcher m = LINE_PATTERN.matcher(line);

            if (m.matches()) {
                int version = Integer.parseInt(m.group(1));
                Date date;
                try {
                    date = DATE_TIME_FORMAT.parse(m.group(2));
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
                Operation operation;
                if (m.group(3).equals("ADD/UPD")) {
                    operation = Operation.ADD_UPDATE;
                }
                else if ( m.group(3).equals("DEL") ) {
                    operation = Operation.DELETE;
                }
                else {
                    throw new IllegalArgumentException("Operation must be 'ADD/UPD' or 'DEL'");
                }

                versions.add(new Entry(version, date, operation));
            }
        }

        return new RpslObjectVersions(versions);
    }

    public List<Entry> getVersions() {
        return this.versions;
    }

    public static class Entry {

        private Date date;
        private int version;
        private Operation operation;

        public Entry(final int version, final Date date, final Operation operation) {
            this.version = version;
            this.date = date;
            this.operation = operation;
        }

        public Operation getOperation() {
            return operation;
        }

        public Date getDate() {
            return date;
        }

        public int getVersion() {
            return version;
        }

    }

    public enum Operation {ADD_UPDATE, DELETE}
}
