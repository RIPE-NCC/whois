package net.ripe.db.whois.logsearch.logformat;

import net.ripe.db.whois.logsearch.NewLogFormatProcessor;
import org.apache.commons.lang.Validate;

import java.io.File;

public class TarredLogEntry extends LoggedUpdate {

    private final String updateId;
    private final String date;

    public TarredLogEntry(String tarFilePath, String date, String tarEntryPath) {
        this(String.format("%s%s%s", tarFilePath, File.separator, tarEntryPath), date);
    }

    public TarredLogEntry(String updateId, String date) {
        Validate.isTrue(NewLogFormatProcessor.INDEXED_LOG_ENTRIES.matcher(updateId).matches());
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
}
