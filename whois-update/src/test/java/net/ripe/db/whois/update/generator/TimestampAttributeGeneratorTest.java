package net.ripe.db.whois.update.generator;

import net.ripe.db.whois.common.TestDateTimeProvider;
import net.ripe.db.whois.common.rpsl.AttributeType;
import net.ripe.db.whois.common.rpsl.RpslAttribute;
import net.ripe.db.whois.common.rpsl.RpslObject;
import net.ripe.db.whois.common.rpsl.RpslObjectBuilder;
import net.ripe.db.whois.common.rpsl.ValidationMessages;
import net.ripe.db.whois.update.domain.Action;
import net.ripe.db.whois.update.domain.Update;
import net.ripe.db.whois.update.domain.UpdateContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static net.ripe.db.whois.common.rpsl.AttributeType.CREATED;
import static net.ripe.db.whois.common.rpsl.AttributeType.LAST_MODIFIED;
import static net.ripe.db.whois.common.rpsl.AttributeType.REMARKS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimestampAttributeGeneratorTest {
    final private static DateTimeFormatter ISO_FORMATER = ISODateTimeFormat.dateTimeNoMillis();

    @Mock private Update update;
    @Mock private UpdateContext updateContext;

    private TimestampAttributeGenerator subject;

    private AttributeGeneratorTestHelper testHelper;
    private TestDateTimeProvider testDateTimeProvider;

    @Before
    public void resetTime(){
        testDateTimeProvider = new TestDateTimeProvider();
        testHelper = new AttributeGeneratorTestHelper(updateContext, update);
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

        final DateTime now = testDateTimeProvider.getCurrentUtcTime();
        testDateTimeProvider.setTime(now);

        final RpslObject updatedObject = subject.generateAttributes(null, PERSON_TEMPLATE, update, updateContext);

        assertThatDateIsExpected(now, updatedObject, CREATED);
        testHelper.validateMessages();
    }

    @Test
    public void action_is_create_input_already_has_created() {
        when(updateContext.getAction(update)).thenReturn(Action.CREATE);

        final DateTime now = testDateTimeProvider.getCurrentUtcTime();

        final RpslObject input = new RpslObjectBuilder(PERSON_TEMPLATE)
                .addAttributeSorted(new RpslAttribute(CREATED, ISO_FORMATER.print(now.plusDays(1)))).get();

        final RpslObject updatedObject = subject.generateAttributes(null, input, update, updateContext);

        assertThatDateIsExpected(now, updatedObject, CREATED);
        testHelper.validateMessages(ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.CREATED));
    }

    @Test
    public void action_is_modify_original_obj_is_10days_old() {
        when(updateContext.getAction(update)).thenReturn(Action.MODIFY);

        final DateTime now = testDateTimeProvider.getCurrentUtcTime();

        final RpslObject original = new RpslObjectBuilder(PERSON_TEMPLATE)
                .addAttributeSorted(new RpslAttribute(CREATED, ISO_FORMATER.print(now.minusDays(10))))
                .addAttributeSorted(new RpslAttribute(LAST_MODIFIED, ISO_FORMATER.print(now.minusDays(10))))
                .get();

        final RpslObject input = new RpslObjectBuilder(original)
                .addAttributeSorted(new RpslAttribute(REMARKS, "test")).get();

        final RpslObject updatedObject = subject.generateAttributes(original, input, update, updateContext);

        assertThatDateIsExpected(now.minusDays(10), updatedObject, CREATED);
        assertThatDateIsExpected(now, updatedObject, LAST_MODIFIED);
        testHelper.validateMessages(ValidationMessages.suppliedAttributeReplacedWithGeneratedValue(AttributeType.LAST_MODIFIED));
    }

    @Test
    public void action_is_modify_input_already_has_modified() {

    }

    @Test
    public void action_is_delete() {

    }

    private void assertThatDateIsExpected(final DateTime expected, final RpslObject updatedObject, final AttributeType attributeType) {
        assertThat(updatedObject.findAttribute(attributeType).getValue(), is(ISO_FORMATER.print(expected)));
    }


}
