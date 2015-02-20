package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.dao.RpslObjectDao;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
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

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING));
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

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING));

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

    @Test
    public void what_to_do_upon_delete() throws Exception {
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


        final RpslObject updatedObject = subject.generateAttributes(null, autnum, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.containsAttribute(AttributeType.LAST_MODIFIED), is(false));
        validateMessages(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
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
