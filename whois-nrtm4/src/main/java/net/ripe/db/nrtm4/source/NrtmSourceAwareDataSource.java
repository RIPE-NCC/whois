package net.ripe.db.nrtm4.source;

import net.ripe.db.whois.common.source.BasicSourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class NrtmSourceAwareDataSource extends AbstractDataSource {
    private final BasicSourceContext sourceContext;

    @Autowired
    public NrtmSourceAwareDataSource(@Qualifier("nrtmSourceContext") final BasicSourceContext sourceContext) {
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
