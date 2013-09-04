package net.ripe.db.whois.logsearch.logformat;

import java.io.File;

public class TarredLogEntry extends LoggedUpdate {

    private final String updateId;
    private final String date;

    public TarredLogEntry(String tarFilePath, String date, String tarEntryPath) {
        this(String.format("%s%s%s", tarFilePath, File.separator, tarEntryPath), date);
    }

    public TarredLogEntry(String updateId, String date) {
        this.date = date;
        this.updateId = updateId;
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
        return Type.TARRED;
    }

    @Override
    public String toString() {
        return updateId;
    }
}
