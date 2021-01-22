package net.ripe.db.whois.update.log;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTest {
    @Spy ByteArrayOutputStream outputStream;
    @Mock DateTimeProvider dateTimeProvider;
    private AuditLogger subject;
    private Update update;

    @Before
    public void setUp() throws Exception {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(LocalDateTime.of(2012, 12, 1, 0, 0));
        update = new Update(new Paragraph("paragraph"), Operation.DELETE, Arrays.asList("reason"), RpslObject.parse("mntner:DEV-ROOT-MNT"));
        subject = new AuditLogger(dateTimeProvider, outputStream);
    }

    @Test
    public void logUpdateStarted() throws Exception {
        subject.logUpdate(update);
        subject.close();

        final String log = outputStream.toString("UTF-8");

        assertThat(trim(log), containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<dbupdate created=\"2012-12-01 00:00:00\">" +
                "<messages/>" +
                "<updates>" +
                "<update attempt=\"1\" time=\"2012-12-01 00:00:00\">" +
                "<key>[mntner] DEV-ROOT-MNT</key>" +
                "<operation>DELETE</operation>" +
                "<reason>reason</reason>" +
                "<paragraph>" +
                "<![CDATA[paragraph]]>" +
                "</paragraph>" +
                "<object>" +
                "<![CDATA[mntner:         DEV-ROOT-MNT]]>" +
                "</object>" +
                "</update>" +
                "</updates>" +
                "</dbupdate>"));
    }

    @Test
    public void logUpdateStarted_twice() throws Exception {
        subject.logUpdate(update);
        subject.logUpdate(update);
        subject.close();

        final String log = outputStream.toString("UTF-8");

        assertThat(trim(log), is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<dbupdate created=\"2012-12-01 00:00:00\">" +
                "<messages/>" +
                "<updates>" +
                "<update attempt=\"2\" time=\"2012-12-01 00:00:00\">" +
                "<key>[mntner] DEV-ROOT-MNT</key>" +
                "<operation>DELETE</operation>" +
                "<reason>reason</reason>" +
                "<paragraph><![CDATA[paragraph]]></paragraph>" +
                "<object><![CDATA[mntner:         DEV-ROOT-MNT]]></object>" +
                "</update>" +
                "</updates>" +
                "</dbupdate>"));
    }

    @Test
    public void logException() throws Exception {
        subject.logUpdate(update);
        subject.logException(update, new NullPointerException());
        subject.close();

        final String log = outputStream.toString("UTF-8");

        assertThat(trim(log), containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<dbupdate created=\"2012-12-01 00:00:00\">" +
                "<messages/>" +
                "<updates>" +
                "<update attempt=\"1\" time=\"2012-12-01 00:00:00\">" +
                "<key>[mntner] DEV-ROOT-MNT</key>" +
                "<operation>DELETE</operation>" +
                "<reason>reason</reason>" +
                "<paragraph>" +
                "<![CDATA[paragraph]]>" +
                "</paragraph>" +
                "<object>" +
                "<![CDATA[mntner:         DEV-ROOT-MNT]]>" +
                "</object>" +
                "<exception>" +
                "<class>java.lang.NullPointerException</class>" +
                "<message>" +
                "<![CDATA[null]]>" +
                "</message>" +
                "<stacktrace>" +
                "<![CDATA[java.lang.NullPointerException"));
    }

    @Test
    public void logDuration() throws Exception {
        subject.logUpdate(update);
        subject.logDuration(update, "1 ns");
        subject.close();

        final String log = outputStream.toString("UTF-8");

        assertThat(trim(log), containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<dbupdate created=\"2012-12-01 00:00:00\">" +
                "<messages/>" +
                "<updates>" +
                "<update attempt=\"1\" time=\"2012-12-01 00:00:00\">" +
                "<key>[mntner] DEV-ROOT-MNT</key>" +
                "<operation>DELETE</operation>" +
                "<reason>reason</reason>" +
                "<paragraph>" +
                "<![CDATA[paragraph]]>" +
                "</paragraph>" +
                "<object>" +
                "<![CDATA[mntner:         DEV-ROOT-MNT" +
                "]]>" +
                "</object>" +
                "<duration>1 ns</duration>" +
                "</update>" +
                "</updates>" +
                "</dbupdate>"));
    }

    @Test
    public void logQuery() throws Exception {
        final Map<Integer, Object> params = Maps.newTreeMap();
        params.put(1, "p1");
        params.put(2, 22);

        final List<List<String>> rows = Arrays.asList(
                Arrays.asList("c1-1", "c1-2"),
                Arrays.asList("c2-1", "c2-2"));

        final StatementInfo statementInfo = new StatementInfo("sql", params);
        final ResultInfo resultInfo = new ResultInfo(rows);

        subject.logUpdate(update);
        subject.logQuery(update, statementInfo, resultInfo);
        subject.close();

        final String log = outputStream.toString("UTF-8");

        assertThat(trim(log), containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                "<dbupdate created=\"2012-12-01 00:00:00\">" +
                "<messages/>" +
                "<updates>" +
                "<update attempt=\"1\" time=\"2012-12-01 00:00:00\">" +
                "<key>[mntner] DEV-ROOT-MNT</key>" +
                "<operation>DELETE</operation>" +
                "<reason>reason</reason>" +
                "<paragraph>" +
                "<![CDATA[paragraph]]>" +
                "</paragraph>" +
                "<object>" +
                "<![CDATA[mntner:         DEV-ROOT-MNT" +
                "]]>" +
                "</object>" +
                "<query>" +
                "<sql>" +
                "<![CDATA[sql]]>" +
                "</sql>" +
                "<params>" +
                "<param idx=\"1\">p1</param>" +
                "<param idx=\"2\">22</param>" +
                "</params>" +
                "<results>" +
                "<row idx=\"1\">" +
                "<column idx=\"0\">" +
                "<![CDATA[c1-1]]>" +
                "</column>" +
                "<column idx=\"1\">" +
                "<![CDATA[c1-2]]>" +
                "</column>" +
                "</row>" +
                "<row idx=\"2\">" +
                "<column idx=\"0\">" +
                "<![CDATA[c2-1]]>" +
                "</column>" +
                "<column idx=\"1\">" +
                "<![CDATA[c2-2]]>" +
                "</column>" +
                "</row>" +
                "</results>" +
                "</query>" +
                "</update>" +
                "</updates>" +
                "</dbupdate>"));
    }

    @Test
    public void empty() throws Exception {
        subject.close();

        assertThat(outputStream.toString("UTF-8"), is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<dbupdate created=\"2012-12-01 00:00:00\">\n" +
                "    <messages/>\n" +
                "    <updates/>\n" +
                "</dbupdate>\n"
        ));

        verify(outputStream).close();
    }

    private String trim(final String value) {
        return value.replaceAll("(?m)^\\s+", "").replaceAll("(?m)\\n", "");
    }
}
