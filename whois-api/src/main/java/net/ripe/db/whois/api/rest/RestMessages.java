package net.ripe.db.whois.api.rest;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.QueryFlag;

public class RestMessages {
    public static Message uriMismatch(final CharSequence objectType, final CharSequence key) {
        return new Message(Messages.Type.ERROR, "Object type and key specified in URI (%s: %s) do not match the WhoisResources contents", objectType, key);
    }

    public static Message uriMismatch(final CharSequence objectType) {
        return new Message(Messages.Type.ERROR, "Object type specified in URI (%s) does not match the WhoisResources contents", objectType);
    }

    public static Message singleObjectExpected(final int found) {
        return new Message(Messages.Type.ERROR, "Single object expected in WhoisResources (found %d)", found);
    }

    public static Message disallowedSearchFlag(final QueryFlag flag) {
        return new Message(Messages.Type.ERROR, "Disallowed search flag '%s'", flag.getName());
    }

    public static Message invalidSearchFlag(final CharSequence flagParameter, final CharSequence flagString) {
        return new Message(Messages.Type.ERROR, "Invalid search flag '%s' (in parameter '%s')", flagString, flagParameter);
    }

    public static Message invalidSource(final CharSequence source) {
        return new Message(Messages.Type.ERROR, "Invalid source '%s'", source);
    }

    public static Message queryStringEmpty() {
        return new Message(Messages.Type.ERROR, "Query param 'query-string' cannot be empty");
    }

    public static Message flagsNotAllowedInQueryString() {
        return new Message(Messages.Type.ERROR, "Flags are not allowed in 'query-string'");
    }

    public static Message ssoAuthIgnored() {
        return new Message(Messages.Type.INFO, "RIPE NCC Access token ignored");
    }
}
