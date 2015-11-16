package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimestampAttributeGeneratorTest {
    //TODO [TP]: remove defensive code checks wher we check whether timestamp attributes are in original object.
    final private static DateTimeFormatter ISO_FORMATTER = ISODateTimeFormat.dateTimeNoMillis();

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

    private AttributeGeneratorTestHelper testHelper;
    private TestDateTimeProvider testDateTimeProvider = new TestDateTimeProvider();
    private TimestampAttributeGenerator subject;

    @Before
    public void before() {
        testHelper = new AttributeGeneratorTestHelper(updateContext, update);
        subject = new TimestampAttributeGenerator(testDateTimeProvider);

        when(updateContext.getPreparedUpdate(update)).thenReturn(preparedUpdate);
        when(preparedUpdate.getOverrideOptions()).thenReturn(overrideOptions);
    }

    // Start of section skip-last-modified is false

    @Test
    public void create_input_has_no_timestamps() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(null, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_ACTION));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.assertNoMessages();
    }

    @Test
    public void create_input_has_wrong_timestamps() {

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

        testHelper.assertAttributeMessage(updatedObject.findAttribute(AttributeType.CREATED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
        testHelper.assertAttributeMessage(updatedObject.findAttribute(AttributeType.LAST_MODIFIED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Test
    public void create_input_has_right_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_ACTION))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_ACTION))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(null, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_ACTION));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.assertNoMessages();
    }

    @Test
    public void modify_original_has_no_timestamps_input_has_no_timestamps() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject original = TEMPLATE;
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.assertNoMessages();
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

        testHelper.assertNoMessages();
    }

    @Ignore("TP: remove defensive code. all attributes have timestamps")
    @Test
    public void modify_original_has_no_timestamps_input_has_wrong_timestamps() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_OTHER))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_OTHER))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(TEMPLATE, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.assertAttributeMessage(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
        testHelper.assertAttributeMessage(updatedObject.findAttribute(AttributeType.LAST_MODIFIED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Test
    public void modify_original_has_no_timestamps_input_has_right_timestamps() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject original = TEMPLATE;
        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_ACTION))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.assertNoMessages();
    }

    @Test
    public void modify_original_has_timestamps_input_has_wrong_timestamps() {

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

        testHelper.assertAttributeMessage(updatedObject.findAttribute(AttributeType.CREATED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
        testHelper.assertAttributeMessage(updatedObject.findAttribute(AttributeType.LAST_MODIFIED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Test
    public void modify_original_has_timestamps_input_has_right_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(false);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_ACTION))
                .get();


        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.assertNoMessages();
    }

    @Test
    public void delete_original_has_no_timestamps_input_has_no_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);

        final RpslObject original = TEMPLATE;
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.containsAttribute(AttributeType.LAST_MODIFIED), is(false));

        testHelper.assertNoMessages();
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

        testHelper.assertNoMessages();
    }

    @Test
    public void delete_original_has_timestamps_input_has_wrong_timestamps() {

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

        testHelper.assertAttributeMessage(updatedObject.findAttribute(AttributeType.CREATED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
        testHelper.assertAttributeMessage(updatedObject.findAttribute(AttributeType.LAST_MODIFIED),
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Test
    public void delete_original_has_timestamps_input_has_right_timestamps() {

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

        testHelper.assertNoMessages();
    }

    @Ignore("TP: remove defensive code. all attributes have timestamps")
    @Test
    public void delete_original_no_timestamps_input_has_wrong_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);

        final RpslObject original = TEMPLATE;

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_OTHER))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_OTHER))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.containsAttribute(AttributeType.LAST_MODIFIED), is(false));

        testHelper.assertAttributeMessage(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
        testHelper.assertAttributeMessage(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Ignore("TP: remove defensive code. all attributes have timestamps")
    @Test
    public void delete_original_no_timestamps_input_has_right_timestamps() {

        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();

        final RpslObject updatedObject = subject.generateAttributes(TEMPLATE, input, update, updateContext);

        assertThat(updatedObject.containsAttribute(AttributeType.CREATED), is(false));
        assertThat(updatedObject.containsAttribute(AttributeType.LAST_MODIFIED), is(false));

        testHelper.assertAttributeMessage(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
        testHelper.assertAttributeMessage(
                ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    // End of section skip-last-modified is false

    // Start of section skip-last-modified is true

    @Test
    public void create_input_has_no_timestamps_skipLastModified() {
        testDateTimeProvider.setTime(actionTime());
        when(overrideOptions.isSkipLastModified()).thenReturn(true);

        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final RpslObject input = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();
        final RpslObject updatedObject = subject.generateAttributes(null, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_ACTION));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_ACTION));

        testHelper.assertNoMessages();
    }

    @Test
    public void modify_original_has_timestamps_input_has_no_timestamps_skipLastModified() {
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

        testHelper.assertNoMessages();
    }

    @Test
    public void modify_original_has_timestamps_input_has_wrong_timestamps_skipLastModified() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(true);

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

        testHelper.assertNoMessages();
    }

    @Test
    public void modify_original_has_timestamps_input_has_right_timestamps_skipLastModified() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);
        when(overrideOptions.isSkipLastModified()).thenReturn(true);

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

        testHelper.assertNoMessages();
    }

    @Test
    public void delete_original_has_timestamps_input_has_no_timestamps_skipLastModified() {
        testDateTimeProvider.setTime(actionTime());
        when(updateContext.getAction(update)).thenReturn(Action.DELETE);
        when(overrideOptions.isSkipLastModified()).thenReturn(true);

        final RpslObject original = new RpslObjectBuilder(TEMPLATE)
                .addAttributeSorted(new RpslAttribute(AttributeType.CREATED, TIMESTAMP_STRING_PAST))
                .addAttributeSorted(new RpslAttribute(AttributeType.LAST_MODIFIED, TIMESTAMP_STRING_PAST))
                .get();
        final RpslObject input = TEMPLATE;

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThat(updatedObject.findAttribute(AttributeType.CREATED).getValue(), is(TIMESTAMP_STRING_PAST));
        assertThat(updatedObject.findAttribute(AttributeType.LAST_MODIFIED).getValue(), is(TIMESTAMP_STRING_PAST));

        testHelper.assertNoMessages();
    }

    // End of section skip-last-modified is true

    private LocalDateTime actionTime() {
        return ISO_FORMATTER.parseDateTime(TIMESTAMP_STRING_ACTION).withZone(DateTimeZone.UTC).toLocalDateTime();
    }
}
