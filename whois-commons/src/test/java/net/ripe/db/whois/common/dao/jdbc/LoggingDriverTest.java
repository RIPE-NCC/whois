package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.jdbc.driver.LoggingDriver;
import net.ripe.db.whois.common.jdbc.driver.LoggingHandler;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class LoggingDriverTest {

    private static LoggingHandler currentLoggingHandler;
    private Properties properties;
    private Connection connection;
    private LoggingHandler loggingHandler;
    private LoggingDriver loggingDriver;

    @BeforeEach
    public void setUp() throws Exception {
        loggingHandler = mock(LoggingHandler.class);
        loggingDriver = new LoggingDriver();
        loggingDriver.setLoggingHandler(loggingHandler);
        currentLoggingHandler = loggingHandler;

        properties = new Properties();
        properties.put("user", "dbint");
        properties.put("password", "");
    }

    @AfterEach
    public void tearDown() throws Exception {
        JdbcUtils.closeConnection(connection);
    }

    @Test
    public void acceptsUrl() {
        assertThat(loggingDriver.acceptsURL("jdbc:log:mariadb"), is(true));
        assertThat(loggingDriver.acceptsURL("jdbc:mariadb"), is(false));
        assertThat(loggingDriver.acceptsURL(null), is(false));
    }

    @Test
    public void connect_no_driver() {
        assertThrows(IllegalArgumentException.class, () -> {
            loggingDriver.connect("jdbc:log:mariadb://localhost;logger=" + TestLoggingHandler.class.getName(), new Properties());
        });
    }

    @Test
    public void connect_unknown_driver() {
        assertThrows(IllegalArgumentException.class, () -> {
            connection = loggingDriver.connect("jdbc:log:mariadb://localhost;driver=SomeUnknownDriver;logger=" + TestLoggingHandler.class.getName(), new Properties());
        });
    }

    @Test
    public void connect_invalid_driver(){
        assertThrows(IllegalArgumentException.class, () -> {
            connection = loggingDriver.connect("jdbc:log:mariadb://localhost;driver=java.lang.String;logger=" + TestLoggingHandler.class.getName(), new Properties());
        });
    }

    @Test
    public void connect_unsupported_url() throws SQLException {
        assertThat(loggingDriver.connect(null, new Properties()), is(nullValue()));
    }

    @Test
    public void getMajorVersion() {
        assertThat(loggingDriver.getMajorVersion(), greaterThanOrEqualTo(1));
    }

    @Test
    public void getMinorVersion() {
        assertThat(loggingDriver.getMinorVersion(), greaterThanOrEqualTo(0));
    }

    @Test
    public void jdbcCompliant() {
        assertThat(loggingDriver.jdbcCompliant(), is(false));
    }

    @Test
    public void getPropertyInfo() {
        assertThrows(UnsupportedOperationException.class, () -> {
            loggingDriver.getPropertyInfo("jdbc:log:mariadb://localhost;driver=org.mariadb.jdbc.Driver;logger=" + TestLoggingHandler.class.getName(), properties);
        });
    }

    static class TestLoggingHandler implements LoggingHandler {
        @Override
        public void log(StatementInfo statementInfo, ResultInfo resultInfo) {
            currentLoggingHandler.log(statementInfo, resultInfo);
        }
    }
}
