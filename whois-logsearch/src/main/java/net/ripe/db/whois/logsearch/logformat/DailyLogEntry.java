package net.ripe.db.whois.logsearch.logformat;

import net.ripe.db.whois.logsearch.NewLogFormatProcessor;
import org.apache.commons.lang.Validate;

public class DailyLogEntry extends LoggedUpdate {
    private final String updateId;
    private final String date;

    public DailyLogEntry(final String updateId, final String date) {
        Validate.isTrue(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher(updateId).matches());
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

    @Override
    public String toString() {
        return updateId;
    }
}
