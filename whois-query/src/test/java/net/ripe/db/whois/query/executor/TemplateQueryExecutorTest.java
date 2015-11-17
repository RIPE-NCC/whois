package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class TemplateQueryExecutorTest {
    private TemplateQueryExecutor subject;

    @Before
    public void setUp() throws Exception {
        subject = new TemplateQueryExecutor();
    }

    @Test
    public void supports_template() {
        assertThat(subject.supports(Query.parse("-t inetnum")), is(true));
    }

    @Test
    public void supports_template_case_insensitive() {
        assertThat(subject.supports(Query.parse("-t iNeTnUm")), is(true));
    }

    @Test
    public void supports_template_multiple() {
        assertThat(subject.supports(Query.parse("-t inetnum,inet6num")), is(true));
    }

    @Test
    public void supports_template_with_type() {
        assertThat(subject.supports(Query.parse("-t inetnum,inet6num -T inetnum 0/0")), is(true));
    }

    @Test
    public void supports_template_with_type_invalid() {
        assertThat(subject.supports(Query.parse("-t inetnum,inet6num -T inetnum")), is(true));
    }

    @Test
    public void supports_template_with_searchValue() {
        assertThat(subject.supports(Query.parse("-t inetnum,inet6num query")), is(true));
    }

    @Test
    public void supports_verbose() {
        assertThat(subject.supports(Query.parse("-v inetnum")), is(true));
    }

    @Test
    public void supports_verbose_case_insensitive() {
        assertThat(subject.supports(Query.parse("-v InEtNuM")), is(true));
    }

    @Test
    public void supports_verbose_multiple() {
        assertThat(subject.supports(Query.parse("-v inetnum,inetn6num")), is(true));
    }

    @Test
    public void supports_verbose_with_type() {
        assertThat(subject.supports(Query.parse("-v inetnum,inet6num -T inetnum 0/0")), is(true));
    }

    @Test
    public void supports_verbose_with_searchValue() {
        assertThat(subject.supports(Query.parse("-v inetnum,inet6num query")), is(true));
    }

    @Test
    public void supports_template_with_other_arguments() {
        assertThat(subject.supports(Query.parse("-V ripews -t person")), is(true));
    }

    @Test
    public void getResponse() {
        for (final ObjectType objectType : ObjectType.values()) {
            final String name = objectType.getName();

            final CaptureResponseHandler templateResponseHandler = new CaptureResponseHandler();
            subject.execute(Query.parse("-t " + name), templateResponseHandler);
            final String templateText = templateResponseHandler.getResponseObjects().iterator().next().toString();
            assertThat(templateText, containsString(name));

            final CaptureResponseHandler verboseResponseHandler = new CaptureResponseHandler();
            subject.execute(Query.parse("-v " + name), verboseResponseHandler);
            final String verboseText = verboseResponseHandler.getResponseObjects().iterator().next().toString();
            assertThat(verboseText, containsString(name));

            assertThat(verboseText, not(is(templateText)));
        }
    }

    @Test
    public void getResponse_multiple_template() {
        testInvalidObjectType("-t", "inetnum,inet6num");
    }

    @Test
    public void getResponse_unknown_template() {
        testInvalidObjectType("-t", "unknown");
    }

    @Test
    public void getResponse_multiple_verbose() {
        testInvalidObjectType("-v", "inetnum,inet6num");
    }

    @Test
    public void getResponse_unknown_verbose() {
        testInvalidObjectType("-v", "unknown");
    }

    private void testInvalidObjectType(final String option, final String objectType) {
        final CaptureResponseHandler responseHandler = new CaptureResponseHandler();
        subject.execute(Query.parse(option + " " + objectType), responseHandler);

        final String templateText = responseHandler.getResponseObjects().iterator().next().toString();
        assertThat(templateText, is(QueryMessages.invalidObjectType(objectType).toString()));
    }
}
