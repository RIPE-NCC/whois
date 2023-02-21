package net.ripe.db.whois.update.handler.validator;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

public class CustomValidationMessage {
    private final Message message;
    private final RpslAttribute attribute;

    public CustomValidationMessage(final Message message, final RpslAttribute attribute) {
        this.attribute= attribute;
        this.message = message;
    }

    public CustomValidationMessage(final Message message) {
        this(message, null);
    }

    public Message getMessage() {
        return message;
    }

    public RpslAttribute getAttribute() {
        return attribute;
    }
}
