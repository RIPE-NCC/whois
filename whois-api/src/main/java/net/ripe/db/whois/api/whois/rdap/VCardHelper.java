package net.ripe.db.whois.api.whois.rdap;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VCardHelper {

    public static List createList(Object... objects) {
        return Lists.newArrayList(objects);
    }

    public static <K, V> HashMap createMap(Map.Entry<K, V>... entries) {
        HashMap <K, V> ret = new HashMap <K, V>();
        for (Map.Entry<K, V>entry : entries) {
            ret.put(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    public static List createName(final String surname, final String given, final String prefix, final String suffix, final List honorifics) {
        return createList(surname, given, prefix, suffix, honorifics);
    }

    public static List createHonorifics(final String prefix, final String suffix) {
        return createList(prefix, suffix);
    }

    public static List createAddress(final String pobox, final String ext, final String street, final String locality, final String region, final String code, final String country) {
        return createList(pobox, ext, street, locality, region, code, country);
    }
}
