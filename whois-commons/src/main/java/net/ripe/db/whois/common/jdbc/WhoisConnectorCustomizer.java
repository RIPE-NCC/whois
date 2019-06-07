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

    private static final String PREFERRED_CHARACTER_SET = "latin1";

    @Override
    public void onCheckOut(final Connection connection, final String parentDataSourceIdentityToken) {
        setSessionCharacterSet(connection);
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

    private void setSessionCharacterSet(final Connection connection) {
        final Optional<String> characterSetClient = getSessionValue(connection, CHARACTER_SET_CLIENT);
        if (characterSetClient.isPresent() && !PREFERRED_CHARACTER_SET.equals(characterSetClient.get())) {
            LOGGER.info("Updating {} from {} to {}", CHARACTER_SET_CLIENT, characterSetClient.get(), PREFERRED_CHARACTER_SET);
            setSessionValue(connection, CHARACTER_SET_CLIENT, PREFERRED_CHARACTER_SET);
        }

        final Optional<String> characterSetResults = getSessionValue(connection, CHARACTER_SET_RESULTS);
        if (characterSetResults.isPresent() && !PREFERRED_CHARACTER_SET.equals(characterSetResults.get())) {
            LOGGER.info("Updating {} from {} to {}", CHARACTER_SET_RESULTS, characterSetResults.get(), PREFERRED_CHARACTER_SET);
            setSessionValue(connection, CHARACTER_SET_RESULTS, PREFERRED_CHARACTER_SET);
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
