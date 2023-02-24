package net.ripe.db.whois.common;

import net.ripe.db.whois.common.rpsl.RpslAttribute;

import javax.annotation.concurrent.Immutable;

@Immutable
public class MessageWithAttribute extends Message {

    private final RpslAttribute rpslAttribute;

    public MessageWithAttribute(final Messages.Type type, final RpslAttribute rpslAttribute, final String text, final Object... args) {
       super(type,text, args);
       this.rpslAttribute = rpslAttribute;
    }

    public RpslAttribute getRpslAttribute() {
        return rpslAttribute;
    }
}
