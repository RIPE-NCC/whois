package net.ripe.db.whois.common.rpsl.transform;

import net.ripe.db.whois.common.rpsl.RpslObject;

import java.util.function.Function;

public interface FilterFunction extends Function<RpslObject, RpslObject> {
}
