package net.ripe.db.whois.common.source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class SourceAwareDataSource extends AbstractDataSource {
    private final SourceContext sourceContext;

    @Autowired
    public SourceAwareDataSource(final SourceContext sourceContext) {
        this.sourceContext = sourceContext;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getActualDataSource().getConnection();
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return getActualDataSource().getConnection(username, password);
    }

    private DataSource getActualDataSource() {
        return sourceContext.getCurrentSourceConfiguration().getDataSource();
    }
}
