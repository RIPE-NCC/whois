package net.ripe.db.whois.api.httpserver;

import org.apache.commons.lang.reflect.FieldUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FilteredPasswordSlf4jRequestLogWriterTest {

    private Logger logger;
    private FilteredSlf4RequestLogWriter subject;

    @Before
    public void setup() throws Exception {
        this.logger = mock(Logger.class);
        this.subject = new FilteredSlf4RequestLogWriter();
        FieldUtils.writeField(subject, "logger", logger, true);
    }

    @Test
    public void no_query_params() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST"));
    }

    @Test
    public void single_password_param() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST?password=test"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST?password=FILTERED"));
    }

    @Test
    public void single_unformatted_param() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST?unformatted"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST?unformatted"));
    }

    @Test
    public void password_and_unformatted_params() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST?password=test&unformatted"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST?password=FILTERED&unformatted"));
    }

    @Test
    public void unformatted_and_password_params() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST?unformatted&password=test"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST?unformatted&password=FILTERED"));
    }

    @Test
    public void multiple_password_params() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST?password=test&password=tset"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST?password=FILTERED&password=FILTERED"));
    }

    @Test
    public void multiple_dry_run_and_passwords_and_unformatted_params() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST?dry-run=true&password=test&password=tset&unformatted"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST?dry-run=true&password=FILTERED&password=FILTERED&unformatted"));
    }

    @Test
    public void multiple_password_and_unformatted_and_dry_run_and_password_params() throws Exception {
        subject.write(requestEntry("/whois/test/person/TP1-TEST?password=test&unformatted&dry-run=true&password=tset"));

        verify(logger).info(contains("/whois/test/person/TP1-TEST?password=FILTERED&unformatted&dry-run=true&password=FILTERED"));
    }

    // helper methods

    private String requestEntry(final String resource) {
        return String.format(
            "127.0.0.1 " +
            "localhost:54632 " +
            "- " +
            "- " +
            "[01/Oct/2021:17:38:52 +0200] " +
            "\"GET %s HTTP/1.1\" " +
            "200 " +
            "1072 " +
            "218000 " +
            "\"-\" " +
            "\"Jersey/2.32 (HttpUrlConnection 11.0.2)\"", resource);
    }



}
