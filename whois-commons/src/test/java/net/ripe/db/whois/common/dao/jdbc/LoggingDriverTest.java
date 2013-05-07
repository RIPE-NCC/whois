package net.ripe.db.whois.common.dao.jdbc;

import net.ripe.db.whois.common.jdbc.driver.LoggingDriver;
import net.ripe.db.whois.common.jdbc.driver.LoggingHandler;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.jdbc.support.JdbcUtils;

import java.sql.*;
import java.util.Properties;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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

    @Test
    public void connect_ps() throws Exception {
        connection = subject.connect("jdbc:log:mysql://localhost;driver=com.mysql.jdbc.Driver;logger=" + TestLoggingHandler.class.getName(), properties);
        assertNotNull(connection);

        final String query = "SELECT 1 FROM DUAL";
        final PreparedStatement ps = connection.prepareStatement(query);
        final ResultSet resultSet = ps.executeQuery();
        resultSet.next();

        JdbcUtils.closeResultSet(resultSet);

        verifyInfo(query);
    }

    @Test
    public void connect_stmt() throws Exception {
        connection = subject.connect("jdbc:log:mysql://localhost;driver=com.mysql.jdbc.Driver;logger=" + TestLoggingHandler.class.getName(), properties);
        assertNotNull(connection);

        final String query = "SELECT 1 FROM DUAL";
        final Statement stmt = connection.createStatement();
        final ResultSet resultSet = stmt.executeQuery(query);
        resultSet.next();
        JdbcUtils.closeResultSet(resultSet);

        verifyInfo(query);
    }

    private void verifyInfo(final String query) {
        verify(loggingHandler, times(1)).log(argThat(new ArgumentMatcher<StatementInfo>() {
            @Override
            public boolean matches(final Object argument) {
                final StatementInfo statementInfo = (StatementInfo) argument;

                assertThat(statementInfo.getSql(), is(query));
                assertThat(statementInfo.getParameters().entrySet(), hasSize(0));

                return true;
            }
        }), argThat(new ArgumentMatcher<ResultInfo>() {
            @Override
            public boolean matches(final Object argument) {
                final ResultInfo resultInfo = (ResultInfo) argument;

                assertThat(resultInfo.getRows(), hasSize(1));

                return true;
            }
        }));
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_no_driver() throws Exception {
        subject.connect("jdbc:log:mysql://localhost;logger=" + TestLoggingHandler.class.getName(), new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_no_logger() throws Exception {
        subject.connect("jdbc:log:mysql://localhost;driver=com.mysql.jdbc.Driver", new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_unknown_driver() throws Exception {
        connection = subject.connect("jdbc:log:mysql://localhost;driver=SomeUnknownDriver;logger=" + TestLoggingHandler.class.getName(), new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_invalid_driver() throws Exception {
        connection = subject.connect("jdbc:log:mysql://localhost;driver=java.lang.String;logger=" + TestLoggingHandler.class.getName(), new Properties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void connect_logger_not_properly_specified() throws Exception {
        connection = subject.connect("jdbc:log:mysql://localhost;driver=com.mysql.jdbc.Driver;logger=", new Properties());
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
        public boolean canLog() {
            return true;
        }

        @Override
        public void log(StatementInfo statementInfo, ResultInfo resultInfo) {
            currentLoggingHandler.log(statementInfo, resultInfo);
        }
    }
}
