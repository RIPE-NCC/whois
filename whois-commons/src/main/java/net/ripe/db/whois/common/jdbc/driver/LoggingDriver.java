package net.ripe.db.whois.common.jdbc.driver;

import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

@Component
public class LoggingDriver implements Driver {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoggingDriver.class);

    private static final String URL_PREFIX = "jdbc:log:";
    private static final String PARAM_DRIVER = "driver";
    private static final String URL_DELIMITERS = ":/;=&?";

    private static final Map<String, Target> targets = Maps.newHashMap();

    // FIXME: [AH] design issue: LoggingHandler is passed down to Connections, but on multiple/reinitialized ApplicationContexts, JDBC is keeping a dangling reference to it
    private LoggingHandler loggingHandler;

    @Autowired(required = false)
    public void setLoggingHandler(final LoggingHandler loggingHandler) {
        this.loggingHandler = loggingHandler;
    }

    @PostConstruct
    @SuppressWarnings("EmptyCatchBlock")
    public synchronized void init() {
        // there should be only one LoggingDriver initialized per JVM (or we won't know which applicationContext's loggerContext to log to)
        try {
            Driver driver = DriverManager.getDriver(URL_PREFIX);
            LOGGER.error("LoggingDriver already installed; removing");
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                throw new IllegalStateException("Unable to de-register logging JDBC driver", e);
            }
        } catch (SQLException expected) {
            // ignore
        }

        try {
            DriverManager.registerDriver(this);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to register logging JDBC driver", e);
        }
    }

    @PreDestroy
    public synchronized void shutdown() {
        try {
            DriverManager.deregisterDriver(this);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to de-register logging JDBC driver", e);
        }
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

        // TODO: [AH] don't create proxies if no logginghandler defined (ergo no logging is going to take place)
        final Target target = getTarget(url);
        final Connection connection = DriverManager.getConnection(target.getUrl(), info);
        final ConnectionInvocationHandler invocationHandler = new ConnectionInvocationHandler(connection, target.getLoggingHandler());

        return (Connection) Proxy.newProxyInstance(connection.getClass().getClassLoader(), new Class<?>[]{Connection.class}, invocationHandler);
    }

    private Target getTarget(final String url) {
        Target target = targets.get(url);
        if (target != null) {
            return target;
        }

        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append("jdbc:");

        String targetDriver = null;
        final String urlWithoutPrefix = url.substring(URL_PREFIX.length());
        final StringTokenizer urlTokenizer = new StringTokenizer(urlWithoutPrefix, URL_DELIMITERS, true);
        while (urlTokenizer.hasMoreTokens()) {
            final String s = urlTokenizer.nextToken();
            if (PARAM_DRIVER.equals(s)) {
                targetDriver = getParameter(urlTokenizer);
            } else {
                urlBuilder.append(s);
            }
        }

        checkClass(PARAM_DRIVER, targetDriver, Driver.class);

        final String targetUrl = StringUtils.stripEnd(urlBuilder.toString(), URL_DELIMITERS);

        target = new Target(targetUrl, loggingHandler);

        targets.put(url, target);
        return target;
    }

    private String getParameter(final StringTokenizer urlTokenizer) {
        String result = null;

        if (urlTokenizer.hasMoreTokens() && "=".equals(urlTokenizer.nextToken()) && urlTokenizer.hasMoreTokens()) {
            result = urlTokenizer.nextToken();
            if (urlTokenizer.hasMoreTokens()) {
                urlTokenizer.nextToken();
            }
        }

        return result;
    }

    private <T> Class<T> checkClass(final String name, final String value, final Class<T> type) {
        if (value == null) {
            throw new IllegalArgumentException("Class of type " + type.getName() + " not specified in url property: " + name);
        }

        final Class<?> clazz;
        try {
            clazz = Class.forName(value);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to load class: " + value);
        }

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Specified class " + value + " must be an instance of " + type.getName());
        }

        return (Class<T>) clazz;
    }

    @Override
    public boolean acceptsURL(final String url) {
        return url != null && url.regionMatches(true, 0, URL_PREFIX, 0, URL_PREFIX.length());
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        throw new UnsupportedOperationException("not implemented in MariaDB driver");
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    static class Target {
        private final String url;
        private final LoggingHandler loggingHandler;

        Target(final String url, final LoggingHandler loggingHandler) {
            this.url = url;
            this.loggingHandler = loggingHandler;
        }

        public String getUrl() {
            return url;
        }

        public LoggingHandler getLoggingHandler() {
            return loggingHandler;
        }
    }
}
