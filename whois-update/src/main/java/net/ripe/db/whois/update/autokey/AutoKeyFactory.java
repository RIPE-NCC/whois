package net.ripe.db.whois.update.autokey;

import net.ripe.db.whois.common.domain.CIString;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.AutoKey;

interface AutoKeyFactory<T extends AutoKey> {
    /* Which attribute type does this implementating class support */
    AttributeType getAttributeType();

    /* is the provided key a valid AUTO- key */
    boolean isKeyPlaceHolder(CharSequence s);

    CIString getKeyPlaceholder(CharSequence s);

    /* claim the given key */
    T claim(String key) throws ClaimException;

    /* generate and claim a valid, non-used key */
    T generate(String keyPlaceHolder, RpslObject object);

    boolean isApplicableFor(RpslObject object);
}
