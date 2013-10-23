package net.ripe.db.whois.logsearch.logformat;

public class LegacyLogEntry extends LoggedUpdate {
    private final String updateId;
    private final String date;

    public LegacyLogEntry(String updateId, String date) {
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
    public Type getType() {
        return Type.LEGACY;
    }

    @Override
    public String toString() {
        return updateId;
    }
}
