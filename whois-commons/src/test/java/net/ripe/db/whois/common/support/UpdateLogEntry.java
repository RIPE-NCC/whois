package net.ripe.db.whois.common.support;

import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.rpsl.ObjectType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateLogEntry {

    private static final Pattern UPDATE_LOG_PATTERN = Pattern.compile(
                        "^(\\d{8}\\s\\d{2}[:]\\d{2}[:]\\d{2})\\s" +     // date and time
                        "\\[\\s*\\d+\\]\\s" +                           // number since restart (ignored)
                        "([\\d.]+ .+)\\s+" +                            // stopwatch
                        "(DRY|UPD)\\s" +                                // is dry run?
                        "(CREATE|DELETE|MODIFY|NOOP|null)\\s+" +        // action
                        "([a-z0-9-]*)\\s" +                             // object type
                        "([^\\(]*)\\s" +                                // primary key
                        "\\(\\d\\)\\s" +                                // retry count (ignored)
                        "(SUCCESS|FAILURE|FAILED|FAILED_AUTHENTICATION|PENDING_AUTHENTICATION)\\s*\\:\\s" +     // status
                        "<E(\\d+),W(\\d+),I(\\d+)>\\s" +                // errors, warnings, infos
                        "AUTH\\s([\\w,]+|)\\s-\\s" +                    // credentials (SSO|OVERRIDE|X509|PGP|PWD)
                        "(Mail|WhoisRestApi|SyncUpdate|InternalJob)" +  // origin type
                        "\\((.*)\\)$");                                 // origin id

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    private static final Splitter COMMA_SPLITTER = Splitter.on(',').omitEmptyStrings();

    private final LocalDateTime timestamp;
    private final String duration;
    private final boolean isDryRun;
    private final String action;
    private final ObjectType objectType;
    private final String primaryKey;
    private final String updateStatus;
    private final int errors;
    private final int warnings;
    private final int infos;
    private final List<String> credentials;
    private final String originType;
    private final String originId;

    private UpdateLogEntry(
        final LocalDateTime timestamp,
        final String duration,
        final boolean isDryRun,
        final String action,
        final ObjectType objectType,
        final String primaryKey,
        final String updateStatus,
        final int errors,
        final int warnings,
        final int infos,
        final List<String> credentials,
        final String originType,
        final String originId) {
        this.timestamp = timestamp;
        this.duration = duration;
        this.isDryRun = isDryRun;
        this.action = action;
        this.objectType = objectType;
        this.primaryKey = primaryKey;
        this.updateStatus = updateStatus;
        this.errors = errors;
        this.warnings = warnings;
        this.infos = infos;
        this.credentials = credentials;
        this.originType = originType;
        this.originId = originId;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getAction() {
        return this.action;
    }

    public ObjectType getObjectType() {
        return this.objectType;
    }

    public String getPrimaryKey() {
        return this.primaryKey;
    }

    public String getUpdateStatus() {
        return this.updateStatus;
    }

    public String getOriginType() {
        return this.originType;
    }

    public String getOriginId() {
        return this.originId;
    }

    public List<String> getCredentials() {
        return this.credentials;
    }

    public static UpdateLogEntry parse(final String line) {
        final Matcher matcher = UPDATE_LOG_PATTERN.matcher(line);
        if (matcher.matches()) {
            try {
                return new UpdateLogEntry(
                    LocalDateTime.from(DATE_TIME_FORMAT.parse(matcher.group(1))),   // date and time
                    matcher.group(2),                       // duration
                    "DRY".equals(matcher.group(3)),         // is dry run
                    matcher.group(4),                       // action
                    ObjectType.getByName(matcher.group(5)), // object type
                    matcher.group(6).trim(),                // primary key
                    matcher.group(7),                       // update status
                    Integer.parseInt(matcher.group(8)),     // errors
                    Integer.parseInt(matcher.group(9)),     // warnings
                    Integer.parseInt(matcher.group(10)),    // infos
                    Lists.newArrayList(COMMA_SPLITTER.split(matcher.group(11))),    // credentials
                    matcher.group(12),                      // origin type
                    matcher.group(13));                     // origin id

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Couldn't parse (" + e.getMessage() + ") :" + line);
            }
        } else {
            throw new IllegalArgumentException("Couldn't parse (no match) :" + line);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", timestamp)
                .add("duration", duration)
                .add("isDryRun", isDryRun)
                .add("action", action)
                .add("objectType", objectType)
                .add("primaryKey", primaryKey)
                .add("updateStatus", updateStatus)
                .add("errors", errors)
                .add("warnings", warnings)
                .add("infos", infos)
                .add("credentials", credentials)
                .add("originType", originType)
                .add("originId", originId)
                .toString();
    }
}
