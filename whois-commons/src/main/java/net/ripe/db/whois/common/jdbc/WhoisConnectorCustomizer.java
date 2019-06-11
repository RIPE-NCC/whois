package net.ripe.db.whois.common.jdbc;

import com.mchange.v2.c3p0.ConnectionCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

/*
    Set connection (session) default to latin1 character set.

    defaults locally (osx laptop / homebrew mariadb 10.2.22):
        character_set_client = utf8mb4
        character_set_connection = utf8mb4  *** different ***
        character_set_database = latin1
        character_set_results = utf8mb4
        character_set_server = latin1
        character_set_system = utf8
        collation_connection = utf8mb4_unicode_ci   *** different ***
        collation_database = latin1_swedish_ci
        collation_server = latin1_swedish_ci
    defaults on db-tools-1 (Centos 7.4/MariaDB 10.2.22):
        character_set_client = utf8mb4
        character_set_connection = latin1   **** different ***
        character_set_database = latin1
        character_set_results = utf8mb4
        character_set_server = latin1
        character_set_system = utf8
        collation_connection = latin1_swedish_ci    *** different ***
        collation_database = latin1_swedish_ci
        collation_server = latin1_swedish_ci

    Somehow, UTF8 characters are written into a VARCHAR column.

    Tested:
        * SET NAMES latin1 COLLATE latin1_swedish_ci
            ** doesn't work on db-tools-1 (utf8 written into VARCHAR column)
        * character_set_connection = latin1
            ** doesn't work on db-tools-1
        * character_set_client = utf8mb4
            ** works on both osx and linux (latin1 written into VARCHAR column?)
*/
public class WhoisConnectorCustomizer implements ConnectionCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WhoisConnectorCustomizer.class);

    private static final String PREFERRED_CHARACTER_SET = "latin1";
    private static final String PREFERRED_COLLATION = "latin1_swedish_ci";

    @Override
    public void onCheckOut(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    @Override
    public void onCheckIn(final Connection connection, final String parentDataSourceIdentityToken) {
        // do nothing
    }

    @Override
    public void onAcquire(final Connection connection, final String parentDataSourceIdentityToken) {
        getAndSetSessionValue(connection, "character_set_client", "utf8mb4");              // TODO: using latin1 will cause utf8 characters to be written to index tables.
        getAndSetSessionValue(connection, "character_set_connection", PREFERRED_CHARACTER_SET);
        getAndSetSessionValue(connection, "character_set_results", PREFERRED_CHARACTER_SET);
        getAndSetSessionValue(connection, "collation_connection", PREFERRED_COLLATION);
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
