package net.ripe.db.whois.common.sso;

import net.ripe.db.whois.common.rpsl.RpslAttribute;

public interface AuthTranslator {
    RpslAttribute translate(String authType, String authToken, RpslAttribute originalAttribute);
}
