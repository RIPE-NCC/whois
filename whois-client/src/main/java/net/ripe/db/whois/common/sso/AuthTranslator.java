package net.ripe.db.whois.common.sso;

import net.ripe.db.whois.common.rpsl.RpslAttribute;

public interface AuthTranslator {
    RpslAttribute translate(final String authType, final String authToken, final RpslAttribute originalAttribute);
}
