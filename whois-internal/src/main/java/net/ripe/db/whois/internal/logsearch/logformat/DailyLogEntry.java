package net.ripe.db.whois.internal.logsearch.logformat;

public class DailyLogEntry extends LoggedUpdate {
    private final String updateId;
    private final String date;

    public DailyLogEntry(final String updateId, final String date) {
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
