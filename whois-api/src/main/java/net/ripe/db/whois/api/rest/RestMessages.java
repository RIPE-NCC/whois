package net.ripe.db.whois.api.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.Messages;
import net.ripe.db.whois.query.QueryFlag;
import org.xml.sax.SAXParseException;

public class RestMessages {
    public static Message pkeyMismatch(final CharSequence key) {
        return new Message(Messages.Type.ERROR, "Primary key (%s) cannot be modified.", key);
    }

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

    public static Message deprecatedPasswordParameter(){
        return new Message(Messages.Type.WARNING, "Password parameter has been deprecated, use basic auth instead");
    }

    public static Message jsonProcessingError(final JsonProcessingException e) {
        final String trimmed = e.getMessage()
                                    .replaceAll("(?m) \\(.*\\)$", "")
                                    .replaceAll("(?m)^ at .*$", "")
                                    .replaceAll("\n*", "");
        if (e.getLocation() != null) {
            return new Message(Messages.Type.ERROR, "JSON processing exception: %s (line: %s, column: %s)", trimmed, e.getLocation().getLineNr(), e.getLocation().getColumnNr());
        }
        return new Message(Messages.Type.ERROR, "JSON processing exception: %s", trimmed);
    }

    public static Message xmlProcessingError(final SAXParseException e) {
        if (e.getLineNumber() != -1 && e.getColumnNumber() != -1) {
            return new Message(Messages.Type.ERROR, "XML processing exception: %s (line: %s, column: %s)", e.getMessage(), e.getLineNumber(), e.getColumnNumber());
        }
        return new Message(Messages.Type.ERROR, "XML processing exception: %s", e.getMessage());
    }
}
