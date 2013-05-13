package net.ripe.db.whois.common.jdbc.driver;

import java.util.Collections;
import java.util.Map;

public class StatementInfo {
    private final String sql;
    private final Map<Integer, Object> parameters;

    public StatementInfo(final String sql) {
        this.sql = sql;
        this.parameters = Collections.emptyMap();
    }

    public StatementInfo(final String sql, final Map<Integer, Object> parameters) {
        this.sql = sql;
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    public String getSql() {
        return sql;
    }

    public Map<Integer, Object> getParameters() {
        return parameters;
    }
}
