package net.ripe.db.whois.query.executor;

import net.ripe.db.whois.common.profiles.WhoisProfile;
import net.ripe.db.whois.common.rpsl.ObjectType;
import net.ripe.db.whois.query.QueryMessages;
import net.ripe.db.whois.query.query.Query;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

//TODO [TP] remove activeprofile, application context dependencey when timestamps are always on
@Component
@ActiveProfiles(WhoisProfile.TEST)
@ContextConfiguration(locations = {"classpath:applicationContext-query-test.xml"})
public class TemplateQueryExecutorTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private TemplateQueryExecutor subject;

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
