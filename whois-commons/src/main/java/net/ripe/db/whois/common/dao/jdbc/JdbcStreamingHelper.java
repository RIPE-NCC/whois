package net.ripe.db.whois.common.dao.jdbc;

import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.JdbcUtils;

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class JdbcStreamingHelper {

    private JdbcStreamingHelper() {}

    public static <T> T executeStreaming(final JdbcTemplate jdbcTemplate, final String sql, final PreparedStatementCallback<T> callback) {
        return jdbcTemplate.execute((ConnectionCallback<T>) con -> {
            PreparedStatement ps = null;
            try {
                // [AK] Creating a statement with criteria below is currently the only way to
                // get database streaming results rather than preloading the entire resultset in memory.
                ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                ps.setFetchSize(1);
                return callback.doInPreparedStatement(ps);
            } finally {
                JdbcUtils.closeStatement(ps);
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

        return executeStreaming(jdbcTemplate, sql, (PreparedStatementCallback<T>) ps -> {
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
        });
    }

    public static void executeStreaming(
            final JdbcTemplate jdbcTemplate,
            final String sql,
            @Nullable final PreparedStatementSetter preparedStatementSetter,
            final RowCallbackHandler rowCallbackHandler) {
        executeStreaming(jdbcTemplate, sql, preparedStatementSetter, (ResultSetExtractor<Void>) rs -> {
            while (rs.next()) {
                rowCallbackHandler.processRow(rs);
            }
            return null;
        });
    }

    public static void executeStreaming(
            final JdbcTemplate jdbcTemplate,
            final String sql,
            final RowCallbackHandler rowCallbackHandler) {
        executeStreaming(jdbcTemplate, sql, null, rowCallbackHandler);
    }
}
