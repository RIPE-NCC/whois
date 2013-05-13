package net.ripe.db.whois.common.support.database.diff;

import com.google.common.collect.Sets;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class Table extends Rows {
    private final String name;
    private final Set<String> primaryColumns;

    public Table(final JdbcTemplate jdbcTemplate, final String name) {
        this.name = name;
        this.addAll(jdbcTemplate.query("SELECT * FROM " + name, new TableMapperResultSetExtractor()));

        primaryColumns = jdbcTemplate.execute(new ConnectionCallback<Set<String>>() {
            @Override
            public Set<String> doInConnection(final Connection connection) throws SQLException, DataAccessException {
                ResultSet rs = null;

                try {
                    rs = connection.getMetaData().getPrimaryKeys(null, null, name);
                    final Set<String> columns = Sets.newLinkedHashSet();
                    while (rs.next()) {
                        columns.add(rs.getString("COLUMN_NAME").toLowerCase());
                    }

                    return Collections.unmodifiableSet(columns);
                } finally {
                    JdbcUtils.closeResultSet(rs);
                }
            }
        });
    }

    protected Table(final Table table, final Rows rows) {
        name = table.name;
        primaryColumns = table.primaryColumns;
        addAll(rows);
    }

    private class TableMapperResultSetExtractor implements ResultSetExtractor<Rows> {
        private final ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper();

        @Override
        public Rows extractData(final ResultSet rs) throws SQLException, DataAccessException {
            final Rows rows = new Rows();

            int rowNum = 0;
            while (rs.next()) {
                final Map<String, Object> objectMap = columnMapRowMapper.mapRow(rs, rowNum++);
                if (objectMap != null) {
                    rows.add(new Row(objectMap));
                }
            }

            return rows;
        }
    }

    public String getName() {
        return name;
    }

    /**
     * Find rows only matching the primary key columns
     */
    public Rows find(final Row rowWithMatchingPkey) {
        return find(getMatchersForColumns(rowWithMatchingPkey, primaryColumns));
    }

    public Row rowDiff(final Row fromRow, final Row toRow) {
        return Row.diff(fromRow, toRow, primaryColumns);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(name).append(":\n");
        for (final Row row : this) {
            builder.append(' ').append(row.toString()).append("\n");
        }
        return builder.toString();
    }
}
