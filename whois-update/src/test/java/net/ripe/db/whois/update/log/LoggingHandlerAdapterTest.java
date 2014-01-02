package net.ripe.db.whois.update.log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoggingHandlerAdapterTest {
    @Mock LoggerContext loggerContext;

    private StatementInfo statementInfo;
    private ResultInfo resultInfo;
    private LoggingHandlerAdapter subject;

    @Before
    public void setUp() throws Exception {
        statementInfo = new StatementInfo("sql", Maps.<Integer, Object>newHashMap());

        final List<List<String>> rows = Lists.newArrayList();
        resultInfo = new ResultInfo(rows);
    }

    @Test
    public void log() throws Exception {
        subject = new LoggingHandlerAdapter(loggerContext);
        subject.log(statementInfo, resultInfo);

        verify(loggerContext, times(1)).logQuery(statementInfo, resultInfo);
    }
}
