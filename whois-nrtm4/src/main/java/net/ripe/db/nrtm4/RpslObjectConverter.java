package net.ripe.db.nrtm4;

import com.fasterxml.jackson.databind.util.StdConverter;
import net.ripe.db.whois.common.rpsl.RpslObject;


public class RpslObjectConverter extends StdConverter<RpslObject, String> {

    @Override
    public String convert(final RpslObject value) {
        return value.toString();
    }

}
