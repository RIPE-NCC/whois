package net.ripe.db.whois.common.jdbc.driver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

class ConnectionInvocationHandler implements InvocationHandler {

    private final Connection target;
    private final LoggingHandler loggingHandler;

    public ConnectionInvocationHandler(final Connection target, final LoggingHandler loggingHandler) {
        this.target = target;
        this.loggingHandler = loggingHandler;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object result = method.invoke(target, args);

        if (result instanceof PreparedStatement && args != null && args.length > 0) {
            final InvocationHandler invocationHandler = new PreparedStatementInvocationHandler(loggingHandler, (PreparedStatement) result, (String) args[0]);
            return Proxy.newProxyInstance(target.getClass().getClassLoader(), new Class<?>[]{PreparedStatement.class}, invocationHandler);
        } else if (result instanceof Statement) {
            final InvocationHandler invocationHandler = new StatementInvocationHandler(loggingHandler, (Statement) result);
            return Proxy.newProxyInstance(target.getClass().getClassLoader(), new Class<?>[]{Statement.class}, invocationHandler);
        }

        return result;
    }
}
