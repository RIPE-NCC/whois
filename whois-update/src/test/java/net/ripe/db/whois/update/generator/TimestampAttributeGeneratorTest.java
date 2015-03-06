package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.TestTimestampsMode;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.OverrideOptions;
import net.ripe.db.whois.update.domain.PreparedUpdate;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class TimestampAttributeGeneratorTest {
    final private static DateTimeFormatter ISO_FORMATER = ISODateTimeFormat.dateTimeNoMillis();

    private static final String TIMESTAMP_STRING_PAST = "2014-01-26T11:44:59Z";
    private static final String TIMESTAMP_STRING_ACTION = "2015-02-27T12:45:00Z";
    private static final String TIMESTAMP_STRING_OTHER = "2016-02-27T12:45:00Z";

    private static final RpslObject TEMPLATE = RpslObject.parse(
            "aut-num: AS3333\n" +
            "descr: ninj-AS\n" +
            "status: OTHER\n" +
            "mnt-by: TEST-MNT\n" +
            "source: RIPE\n");


    @Mock private Update update;
    @Mock private UpdateContext updateContext;
    @Mock private PreparedUpdate preparedUpdate;
    @Mock private OverrideOptions overrideOptions;

    private TestTimestampsMode testTimestampsMode;
    private AttributeGeneratorTestHelper testHelper;
    private TestDateTimeProvider testDateTimeProvider = new TestDateTimeProvider();
    private TimestampAttributeGenerator subject;

    @Before
    public void before(){
        testHelper = new AttributeGeneratorTestHelper(updateContext, update);
        testTimestampsMode = new TestTimestampsMode();
        testTimestampsMode.setTimestampsOff(false);
        subject = new TimestampAttributeGenerator(testDateTimeProvider, testTimestampsMode);

        when(updateContext.getPreparedUpdate(update)).thenReturn(preparedUpdate);
        when(preparedUpdate.getOverrideOptions()).thenReturn(overrideOptions);
    }

    @Test
    public void create_input_has_no_timestamps() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(null, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_ACTION));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.validateMessages();
    }

    @Test
    public void create_input_has_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(null, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_ACTION));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));
    }

    @Test
    public void modify_original_has_no_timestamps_input_has_no_timestamps() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject original = TEMPLATE;
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        //TODO [TP]: The attribute generator should ensure the correctness of the objects. The AttributeType.CREATED/last-modified
        //TODO [TP]:    will be mandatory in final phase. Therefore by the time the batch job filling the timestamps
        //TODO [TP]:    finishes, at latest, the code and this assertion should be changed
        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.validateMessages();
    }

    @Test
    public void modify_original_has_timestamps_input_has_no_timestamps() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.validateMessages();

    }

    @Test
    public void modify_original_has_timestamps_input_has_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_OTHER))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_OTHER))
                .get();


        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

//        testHelper.validateAttributeMessage(updatedObject.findAttribute(AttributeType.CREATED),
//                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
        testHelper.validateAttributeMessage(updatedObject.findAttribute(AttributeType.LAST_MODIFIED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Test
    public void modify_original_has_timestamps_input_has_no_timestamps_skipLastModified_true() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(true);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_PAST));

        testHelper.validateMessages();
    }


    @Test
    public void modify_original_has_no_timestamps_input_has_no_timestamps_skipLastModified_true() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(true);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE).get();
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.containsAttribute(AttributeType.LAST_MODIFIED), is(false));

        testHelper.validateMessages();
    }

    @Test
    public void delete_original_has_timestamps_input_has_different_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_OTHER))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_OTHER))
                .get();


        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_PAST));

        testHelper.validateMessages();
    }

    @Test
    public void delete_original_has_timestamps_input_has_same_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_PAST));

        testHelper.validateMessages();
    }

    @Test
    public void delete_original_has_timestamps_input_has_no_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_PAST));

        testHelper.validateMessages();
    }

    @Test
    public void delete_original_no_timestamps_input_has_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);

        final RpslObject original = TEMPLATE;

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.containsAttribute(AttributeType.LAST_MODIFIED), is(false));

        testHelper.validateMessages();
    }

    private LocalDateTime actionTime() {
        return ISO_FORMATER.parseDateTime(TIMESTAMP_STRING_ACTION).withZone(DateTimeZone.UTC).toLocalDateTime();
    }
}
