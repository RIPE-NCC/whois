package net.ripe.db.whois.logsearch.logformat;

import org.apache.commons.lang.Validate;

import java.io.File;
import java.util.regex.Pattern;

public class TarredLogEntry extends LoggedUpdate {

    private static final Pattern INDEXED_LOG_ENTRIES = Pattern.compile(".*/[0-9]+\\.(?:msg-in|ack)\\.txt\\.gz$");

    private final String updateId;
    private final String date;

    public TarredLogEntry(String tarFilePath, String date, String tarEntryPath) {
        this(String.format("%s%s%s", tarFilePath, File.separator, tarEntryPath), date);
    }

    public TarredLogEntry(String updateId, String date) {
        this.date = date;
        this.updateId = updateId;
        Validate.isTrue(INDEXED_LOG_ENTRIES.matcher(updateId).matches());
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
}
