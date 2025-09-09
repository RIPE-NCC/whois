package net.ripe.db.whois.common.rpsl;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.MessageWithAttribute;
import net.ripe.db.whois.common.Messages;

public final class ValidationMessages {
    private ValidationMessages() {
    }

    public static Message missingMandatoryAttribute(final AttributeType type) {
        return new Message(Messages.Type.ERROR, "Mandatory attribute \"%s\" is missing", type.getName());
    }

    public static Message missingConditionalRequiredAttribute(final AttributeType type) {
        return new Message(Messages.Type.ERROR, "Missing required \"%s\" attribute", type.getName());
    }

    public static Message tooManyAttributesOfType(final AttributeType type) {
        return new Message(Messages.Type.ERROR, "Attribute \"%s\" appears more than once", type.getName());
    }

    public static Message unknownAttribute(final CharSequence key) {
        return new Message(Messages.Type.ERROR, "\"%s\" is not a known RPSL attribute", key);
    }

    public static Message invalidAttributeForObject(final AttributeType attributeType) {
        return new Message(Messages.Type.ERROR, "\"%s\" is not valid for this object type", attributeType.getName());
    }

    public static Message syntaxError(final CharSequence value) {
        return new Message(Messages.Type.ERROR, "Syntax error in %s", value);
    }

    public static Message syntaxError(final CharSequence value, final CharSequence reason) {
        return new Message(Messages.Type.ERROR, "Syntax error in %s (%s)", value, reason);
    }

    public static Message syntaxError(final RpslAttribute attribute, final CharSequence reason) {
        return new MessageWithAttribute(Messages.Type.ERROR, attribute, "Syntax error in %s (%s)", attribute.getCleanValue(), reason);
    }

    public static Message suppliedAttributeReplacedWithGeneratedValue(final AttributeType type) {
        return new Message(Messages.Type.WARNING, "Supplied attribute '%s' has been replaced with a generated value", type.getName());
    }

    public static Message attributeValueConverted(final CharSequence original, final CharSequence converted) {
        return new Message(Messages.Type.INFO, "Value %s converted to %s", original, converted);
    }

    public static Message continuationLinesRemoved() {
        return new Message(Messages.Type.INFO, "Continuation lines are not allowed here and have been removed");
    }

    public static Message remarksReformatted() {
        return new Message(Messages.Type.INFO, "Please use the \"remarks:\" attribute instead of end of line comment on primary key");
    }

    public static Message deprecatedAttributeFound(final AttributeType type) {
        return new Message(Messages.Type.WARNING, "Deprecated attribute \"%s\". This attribute will be removed in the future.", type.getName());
    }

    public static Message deprecatedPasswordAuthentication() {
        return new Message(Messages.Type.WARNING, "MD5 hashed password authentication is deprecated and support will be removed at the end of 2025. Please switch to an alternative authentication method before then.");
    }

}
