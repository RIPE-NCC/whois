package net.ripe.db.whois.logsearch.logformat;

import org.apache.commons.lang.Validate;

import java.util.regex.Pattern;

public class DailyLogEntry extends LoggedUpdate {
    public static final Pattern UPDATE_LOG_FILE_PATTERN = Pattern.compile("(?i)(?:^|.*/)(\\d+)\\.[\\-.\\w]+\\.gz");

    private final String updateId;
    private final String date;

    public DailyLogEntry(final String updateId, final String date) {
        Validate.isTrue(UPDATE_LOG_FILE_PATTERN.matcher(updateId).matches());
        this.updateId = updateId;
        this.date = date;
    }

    @Override
    public String getUpdateId() {
        return updateId;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public LoggedUpdate.Type getType() {
        return LoggedUpdate.Type.DAILY;
    }
}
