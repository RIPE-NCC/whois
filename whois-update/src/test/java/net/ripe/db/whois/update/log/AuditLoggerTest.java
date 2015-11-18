package net.ripe.db.whois.update.log;

import com.google.common.collect.Maps;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.jdbc.driver.ResultInfo;
import net.ripe.db.whois.common.jdbc.driver.StatementInfo;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.update.domain.Operation;
import net.ripe.db.whois.update.domain.Paragraph;
import net.ripe.db.whois.update.domain.Update;
import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditLoggerTest {
    @Spy ByteArrayOutputStream outputStream;
    @Mock DateTimeProvider dateTimeProvider;
    AuditLogger subject;

    private Update update;

    @Before
    public void setUp() throws Exception {
        when(dateTimeProvider.getCurrentDateTime()).thenReturn(new LocalDateTime(2012, 12, 1, 0, 0));
        update = new Update(new Paragraph("paragraph"), Operation.DELETE, Arrays.asList("reason"), RpslObject.parse("mntner:DEV-ROOT-MNT"));

        subject = new AuditLogger(dateTimeProvider, outputStream);
    }

    @Test
    public void logUpdateStarted() throws Exception {
        subject.logUpdate(update);
        subject.close();

        final String log = outputStream.toString("UTF-8");
        assertThat(log, containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<dbupdate created=\"2012-12-01 00:00:00\">\n" +
                "    <messages/>\n" +
                "    <updates>\n" +
                "        <update attempt=\"1\" time=\"2012-12-01 00:00:00\">\n" +
                "            <key>[mntner] DEV-ROOT-MNT</key>\n" +
                "            <operation>DELETE</operation>\n" +
                "            <reason>reason</reason>\n" +
                "            <paragraph><![CDATA[paragraph]]></paragraph>\n" +
                "            <object><![CDATA[mntner:         DEV-ROOT-MNT\n" +
                "]]></object>\n" +
                "        </update>\n" +
                "    </updates>\n" +
                "</dbupdate>\n"));
    }

    @Test
    public void logUpdateStarted_twice() throws Exception {
        subject.logUpdate(update);
        subject.logUpdate(update);
        subject.close();

        final String log = outputStream.toString("UTF-8");
        assertThat(log, is("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<dbupdate created=\"2012-12-01 00:00:00\">\n" +
                "    <messages/>\n" +
                "    <updates>\n" +
                "        <update attempt=\"2\" time=\"2012-12-01 00:00:00\">\n" +
                "            <key>[mntner] DEV-ROOT-MNT</key>\n" +
                "            <operation>DELETE</operation>\n" +
                "            <reason>reason</reason>\n" +
                "            <paragraph><![CDATA[paragraph]]></paragraph>\n" +
                "            <object><![CDATA[mntner:         DEV-ROOT-MNT\n" +
                "]]></object>\n" +
                "        </update>\n" +
                "    </updates>\n" +
                "</dbupdate>\n"));
    }

    @Test
    public void logException() throws Exception {
        subject.logUpdate(update);
        subject.logException(update, new NullPointerException());
        subject.close();

        final String log = outputStream.toString("UTF-8");
        assertThat(log, containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<dbupdate created=\"2012-12-01 00:00:00\">\n" +
                "    <messages/>\n" +
                "    <updates>\n" +
                "        <update attempt=\"1\" time=\"2012-12-01 00:00:00\">\n" +
                "            <key>[mntner] DEV-ROOT-MNT</key>\n" +
                "            <operation>DELETE</operation>\n" +
                "            <reason>reason</reason>\n" +
                "            <paragraph><![CDATA[paragraph]]></paragraph>\n" +
                "            <object><![CDATA[mntner:         DEV-ROOT-MNT\n" +
                "]]></object>\n" +
                "            <exception>\n" +
                "                <class>java.lang.NullPointerException</class>\n" +
                "                <message><![CDATA[null]]></message>\n" +
                "                <stacktrace><![CDATA[java.lang.NullPointerException\n"));
    }

    @Test
    public void logDuration() throws Exception {
        subject.logUpdate(update);
        subject.logDuration(update, "1 ns");
        subject.close();

        final String log = outputStream.toString("UTF-8");
        assertThat(log, containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<dbupdate created=\"2012-12-01 00:00:00\">\n" +
                "    <messages/>\n" +
                "    <updates>\n" +
                "        <update attempt=\"1\" time=\"2012-12-01 00:00:00\">\n" +
                "            <key>[mntner] DEV-ROOT-MNT</key>\n" +
                "            <operation>DELETE</operation>\n" +
                "            <reason>reason</reason>\n" +
                "            <paragraph><![CDATA[paragraph]]></paragraph>\n" +
                "            <object><![CDATA[mntner:         DEV-ROOT-MNT\n" +
                "]]></object>\n" +
                "            <duration>1 ns</duration>\n" +
                "        </update>\n" +
                "    </updates>\n" +
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
        assertThat(log, containsString("" +
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                "<dbupdate created=\"2012-12-01 00:00:00\">\n" +
                "    <messages/>\n" +
                "    <updates>\n" +
                "        <update attempt=\"1\" time=\"2012-12-01 00:00:00\">\n" +
                "            <key>[mntner] DEV-ROOT-MNT</key>\n" +
                "            <operation>DELETE</operation>\n" +
                "            <reason>reason</reason>\n" +
                "            <paragraph><![CDATA[paragraph]]></paragraph>\n" +
                "            <object><![CDATA[mntner:         DEV-ROOT-MNT\n" +
                "]]></object>\n" +
                "            <query>\n" +
                "                <sql><![CDATA[sql]]></sql>\n" +
                "                <params>\n" +
                "                    <param idx=\"1\">p1</param>\n" +
                "                    <param idx=\"2\">22</param>\n" +
                "                </params>\n" +
                "                <results>\n" +
                "                    <row idx=\"1\">\n" +
                "                        <column idx=\"0\"><![CDATA[c1-1]]></column>\n" +
                "                        <column idx=\"1\"><![CDATA[c1-2]]></column>\n" +
                "                    </row>\n" +
                "                    <row idx=\"2\">\n" +
                "                        <column idx=\"0\"><![CDATA[c2-1]]></column>\n" +
                "                        <column idx=\"1\"><![CDATA[c2-2]]></column>\n" +
                "                    </row>\n" +
                "                </results>\n" +
                "            </query>\n" +
                "        </update>\n" +
                "    </updates>\n" +
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

        verify(outputStream, times(1)).close();
    }
}
