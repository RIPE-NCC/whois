package net.ripe.db.whois.query;

import com.google.common.base.Joiner;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.common.QueryMessage;
import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.domain.Hosts;
import org.apache.commons.lang.StringUtils;

import java.net.InetAddress;
import java.util.Set;

import static net.ripe.db.whois.common.Messages.Type;

public final class QueryMessages {
    private static final Joiner JOINER = Joiner.on(", ");

    private QueryMessages() {
    }

    // solely used by port43 pipeline handler
    public static Message termsAndConditions() {
        return new Message(Type.INFO, ""
                + "% This is the RIPE Database query service.\n"
                + "% The objects are in RPSL format.\n"
                + "%\n"
                + "% The RIPE Database is subject to Terms and Conditions.\n"
                + "% See http://www.ripe.net/db/support/db-terms-conditions.pdf\n");
    }

    // solely used by port43 pipeline handler
    public static Message servedByNotice(final CharSequence version) {
        return new Message(Type.INFO,
                "%% This query was served by the RIPE Database Query Service version %s (%s)\n",
                version, Hosts.getLocalHostName());
    }

    // solely used by text_export
    public static Message termsAndConditionsDump() {
        return new Message(Type.INFO, "" +
                "#\n" +
                "# The contents of this file are subject to \n" +
                "# RIPE Database Terms and Conditions\n" +
                "#\n" +
                "# http://www.ripe.net/db/support/db-terms-conditions.pdf\n" +
                "#\n");
    }

    public static Message relatedTo(final CharSequence key) {
        return new QueryMessage(Type.INFO, "Information related to '%s'", key);
    }

    public static Message noPersonal() {
        return new QueryMessage(Type.INFO, "Note: %s means ALL personal data has been filtered from this output.", QueryFlag.NO_PERSONAL.getLongFlag());
    }

    public static Message abuseCShown(final CharSequence key, final CharSequence value) {
        return new QueryMessage(Type.INFO, "Abuse contact for '%s' is '%s'", key, value);
    }

    public static Message abuseCNotRegistered(final CharSequence key) {
        return new QueryMessage(Type.INFO, "No abuse contact registered for %s", key);
    }

    public static Message outputFilterNotice() {
        return new QueryMessage(Type.INFO, ""
                + "Note: this output has been filtered.\n"
                + "      To receive output for a database update, use the \"-B\" flag.");
    }

    public static Message primaryKeysOnlyNotice() {
        return new QueryMessage(Type.INFO, ""
                + "Note: this output has been filtered.\n"
                + "Only primary keys and abuse contacts will be visible.\n"
                + "No other contact information will be shown.");
    }

    public static Message versionListStart(final CharSequence type, final CharSequence key) {
        return new QueryMessage(Type.INFO, ""
                + "Version history for %s object \"%s\"\n"
                + "You can use \"%s rev#\" to get an exact version of the object.",
                type, key, QueryFlag.SHOW_VERSION);
    }

    public static Message versionInformation(final int version, final boolean isCurrentVersion, final CIString key, final String operation, final VersionDateTime timestamp) {
        return new QueryMessage(Type.INFO, ""
                + "Version %d %sof object \"%s\"\n"
                + "This version was a %s operation on %s\n"
                + "You can use \"%s\" to get a list of versions for an object.",
                version,
                (isCurrentVersion ? "(current version) " : ""),
                key, operation,
                timestamp,
                QueryFlag.LIST_VERSIONS);
    }

    public static Message versionDifferenceHeader(final int earlierVersion, final int laterVersion, final CIString key) {
        return new QueryMessage(Type.INFO, "Difference between version %d and %d of object \"%s\"",
                earlierVersion,
                laterVersion,
                key);
    }

    public static Message versionDeleted(final CharSequence deletionTime) {
        return new QueryMessage(Type.INFO, "This object was deleted on %-16s", deletionTime);
    }

    public static Message versionPersonRole(final CharSequence type, final CharSequence key) {
        return new QueryMessage(Type.INFO,
                "Version history for %s object \"%s\"\n" +
                "History not available for PERSON and ROLE objects.", type, key);
    }

