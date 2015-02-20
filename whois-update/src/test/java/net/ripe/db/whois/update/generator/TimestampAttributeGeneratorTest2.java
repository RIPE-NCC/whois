package net.ripe.db.whois.update.generator;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.DateTimeProvider;
import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.domain.Maintainers;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.ObjectMessages;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import net.ripe.db.whois.update.keycert.KeyWrapperFactory;
import net.ripe.db.whois.update.keycert.PgpPublicKeyWrapper;
import net.ripe.db.whois.update.keycert.X509CertificateWrapper;
import org.aspectj.lang.annotation.Before;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimestampAttributeGeneratorTest2 {
    @Mock private Update update;
    @Mock private UpdateContext updateContext;
    @Mock private RpslObjectDao rpslObjectDao;
    private TestDateTimeProvider testDateTimeProvider = new TestDateTimeProvider();
    @InjectMocks private TimestampAttributeGenerator subject = new TimestampAttributeGenerator(testDateTimeProvider);

    private static String TIMESTAMP_STRING = "2015-02-27T12:45:00Z";
    private static String TIMESTAMP_STRING_MODIFIED = "2014-01-26T11:44:59Z";

    private DateTime time() {
        DateTimeFormatter parser = ISODateTimeFormat.dateTimeNoMillis();
        DateTime dt = parser.parseDateTime(TIMESTAMP_STRING);
        dt.withZone(DateTimeZone.UTC);
        return dt;
    }

    @Test
    public void generate_multiple_timestamp_attributes_upon_creation() throws Exception {
        testDateTimeProvider.setTime(time());
        final RpslObject autnum = RpslObject.parse(
                "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "remarks: My remark\n" +
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n");

        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final RpslObject updatedObject = subject.generateAttributes(null, autnum, update, updateContext);

        validateAttributeType(updatedObject, AttributeType.CREATED, TIMESTAMP_STRING);
        validateAttributeType(updatedObject, AttributeType.LAST_MODIFIED, TIMESTAMP_STRING);
        validateMessages();
    }

    @Test
    public void generate_single_timestamp_attribute_upon_modification() throws Exception {
        testDateTimeProvider.setTime(time());
        final RpslObject autnum = RpslObject.parse(
                "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "remarks: My remark\n" +
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n");

        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);

        final RpslObject updatedObject = subject.generateAttributes(null, autnum, update, updateContext);

        validateAttributeType(updatedObject, AttributeType.LAST_MODIFIED, TIMESTAMP_STRING);
        validateMessages();
    }

    @Test
    public void generate_two_warnings_upon_creation() throws Exception {
        final RpslObject autnum = RpslObject.parse(
                "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "remarks: My remark\n" +
                        "created: " + TIMESTAMP_STRING_MODIFIED + "\n"+
                        "last-modified:" + TIMESTAMP_STRING_MODIFIED + "\n"+
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n");

        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        subject.generateAttributes(null, autnum, update, updateContext);

        validateMessages(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Test
    public void generate_single_warning_upon_modification() throws Exception {
        final RpslObject autnum = RpslObject.parse(
                "aut-num: AS3333\n" +
                        "descr: ninj-AS\n" +
                        "remarks:\n" +
                        "remarks: My remark\n" +
                        "created: " + TIMESTAMP_STRING_MODIFIED + "\n"+
                        "status: OTHER\n" +
                        "mnt-by: TEST-MNT\n" +
                        "source: RIPE\n");

        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);

        subject.generateAttributes(null, autnum, update, updateContext);

        validateMessages(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
    }

    private void validateAttributeType(final RpslObject rpslObject, final AttributeType attributeType, final String... values) {
        final List attributes = Lists.transform(Arrays.asList(values), new Function<String, RpslAttribute>() {
            @Override
            public RpslAttribute apply(final String input) {
                return new RpslAttribute(attributeType, input);
            }
        });

        assertThat(rpslObject.findAttributes(attributeType), is(attributes));
    }

    private void validateMessages(final Message... messages) {
        if (messages.length == 0) {
            verify(updateContext, never()).addMessage(any(UpdateContainer.class), any(Message.class));
        } else {
            verify(updateContext, times(messages.length)).addMessage(any(UpdateContainer.class), any(Message.class));
            for (final Message message : messages) {
                verify(updateContext).addMessage(update, message);
            }
        }
    }
}
