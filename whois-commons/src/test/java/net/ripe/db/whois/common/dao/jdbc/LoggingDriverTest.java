package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.jdbc.driver.LoggingDriver;
import net.ripe.db.whois.common.jdbc.driver.LoggingHandler;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
public class LoggingDriverTest {
    private static LoggingHandler currentLoggingHandler;

    private Properties properties;
    @Mock private LoggingHandler loggingHandler;
    @InjectMocks private LoggingDriver subject;

    private Connection connection;

    @BeforeEach
    public void setUp() throws Exception {
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
        assertThat(subject.acceptsURL("jdbc:log:mariadb"), is(true));
        assertFalse(subject.acceptsURL("jdbc:mariadb"));
        assertFalse(subject.acceptsURL(null));
    }

    @Test
    public void connect_no_driver() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            subject.connect("jdbc:log:mariadb://localhost;logger=" + TestLoggingHandler.class.getName(), new Properties());
        });
    }

    @Test
    public void connect_unknown_driver() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            connection = subject.connect("jdbc:log:mariadb://localhost;driver=SomeUnknownDriver;logger=" + TestLoggingHandler.class.getName(), new Properties());
        });
    }

    @Test
    public void connect_invalid_driver(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            connection = subject.connect("jdbc:log:mariadb://localhost;driver=java.lang.String;logger=" + TestLoggingHandler.class.getName(), new Properties());
        });
    }

    public void connect_unsupported_url() throws SQLException {
        assertNull(subject.connect(null, new Properties()));
    }

    @Test
    public void getMajorVersion() {
        assertThat(subject.getMajorVersion(), greaterThanOrEqualTo(1));
    }

    @Test
    public void getMinorVersion() {
        assertThat(subject.getMinorVersion(), greaterThanOrEqualTo(0));
    }

    @Test
    public void jdbcCompliant() {
        assertFalse(subject.jdbcCompliant());
    }

    @Test
    public void getPropertyInfo() {
        Assertions.assertThrows(UnsupportedOperationException.class, () -> {
            subject.getPropertyInfo("jdbc:log:mariadb://localhost;driver=org.mariadb.jdbc.Driver;logger=" + TestLoggingHandler.class.getName(), properties);

        });
    }

    static class TestLoggingHandler implements LoggingHandler {
        @Override
        public void log(StatementInfo statementInfo, ResultInfo resultInfo) {
            currentLoggingHandler.log(statementInfo, resultInfo);
        }
    }
}
