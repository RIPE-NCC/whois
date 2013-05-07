package net.ripe.db.whois.common.support.database.diff;

import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Map;
import java.util.Set;

public class Row extends LinkedCaseInsensitiveMap<Object> {
    Row() {
        // Not allowed!
    }

    public Row(Map<String, Object> map) {
        // [EB] We have problems with 'small numbers' being returned as Short/Byte and then later matchers failing
        // and forcing the user to know which column has 'small numbers' and cast the arguments to byte.
        // [ES] also problem matching long values, force convert to integer
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof Short || value instanceof Byte) {
                entry.setValue(((Number) value).intValue());
            }
            if (value instanceof Long) {
                entry.setValue(((Long)value).intValue());   // TODO: [ES] loss of precision
            }
        }

        putAll(map);
    }

    public int getInt(final String key) {
        return (Integer) get(key);
    }

    public String getString(final String key) {
        return (String) get(key);
    }

    public static Row diff(final Row from, final Row to, final Set<String> keepColumns) {
        final Row row = new Row();
        for (final String key : to.keySet()) {
            if (keepColumns.contains(key.toLowerCase()) || !to.get(key).equals(from.get(key))) {
                row.put(key, to.get(key));
            }
        }

        return row;
    }
}
