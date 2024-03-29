package net.ripe.db.whois.query.executor;

import com.google.common.base.Splitter;
import net.ripe.db.whois.query.QueryFlag;
import net.ripe.db.whois.query.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

public class HelpQueryExecutorTest {
    private HelpQueryExecutor subject;

    @BeforeEach
    public void setUp() throws Exception {
        subject = new HelpQueryExecutor();
    }

    @Test
    public void supports_help() {
        assertThat(subject.supports(Query.parse("help")), is(true));
    }

    @Test
    public void supports_help_ignore_case() {
        assertThat(subject.supports(Query.parse("HeLp")), is(true));
    }

    @Test
    public void supports_help_with_other_argument() {
        assertThat(subject.supports(Query.parse("help invalid")), is(false));
    }

    @Test
    public void supports_help_with_other_flags() {
        assertThat(subject.supports(Query.parse("help -T person")), is(false));
    }

    @Test
    public void getResponse() {
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(null, responseHandler);
        final String helpText = responseHandler.getResponseObjects().get(0).toString();

        assertThat(helpText, containsString("NAME"));
        assertThat(helpText, containsString("DESCRIPTION"));

        for (final QueryFlag queryFlag : QueryFlag.values()) {
            if (!HelpQueryExecutor.SKIPPED.contains(queryFlag)) {
                assertThat(helpText, containsString(queryFlag.toString()));
            }
        }

        for (final String line : Splitter.on('\n').split(helpText)) {
            if (line.length() > 0) {
                assertThat(line, startsWith("%"));
            }
        }

        assertThat(helpText, containsString("RIPE Database Reference Manual"));
    }
}
