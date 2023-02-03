package net.ripe.db.whois.update.handler.validator;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.rpsl.RpslAttribute;

public class CustomValidationMessage {
    private final Message message;
    private final RpslAttribute attribute;
    private final boolean canBeWarning;

    public CustomValidationMessage(final Message message, final RpslAttribute attribute, final boolean canBeWarning) {
        this.attribute= attribute;
        this.message = message;
        this.canBeWarning = canBeWarning;
    }

    public CustomValidationMessage(final Message message, final RpslAttribute attribute) {
       this(message, attribute, true);
    }

    public CustomValidationMessage(final Message message) {
        this(message, null, true);
    }

    public CustomValidationMessage(final Message message, final boolean canBeWarning) {
        this(message, null, canBeWarning);
    }

    public boolean canBeWarning() {
        return canBeWarning;
    }

    public Message getMessage() {
        return message;
    }

    public RpslAttribute getAttribute() {
        return attribute;
    }
}