    public static Message internalErroroccurred() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:100: internal software error\n"
                + "\n"
                + "Please contact ripe-dbm@ripe.net if the problem persists.");
    }

    public static Message noResults(final CharSequence source) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:101: no entries found\n"
                + "\n"
                + "No entries found in source %s.",
                source);
    }

    public static Message unknownSource(final CharSequence source) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:102: unknown source\n"
                + "\n"
                + "\"%s\" is not a known source.\n"
                + "Use \"-q sources\" to list known sources.",
                source);
    }

    public static Message invalidObjectType(final CharSequence type) {
        return new QueryMessage(Type.ERROR, "ERROR:103: unknown object type '%s'", type);
    }

    public static Message invalidAttributeType(final CharSequence type) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:104: unknown attribute\n"
                + "\n"
                + "\"%s\" is not a known attribute.",
                type);
    }

    public static Message attributeNotSearchable(final CharSequence type) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:105: attribute is not searchable\n"
                + "\n"
                + "\"%s\" is not an inverse searchable attribute.",
                type);
    }

    public static Message noSearchKeySpecified() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:106: no search key specified\n"
                + "\n"
                + "No search key specified");
    }

    public static Message inputTooLong() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:107: input line too long\n"
                + "\n"
                + "Input exceeds the maximum line length.");
    }

    public static Message invalidCombinationOfFlags(final CharSequence flag, final CharSequence otherFlag) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:109: invalid combination of flags passed\n"
                + "\n"
                + "The flags \"%s\" and \"%s\" cannot be used together.",
                flag, otherFlag);
    }

    public static Message invalidMultipleFlags(final CharSequence flag) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:110: multiple use of flag\n"
                + "\n"
                + "The flag \"%s\" cannot be used multiple times.",
                flag);
    }

    public static Message malformedQuery() {
        return malformedQuery(null);
    }

    public static Message malformedQuery(final String reason) {
        StringBuilder message = new StringBuilder();

        if (!StringUtils.isBlank(reason)) {
            message.append(reason).append("\n\n");
        }

        message.append("" +
                "ERROR:111: invalid option supplied\n" +
                "\n" +
                "Use help query to see the valid options.");

        return new QueryMessage(Type.ERROR, message.toString());
    }

    public static Message illegalRange() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:112: unsupported query\n"
                + "\n"
                + "'-mM' query options are not allowed on very large ranges/prefixes.\n"
                + "This data is available from the daily object split files:\n"
                + "ftp://ftp.ripe.net/ripe/dbase/split/");
    }

    public static Message unsupportedQuery() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:114: unsupported query\n"
                + "\n"
                + "Search key doesn't match any known query types");
    }

    public static Message invalidSearchKey() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:115: invalid search key\n"
                + "\n"
                + "Search key entered is not valid for the specified object type(s)");
    }

    public static Message unsupportedVersionObjectType() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:116: unsupported query\n"
                + "\n"
                + "Versions are not supported for PERSON/ROLE objects");
    }

    public static Message versionOutOfRange(final int max) {
        return new QueryMessage(Type.ERROR, "" +
                "ERROR:117: version cannot exceed %d for this object\n" +
                "\n" +
                "Versions are numbers greater or equal to 1\n" +
                "but cannot exceed the object's current version number.",
                max);
    }

    public static Message accessDeniedPermanently(final InetAddress remoteAddress) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:201: access denied for %s\n"
                + "\n"
                + "Sorry, access from your host has been permanently\n"
                + "denied because of a repeated excessive querying.\n"
                + "For more information, see\n"
                + "http://www.ripe.net/data-tools/db/faq/faq-db/why-did-you-receive-the-error-201-access-denied",
                remoteAddress.getHostAddress());
    }

    public static Message accessDeniedTemporarily(final InetAddress remoteAddress) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:201: access denied for %s\n"
                + "\n"
                + "Queries from your IP address have passed the daily limit of controlled objects.\n"
                + "Access from your host has been temporarily denied.\n"
                + "For more information, see\n"
                + "http://www.ripe.net/data-tools/db/faq/faq-db/why-did-you-receive-the-error-201-access-denied",
                remoteAddress.getHostAddress());
    }

    public static Message notAllowedToProxy() {
        return new QueryMessage(Type.ERROR, "ERROR:203: you are not allowed to act as a proxy");
    }

    public static Message timeout() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:305: connection has been closed\n"
                + "\n"
                + "The connection to the RIPE Database query server\n"
                + "has been closed after a period of inactivity.");
    }

    public static Message connectionsExceeded(final int connectionLimit) {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:306: connections exceeded\n"
                + "\n"
                + "Number of connections from a single IP address\n"
                + "has exceeded the maximum number allowed (%d).", connectionLimit);
    }

    public static Message duplicateIpFlagsPassed() {
        return new QueryMessage(Type.ERROR, ""
                + "ERROR:901: duplicate IP flags passed\n"
                + "\n"
                + "More than one IP flag (-l, -L, -m, -M or -x) passed to the server.");
    }

    public static Message uselessIpFlagPassed() {
        return new QueryMessage(Type.WARNING, ""
                + "WARNING:902: useless IP flag passed\n"
                + "\n"
                + "An IP flag (-l, -L, -m, -M, -x, -d or -b) used without an IP key.");
    }

    public static Message tagInfoStart(final CharSequence pkey) {
        return new QueryMessage(Type.INFO, "Tags relating to '%s'", pkey);
    }

    public static Message tagInfo(final CharSequence tagType, final CharSequence tagValue) {
        if (tagValue != null && tagValue.length() > 0) {
            return new QueryMessage(Type.INFO, "%s # %s", tagType, tagValue);
        } else {
            return new QueryMessage(Type.INFO, "%s", tagType);
        }
    }

    public static Message unreferencedTagInfo(final CharSequence pkey, final CharSequence value) {
        return new QueryMessage(Type.INFO, "Unreferenced # '%s' will be deleted in %s days", pkey, value);
    }

    public static Message filterTagNote(final Set<? extends CharSequence> includeArgs, final Set<? extends CharSequence> excludeArgs) {
        final StringBuilder message = new StringBuilder("Note: tag filtering is enabled,\n");

        if (!includeArgs.isEmpty()) {
            message.append("      Only showing objects WITH tag(s): ").append(JOINER.join(includeArgs));
            if (!excludeArgs.isEmpty()) {
                message.append('\n');
            }
        }

        if (!excludeArgs.isEmpty()) {
            message.append("      Only showing objects WITHOUT tag(s): ").append(JOINER.join(excludeArgs));
        }

        return new QueryMessage(Type.INFO, message.toString());
    }

    // FIXME: [AH] this message should be '*HAS* invalid syntax'
    public static Message invalidSyntax(final CharSequence objectKey) {
        return new QueryMessage(Type.INFO, "'%s' invalid syntax", objectKey);
    }

    public static Message validSyntax(final CharSequence objectKey) {
        return new QueryMessage(Type.INFO, "'%s' has valid syntax", objectKey);
    }

    public static Message inverseSearchNotAllowed() {
        return new QueryMessage(Type.ERROR, "Inverse search on 'auth' attribute is limited to 'key-cert' objects only");
    }
}
