package net.ripe.db.whois.common.support.database.diff;


import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.hamcrest.Matcher;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class Database {
    final Map<String, Table> tables = Maps.newHashMap();

    public Database(final JdbcTemplate jdbcTemplate) {
        for (final String table : getDatabaseTables(jdbcTemplate)) {
            tables.put(table, new Table(jdbcTemplate, table));
        }
    }

    protected Database(final Collection<Table> tables) {
        for (final Table table : tables) {
            this.tables.put(table.getName(), table);
        }
    }

    private Set<String> getDatabaseTables(final JdbcTemplate jdbcTemplate) {
        return jdbcTemplate.execute(new ConnectionCallback<Set<String>>() {
            @Override
            public Set<String> doInConnection(final Connection connection) throws SQLException, DataAccessException {
                final Set<String> tables = Sets.newHashSet();

                ResultSet rs = null;
                try {
                    rs = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
                    while (rs.next()) {
                        final String tableName = rs.getString("TABLE_NAME").toLowerCase();

                        // [EB]: We do *NOT* care for the lock table
                        if (tableName.equals("update_lock") || tableName.equals("x509")) {
                            continue;
                        }

                        tables.add(tableName);
                    }
                } finally {
                    JdbcUtils.closeResultSet(rs);
                }

                return tables;
            }
        });
    }

    public Set<String> getTableNames() {
        return tables.keySet();
    }

    public Table getTable(final String name) {
        return tables.get(name);
    }

    public Rows find(final String table, final Matcher... matchers) {
        return getTable(table).find(matchers);
    }

    public Row get(final String table, final Matcher... matchers) {
        return getTable(table).get(matchers);
    }

    public Rows getAllButTables(final String... excludedTables) {
        Arrays.sort(excludedTables);
        final Rows result = new Rows();
        for (final Table rows : tables.values()) {
            if (Arrays.binarySearch(excludedTables, rows.getName()) < 0) {
                result.addAll(rows);
            }
        }
        return result;
    }

    public Rows getAll() {
        final Rows result = new Rows();
        for (final Table rows : tables.values()) {
            result.addAll(rows);
        }
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        for (final String name : Sets.newTreeSet(tables.keySet())) {
            final Table table = tables.get(name);
            if (table.isEmpty()) {
                continue;
            }
            builder.append(table.toString());
        }

        return builder.toString();
    }

    public static DatabaseDiff diff(final Database from, final Database to) {
        return new DatabaseDiff(from, to);
    }
}
