package net.ripe.db.whois.common.jdbc.driver;

import com.google.common.collect.Maps;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class LoggingDriver implements Driver {
    private static final String URL_PREFIX = "jdbc:log:";
    private static final String PARAM_DRIVER = "driver";
    private static final String PARAM_LOGGER = "logger";
    private static final String URL_DELIMITERS = ":/;=&?";

    private static final Map<String, Target> targets = Maps.newHashMap();

    static {
        final LoggingDriver driver = new LoggingDriver();
        try {
            DriverManager.registerDriver(driver);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to register driver: " + driver.getClass(), e);
        }
    }

    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            return null;
        }

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

        String targetLogger = null;
        String targetDriver = null;
        final String urlWithoutPrefix = url.substring(URL_PREFIX.length());
        final StringTokenizer urlTokenizer = new StringTokenizer(urlWithoutPrefix, URL_DELIMITERS, true);
        while (urlTokenizer.hasMoreTokens()) {
            final String s = urlTokenizer.nextToken();
            if (PARAM_DRIVER.equals(s)) {
                targetDriver = getParameter(urlTokenizer);
            } else if (PARAM_LOGGER.equals(s)) {
                targetLogger = getParameter(urlTokenizer);
            } else {
                urlBuilder.append(s);
            }
        }

        checkClass(PARAM_DRIVER, targetDriver, Driver.class);

        String targetUrl = urlBuilder.toString();
        while (URL_DELIMITERS.contains(targetUrl.substring(targetUrl.length() - 1))) {
            targetUrl = targetUrl.substring(0, targetUrl.length() - 1);
        }

        final LoggingHandler loggingHandler = createLoggingHandler(targetLogger);

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

    private LoggingHandler createLoggingHandler(final String targetLogger) {
        final Class<LoggingHandler> loggingHandlerClass = checkClass(PARAM_LOGGER, targetLogger, LoggingHandler.class);

        try {
            return loggingHandlerClass.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Unable to create instance of " + targetLogger);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to create instance of " + targetLogger);
        }
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
        return DriverManager.getDriver(getTarget(url).getUrl()).getPropertyInfo(url, info);
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

    // @Override, but only in Java 1.7
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
