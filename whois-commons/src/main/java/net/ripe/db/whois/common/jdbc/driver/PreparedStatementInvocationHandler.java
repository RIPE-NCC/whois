package net.ripe.db.whois.common.jdbc.driver;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.SortedMap;

class PreparedStatementInvocationHandler implements InvocationHandler {
    private static final List SET_METHODS = Lists.newArrayList("setAsciiStream", "setBigDecimal", "setBinaryStream", "setBoolean", "setByte", "setBytes", "setCharacterStream", "setDate", "setDouble", "setFloat", "setInt", "setLong", "setObject", "setShort", "setString", "setTime", "setTimestamp", "setURL");

    private final LoggingHandler loggingHandler;
    private final PreparedStatement target;
    private final String sql;
    private final SortedMap<Integer, Object> parameters = Maps.newTreeMap();

    public PreparedStatementInvocationHandler(final LoggingHandler loggingHandler, final PreparedStatement target, final String sql) {
        this.loggingHandler = loggingHandler;
        this.target = target;
        this.sql = sql;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object result = method.invoke(target, args);

        if (SET_METHODS.contains(method.getName()) && args[0] instanceof Integer) {
            parameters.put((Integer) args[0], args[1]);
        }

        if ("clearParameters".equals(method.getName())) {
            parameters.clear();
        }

        if (result instanceof ResultSet) {
            final StatementInfo statementInfo = new StatementInfo(sql, parameters);

            final ResultSet resultSet = (ResultSet) result;
            final ResultSetInvocationHandler invocationHandler = new ResultSetInvocationHandler(loggingHandler, statementInfo, resultSet);

            return Proxy.newProxyInstance(target.getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, invocationHandler);
        }

        return result;
    }
}
