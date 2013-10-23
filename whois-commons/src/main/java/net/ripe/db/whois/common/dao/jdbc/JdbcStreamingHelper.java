package net.ripe.db.whois.common.dao.jdbc;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcStreamingHelper {

   private JdbcStreamingHelper() {}

    public static <T> T executeStreaming(final JdbcTemplate jdbcTemplate, final String sql, final PreparedStatementCallback<T> callback) {
        return jdbcTemplate.execute(new ConnectionCallback<T>() {
            @Override
            public T doInConnection(final Connection con) throws SQLException, DataAccessException {
                PreparedStatement ps = null;

                try {
                    // [AK] Creating a statement with criteria below is currently the only way to
                    // get MySQL streaming results rather than preloading the entire resultset in memory.
                    ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                    ps.setFetchSize(Integer.MIN_VALUE);

                    return callback.doInPreparedStatement(ps);
                } finally {
                    JdbcUtils.closeStatement(ps);
                }
            }
        });
    }

    public static <T> T executeStreaming(
            final JdbcTemplate jdbcTemplate,
            final String sql,
            final ResultSetExtractor<T> resultSetExtractor) {

        return executeStreaming(jdbcTemplate, sql, null, resultSetExtractor);
    }

    public static <T> T executeStreaming(
            final JdbcTemplate jdbcTemplate,
            final String sql,
            @Nullable final PreparedStatementSetter preparedStatementSetter,
            final ResultSetExtractor<T> resultSetExtractor) {

        return executeStreaming(jdbcTemplate, sql, new PreparedStatementCallback<T>() {
            @Override
            public T doInPreparedStatement(final PreparedStatement ps) throws SQLException, DataAccessException {
                ResultSet rs = null;

                try {
                    if (preparedStatementSetter != null) {
                        preparedStatementSetter.setValues(ps);
                    }
                    rs = ps.executeQuery();
                    return resultSetExtractor.extractData(rs);
                } finally {
                    JdbcUtils.closeResultSet(rs);
                }
            }
        });
    }

    public static void executeStreaming(
            final JdbcTemplate jdbcTemplate,
            final String sql,
            @Nullable final PreparedStatementSetter preparedStatementSetter,
            final RowCallbackHandler rowCallbackHandler) {
        executeStreaming(jdbcTemplate, sql, preparedStatementSetter, new ResultSetExtractor<Void>() {
            @Override
            public Void extractData(final ResultSet rs) throws SQLException, DataAccessException {
                while (rs.next()) {
                    rowCallbackHandler.processRow(rs);
                }

                return null;
            }
        });
    }

    public static void executeStreaming(
            final JdbcTemplate jdbcTemplate,
            final String sql,
            final RowCallbackHandler rowCallbackHandler) {
        executeStreaming(jdbcTemplate, sql, null, rowCallbackHandler);
    }
}
