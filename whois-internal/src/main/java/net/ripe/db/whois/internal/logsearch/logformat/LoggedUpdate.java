package net.ripe.db.whois.internal.logsearch.logformat;

/**
 * updateId is a unique identifier of an indexed file. It is the absolute path to the indexed file, except for:
 * - tarredlogentry: absolute location of tarfile, concatenated with the path inside the tarfile;
 * - legacylogentry: absolute location of bzip2 file, followed by / and the number of message in the file (starting from 0)
 */
public abstract class LoggedUpdate implements Comparable<LoggedUpdate> {

    public static enum Type {DAILY, TARRED, LEGACY}

    public abstract String getUpdateId();
    public abstract String getDate();
    public abstract Type getType();

    public static final LoggedUpdate parse(final String updateId, final String date) {
        switch (getUpdateType(updateId)) {
            case DAILY:
                return new DailyLogEntry(updateId, date);
            case TARRED:
                return new TarredLogEntry(updateId, date);
            case LEGACY:
                return new LegacyLogEntry(updateId, date);
            default:
                throw new IllegalStateException("Unknown type");
        }
    }

    @Override
    public int compareTo(LoggedUpdate o) {
        return getUpdateId().compareTo(o.getUpdateId());
    }

    private static Type getUpdateType(final String updateId) {
        if (updateId.contains(".bz2")) {
            return Type.LEGACY;
        } else {
            if (updateId.contains(".tar")) {
                return Type.TARRED;
            } else {
                return Type.DAILY;
            }
        }
    }
}
