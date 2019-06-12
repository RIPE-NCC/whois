package net.ripe.db.whois.common.jdbc;

import com.mchange.v2.c3p0.ConnectionCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/*
    Set connection (session) character encoding.
*/
public class WhoisConnectorCustomizer implements ConnectionCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisConnectorCustomizer.class);

    // the mariadb jdbc driver uses utf8, but data is stored as latin1
    private static final String JDBC_DRIVER_CHARACTER_SET = "utf8mb4";

    @Override
    public void onAcquire(final Connection connection, final String parentDataSourceIdentityToken) {
        setNames(connection, JDBC_DRIVER_CHARACTER_SET);
    }

    @Override
    public void onCheckOut(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    @Override
    public void onCheckIn(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    @Override
    public void onDestroy(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    private void setNames(final Connection connection, final String charset) {
        try (final Statement statement = connection.createStatement()) {
            statement.executeQuery(String.format("SET NAMES '%s' COLLATE DEFAULT", charset));
        } catch (SQLException e) {
            LOGGER.error("Caught {}: {} (ignored)", e.getClass().getName(), e.getMessage());
        }
    }

}
