package net.ripe.db.whois.common.jdbc.driver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.Statement;

class StatementInvocationHandler implements InvocationHandler {
    private final LoggingHandler loggingHandler;
    private final Statement target;


    public StatementInvocationHandler(final LoggingHandler loggingHandler, final Statement target) {
        this.loggingHandler = loggingHandler;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object result = method.invoke(target, args);

        if (result instanceof ResultSet && args != null && args.length > 0) {
            final StatementInfo statementInfo = new StatementInfo((String) args[0]);

            final ResultSet resultSet = (ResultSet) result;
            final ResultSetInvocationHandler invocationHandler = new ResultSetInvocationHandler(loggingHandler, statementInfo, resultSet);

            return Proxy.newProxyInstance(target.getClass().getClassLoader(), new Class<?>[]{ResultSet.class}, invocationHandler);
        }

        return result;
    }
}
