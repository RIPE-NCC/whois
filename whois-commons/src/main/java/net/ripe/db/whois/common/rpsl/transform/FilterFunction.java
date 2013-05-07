package net.ripe.db.whois.common.rpsl.transform;

import com.google.common.base.Function;
import net.ripe.db.whois.common.rpsl.RpslObject;

public interface FilterFunction extends Function<RpslObject, RpslObject> {
}
