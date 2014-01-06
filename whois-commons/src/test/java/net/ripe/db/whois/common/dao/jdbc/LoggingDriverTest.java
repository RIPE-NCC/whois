package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.jdbc.driver.LoggingDriver;
import net.ripe.db.whois.common.jdbc.driver.LoggingHandler;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.Connection;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class LoggingDriverTest {
    private static LoggingHandler currentLoggingHandler;

    private Properties properties;
    @Mock private LoggingHandler loggingHandler;
    @InjectMocks private LoggingDriver subject;

    private Connection connection;

    @Before
    public void setUp() throws Exception {
        currentLoggingHandler = loggingHandler;

        properties = new Properties();
        properties.put("user", "dbint");
        properties.put("password", "");
    }

    @After
    public void tearDown() throws Exception {
        JdbcUtils.closeConnection(connection);
    }

    @Test
    public void acceptsUrl() {
        assertTrue(subject.acceptsURL("jdbc:log:mysql"));
        assertFalse(subject.acceptsURL("jdbc:mysql"));
        assertFalse(subject.acceptsURL(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_no_driver() throws Exception {
        subject.connect("jdbc:log:mysql://localhost;logger=" + TestLoggingHandler.class.getName(), new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_unknown_driver() throws Exception {
        connection = subject.connect("jdbc:log:mysql://localhost;driver=SomeUnknownDriver;logger=" + TestLoggingHandler.class.getName(), new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_invalid_driver() throws Exception {
        connection = subject.connect("jdbc:log:mysql://localhost;driver=java.lang.String;logger=" + TestLoggingHandler.class.getName(), new Properties());
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
    public void getPropertyInfo() throws Exception {
        final DriverPropertyInfo[] propertyInfo = subject.getPropertyInfo("jdbc:log:mysql://localhost;driver=com.mysql.jdbc.Driver;logger=" + TestLoggingHandler.class.getName(), properties);

        assertThat(propertyInfo.length, greaterThan(0));
    }

    public static class TestLoggingHandler implements LoggingHandler {
        @Override
        public void log(StatementInfo statementInfo, ResultInfo resultInfo) {
            currentLoggingHandler.log(statementInfo, resultInfo);
        }
    }
}
