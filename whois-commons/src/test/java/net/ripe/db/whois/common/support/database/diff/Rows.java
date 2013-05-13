package net.ripe.db.whois.common.support.database.diff;

import com.google.common.collect.Lists;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsMapContaining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class Rows extends ArrayList<Row> {
    public static IsMapContaining<String, Object> with(final String key, final Object value) {
        return with(key, is(value));
    }

    @SuppressWarnings("unchecked")
    public static IsMapContaining<String, Object> with(final String key, final Matcher<?> matcher) {
        return new IsMapContaining<String, Object>(equalToIgnoringCase(key), (Matcher<? super Object>) matcher);
    }

    /** Find identical matching rows */
    public Row get(final Row row) {
        return get(getMatchersForColumns(row, row.keySet()));
    }

    public Rows find(final Row row) {
        return find(getMatchersForColumns(row, row.keySet()));
    }

    /* generate matchers from this rows columns */
    static Matcher[] getMatchersForColumns(final Row row, final Set<String> columns) {
        final List<Matcher> matchers = Lists.newArrayList();
        for (final String column : columns) {
            matchers.add(with(column, row.get(column)));
        }
        return matchers.toArray(new Matcher[matchers.size()]);
    }

    /** Get a single row that matches the matchers */
    public Row get(final Matcher... matchers) {
        final Rows results = find(matchers);
        final StringBuilder builder = new StringBuilder("Expected to find a single record for: [");
        if (matchers.length > 0) builder.append('\n');
        for (int i = 0; i < matchers.length; i++) {
            if (i > 0) builder.append(",\n");
            builder.append(matchers[i].toString());
        }
        builder.append("]");

        assertThat(builder.toString(), results, hasSize(1));

        return results.get(0);
    }

    /** Get the rows matching the matchers */
    public Rows find(final Matcher... matchers) {
        final Rows result = new Rows();
        if (matchers.length == 0) {
            result.addAll(this);
            return result;
        }

        row:
        for (final Row row : this) {
            for (final Matcher matcher : matchers) {
                if (!matcher.matches(row)) {
                    continue row;
                }
            }

            result.add(row);
        }
        return result;
    }
}
