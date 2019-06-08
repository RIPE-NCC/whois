package net.ripe.db.whois.common.jdbc;

import com.mchange.v2.c3p0.ConnectionCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class WhoisConnectorCustomizer implements ConnectionCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisConnectorCustomizer.class);

    private static final String CHARACTER_SET_CLIENT = "character_set_client";
    private static final String CHARACTER_SET_RESULTS = "character_set_results";

    private static final String PREFERRED_CHARACTER_SET = "utf8mb4";

    @Override
    public void onCheckOut(final Connection connection, final String parentDataSourceIdentityToken) {
        getAndSetSessionValue(connection, CHARACTER_SET_CLIENT, PREFERRED_CHARACTER_SET);
        getAndSetSessionValue(connection, CHARACTER_SET_RESULTS, PREFERRED_CHARACTER_SET);
    }

    @Override
    public void onCheckIn(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    @Override
    public void onAcquire(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    @Override
    public void onDestroy(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    private void getAndSetSessionValue(final Connection connection, final String key, final String value) {
        final Optional<String> sessionValue = getSessionValue(connection, key);
        if (sessionValue.isPresent() && !value.equals(sessionValue.get())) {
            LOGGER.info("Updating {} from {} to {}", key, sessionValue.get(), value);
            setSessionValue(connection, key, value);
        }
    }

    private Optional<String> getSessionValue(final Connection connection, final String key) {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(String.format("SHOW SESSION VARIABLES LIKE '%s'", key));
            if (!resultSet.next()) {
                return Optional.empty();
            } else {
                return Optional.of(resultSet.getString(2));
            }
        } catch (SQLException e) {
            LOGGER.error("Caught {}: {} (ignored)", e.getClass().getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private void setSessionValue(final Connection connection, final String key, final String value) {
        try (final Statement statement = connection.createStatement()) {
            statement.executeQuery(String.format("SET SESSION %s = '%s'", key, value));
        } catch (SQLException e) {
            LOGGER.error("Caught {}: {} (ignored)", e.getClass().getName(), e.getMessage());
        }
    }

}
