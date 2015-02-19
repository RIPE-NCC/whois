package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.Message;
import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContainer;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.rpsl.AttributeType.CHANGED;
import static net.ripe.db.whois.common.rpsl.AttributeType.CREATED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimestampAttributeGeneratorTest {
    final private static DateTimeFormatter ISO_FORMATER = ISODateTimeFormat.dateTimeNoMillis();

    @Mock private Update update;
    @Mock private UpdateContext updateContext;
//    @InjectMocks
    TimestampAttributeGenerator subject;

//    @Autowired
    private TestDateTimeProvider testDateTimeProvider;

    @Before
    public void resetTime(){
        testDateTimeProvider = new TestDateTimeProvider();
        subject = new TimestampAttributeGenerator(testDateTimeProvider);
        testDateTimeProvider.reset();

    }

    private static final RpslObject PERSON_TEMPLATE = RpslObject.parse("" +
            "person:    Pauleth Palthen\n" +
            "address:   Singel 258\n" +
            "phone:     +31-1234567890\n" +
            "e-mail:    noreply@ripe.net\n" +
            "mnt-by:    OWNER-MNT\n" +
            "nic-hdl:   PP1-TEST\n" +
            "changed:   noreply@ripe.net 20120101\n" +
            "source:    TEST\n");


    @Test
    public void action_is_create_input_does_not_have_created() {
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final DateTime now = getNowInUTC();

        final RpslObject updatedObject = subject.generateAttributes(null, PERSON_TEMPLATE, update, updateContext);

        assertThatDateIsRecent(now, updatedObject, CREATED);

    }

    @Test
    public void action_is_create_input_already_has_created() {
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final DateTime now = getNowInUTC();

        final RpslObject input = new RpslObjectBuilder(PERSON_TEMPLATE).addAttributeAfter(
                new RpslAttribute(CREATED, ISO_FORMATER.print(now.plusDays(1))), CHANGED).get();

        final RpslObject updatedObject = subject.generateAttributes(null, input, update, updateContext);

        assertThatDateIsRecent(now, updatedObject, CREATED);
        validateMessages(ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
    }

    @Test
    public void action_is_modify_input_does_not_have_modified() {
        //check original date is diff.
    }

    @Test
    public void action_is_modify_input_already_has_modified() {

    }

    @Test
    public void action_is_delete() {

    }

    private void assertThatDateIsRecent(final DateTime now, final RpslObject updatedObject, final AttributeType attributeType) {
        assertThat(ISO_FORMATER.parseDateTime(updatedObject.findAttribute(attributeType).getValue()).isBefore(now.plusMinutes(2)), is(true));
    }

    private DateTime getNowInUTC() {
        return testDateTimeProvider.getCurrentDateTime().toDateTime(DateTimeZone.UTC);
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
