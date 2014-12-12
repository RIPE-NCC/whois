package net.ripe.db.whois.common.jdbc.driver;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

class ResultSetInvocationHandler implements InvocationHandler {
    private final LoggingHandler loggingHandler;
    private final StatementInfo statementInfo;
    private final ResultSet target;

    private final List<List<String>> rows = Lists.newArrayList();

    public ResultSetInvocationHandler(final LoggingHandler loggingHandler, final StatementInfo statementInfo, final ResultSet target) {
        this.loggingHandler = loggingHandler;
        this.statementInfo = statementInfo;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Object result = method.invoke(target, args);

        if (loggingHandler != null) {
            if (method.getName().equals("next") && (Boolean) result) {
                handleNext();
            } else if (method.getName().equals("close")) {
                final ResultInfo resultInfo = new ResultInfo(rows);
                loggingHandler.log(statementInfo, resultInfo);
            }
        }

        return result;
    }

    private void handleNext() throws SQLException {
        final ResultSetMetaData md = target.getMetaData();

        final int columnCount = md.getColumnCount();
        final List<String> row = Lists.newArrayListWithCapacity(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            final Object object = target.getObject(i);
            if (object == null) {
                row.add("<null>");
            } else if (object instanceof Blob) {
                final Blob blob = (Blob) object;
                row.add(new String(blob.getBytes(0, (int) blob.length()), Charsets.UTF_8));
            } else if (object instanceof byte[]) {
                row.add(new String((byte[]) object, Charsets.UTF_8));
            } else {
                row.add(object.toString());
            }
        }

        rows.add(Collections.unmodifiableList(row));
    }
}
