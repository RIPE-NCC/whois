package net.ripe.db.whois.update.domain;

import net.ripe.db.whois.common.rpsl.RpslObject;
import org.apache.commons.lang.Validate;

import javax.annotation.concurrent.Immutable;

@Immutable
public class GeneratedKey {
    private final RpslObject object;
    private final AutoKey autoKey;

    public GeneratedKey(final RpslObject object, final AutoKey autoKey) {
        Validate.notNull(object, "Object cannot be null");
        Validate.notNull(autoKey, "Autokey cannot be null");
        this.object = object;
        this.autoKey = autoKey;
    }

    public RpslObject getObject() {
        return object;
    }

    public AutoKey getAutoKey() {
        return autoKey;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", object.getType().getName(), autoKey);
    }
}
